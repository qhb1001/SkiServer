package util.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQDriver {
    private static String RABBITMQ_URL = System.getProperty("RABBITMQ_URI");

    public static ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = null;
        try {
            factory = new ConnectionFactory();
            factory.setUri(RABBITMQ_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return factory;
    }
}
