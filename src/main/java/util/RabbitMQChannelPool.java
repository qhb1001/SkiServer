package util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class RabbitMQChannelPool extends BasePooledObjectFactory<Channel> {

    private ConnectionFactory connectionFactory;
    private Connection connection;

    public RabbitMQChannelPool(ConnectionFactory connectionFactory, Connection connection) {
        this.connectionFactory = connectionFactory;
        this.connection = connection;
    }

    @Override
    public Channel create() throws Exception {
        return connection.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<Channel>(channel);
    }

    @Override
    public void passivateObject(PooledObject<Channel> p) throws Exception {
        super.passivateObject(p);
    }

    @Override
    public void destroyObject(PooledObject<Channel> p, DestroyMode mode) throws Exception {
        super.destroyObject(p, mode);
    }
}
