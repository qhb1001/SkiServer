package consumer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import model.LiftRideMessage;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecvMT {

    private final static String QUEUE_NAME = "newLiftRideQueue";

    public static void main(String[] argv) throws Exception {


        Options options = new Options();

        Option t = new Option("t", "numThreads", true, "number of consumer thread to retrieve message from rabbitmq queue.");
        t.setRequired(true);
        options.addOption(t);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        int numThreads = 128;
        LiftRideResult result = new LiftRideResult();

        try {
            cmd = parser.parse(options, argv);
            numThreads = Integer.parseInt(cmd.getOptionValue("numThreads"));
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final Channel channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                    // max one message per receiver
                    channel.basicQos(1);
                    System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
                        Gson gson = new Gson();
                        LiftRideMessage liftRideMessage = gson.fromJson(message, LiftRideMessage.class);
                        result.addTime(liftRideMessage.getSkierID(), liftRideMessage.getTime());
                    };
                    // process messages
                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
                } catch (IOException ex) {
                    Logger.getLogger(RecvMT.class.getName()).log(Level.SEVERE, null, ex);
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