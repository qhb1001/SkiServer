package consumer.skiermicroservice;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import consumer.skiermicroservice.dao.LiftRideDao;
import consumer.skiermicroservice.model.LiftRide;
import model.LiftRideMessage;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.ArrayList;

public class RecvMT {

//    private final static String QUEUE_NAME = "newLiftRideQueue";
    public static final String liftRideExchange = "liftRideExchange";

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

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        int numThreads = 128;
        String HOST_NAME = "127.0.0.1";
        String PORT = "3306";
        String DATABASE = "SkierMicroService";
        String USERNAME = "username";
        String PASSWORD = "password";

        try {
            cmd = parser.parse(options, argv);
            numThreads = Integer.parseInt(cmd.getOptionValue("numThreads"));
            HOST_NAME = cmd.getOptionValue("dbHostName");
            PORT = cmd.getOptionValue("dbPort");
            DATABASE = cmd.getOptionValue("dbSchema");
            USERNAME = cmd.getOptionValue("dbUsername");
            PASSWORD = cmd.getOptionValue("dbPassword");
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
        factory.setUri("amqp://bo:passwordforrabbitmq@54.208.30.94:5672/vhost");
        final Connection connection = factory.newConnection();
        final Channel mainChannel = connection.createChannel();
        mainChannel.exchangeDeclare(liftRideExchange, "fanout");
        String subQueueName = mainChannel.queueDeclare("skierMicroServiceSubscriptionQueue", false, false, true, null).getQueue();
        mainChannel.queueBind(subQueueName, liftRideExchange, "");

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
//                    channel.queueDeclare(subQueueName, true, false, false, null);
                    // max one message per receiver
                    channel.basicQos(1);
                    System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

                    LiftRideDao liftRideDao = new LiftRideDao(finalHOST_NAME, finalPORT, finalDATABASE, finalUSERNAME, finalPASSWORD);
                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                        System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
                        Gson gson = new Gson();
                        LiftRideMessage liftRideMessage = gson.fromJson(message, LiftRideMessage.class);
                        LiftRide newLiftRide = new LiftRide(liftRideMessage.getSkierID(), liftRideMessage.getLiftID(),
                                liftRideMessage.getSeasonID(), liftRideMessage.getDayID(), 100);

                        liftRideDao.createLiftRide(newLiftRide);
                    };
                    // process messages
                    channel.basicConsume(subQueueName, false, deliverCallback, consumerTag -> { });
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