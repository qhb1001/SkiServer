package consumer.resortmicroservice;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import consumer.resortmicroservice.dao.LiftRideDao;
import consumer.resortmicroservice.model.*;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.ArrayList;

public class RPCRecvMT {

    private final static String RPC_QUEUE_NAME = "resort_rpc_queue";

    public static void main(String[] argv) throws Exception {


        Options options = new Options();

        Option t = new Option("t", "numThreads", true, "number of consumer thread to retrieve message from rabbitmq queue.");
        t.setRequired(true);
        options.addOption(t);

        Option h = new Option("h", "dbHostName", true, "mysql host name");
        h.setRequired(true);
        options.addOption(h);

        Option p = new Option("p", "dbPort", true, "mysql port");
        p.setRequired(true);
        options.addOption(p);

        Option s = new Option("s", "dbSchema", true, "mysql schema");
        s.setRequired(true);
        options.addOption(s);

        Option u = new Option("u", "dbUsername", true, "mysql username");
        u.setRequired(true);
        options.addOption(u);

        Option w = new Option("w", "dbPassword", true, "mysql password");
        w.setRequired(true);
        options.addOption(w);

        Option r = new Option("r", "rabbitmqUri", true, "rabbitmq Uri");
        r.setRequired(true);
        options.addOption(r);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        int numThreads = 128;
        String HOST_NAME = "127.0.0.1";
        String PORT = "3306";
        String DATABASE = "ResortMicroService";
        String USERNAME = "username";
        String PASSWORD = "password";
        String RABBITMQURI = "rabbitmqUri";

        try {
            cmd = parser.parse(options, argv);
            numThreads = Integer.parseInt(cmd.getOptionValue("numThreads"));
            HOST_NAME = cmd.getOptionValue("dbHostName");
            PORT = cmd.getOptionValue("dbPort");
            DATABASE = cmd.getOptionValue("dbSchema");
            USERNAME = cmd.getOptionValue("dbUsername");
            PASSWORD = cmd.getOptionValue("dbPassword");
            RABBITMQURI = cmd.getOptionValue("rabbitmqUri");
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
        factory.setUri(RABBITMQURI);
//        factory.setUri("amqp://bo:passwordforrabbitmq@54.208.30.94:5672/vhost");
        final Connection connection = factory.newConnection();

        String finalHOST_NAME = HOST_NAME;
        String finalPORT = PORT;
        String finalDATABASE = DATABASE;
        String finalUSERNAME = USERNAME;
        String finalPASSWORD = PASSWORD;
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final Channel channel = connection.createChannel();
                    channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
                    channel.queuePurge(RPC_QUEUE_NAME);
                    channel.basicQos(1);
                    System.out.println(" [x] Awaiting RPC requests");

                    Object monitor = new Object();
                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                                .Builder()
                                .correlationId(delivery.getProperties().getCorrelationId())
                                .build();

                        String response = "";

                        LiftRideDao liftRideDao = new LiftRideDao(finalHOST_NAME, finalPORT, finalDATABASE, finalUSERNAME, finalPASSWORD);

                        try {
                            String message = new String(delivery.getBody(), "UTF-8");
                            Gson gson = new Gson();
                            GetRequestRPCMessageBase base = gson.fromJson(message, GetRequestRPCMessageBase.class);

                            switch (base.getType()) {
                                case "UniqueSkier":
                                    UniqueSkierMessage uniqueSkierMessage = gson.fromJson(message, UniqueSkierMessage.class);
                                    Integer uniqueSkierCount = liftRideDao.getUniqueSkiersCount(
                                            uniqueSkierMessage.getResortId(),
                                            uniqueSkierMessage.getSeasonId(),
                                            uniqueSkierMessage.getDayId()
                                    );
                                    response = "{ \"numSkier\": " + uniqueSkierCount + "}";
                                    break;
                                case "SkiDayVertical":
                                    SkiDayVerticalMessage skiDayVerticalMessage = gson.fromJson(message, SkiDayVerticalMessage.class);
                                    Integer verticals = liftRideDao.getSkierDayVerticalForSkier(
                                            skiDayVerticalMessage.getResortId(),
                                            skiDayVerticalMessage.getSeasonId(),
                                            skiDayVerticalMessage.getDayId(),
                                            skiDayVerticalMessage.getSkierId()
                                    );
                                    response = String.valueOf(verticals);
                                    break;
                                case "TotalVertical":
                                    TotalVerticalMessage totalVerticalMessage = gson.fromJson(message, TotalVerticalMessage.class);
                                    SeasonalVerticals seasonalVerticals = liftRideDao.getTotalVerticalForResort(
                                            totalVerticalMessage.getSkierId(),
                                            totalVerticalMessage.getResortId(),
                                            totalVerticalMessage.getSeasonId()
                                    );
                                    response = gson.toJson(seasonalVerticals);
                                    break;
                            }

                        } catch (RuntimeException e) {
                            System.out.println(" [.] " + e.toString());
                        } finally {
                            channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                            // RabbitMq consumer worker thread notifies the RPC server owner thread
                            synchronized (monitor) {
                                monitor.notify();
                            }
                        }
                    };

                    channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));
                    // Wait and be prepared to consume the message from RPC client.
                    while (true) {
                        synchronized (monitor) {
                            try {
                                monitor.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
//                    Logger.getLogger(RecvMT.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        ArrayList<Thread> threads = new ArrayList<>(0);
        for (int i=0;i<numThreads; i++) {
            Thread recv = new Thread(runnable);
            threads.add(recv);
            recv.start();
        }
        for (int i=0;i<numThreads;i++) {
            threads.get(i).join();
        }

    }
}