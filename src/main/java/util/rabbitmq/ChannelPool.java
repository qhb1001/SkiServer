package util.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public interface ChannelPool {
    void init();

    Channel take() throws InterruptedException;
    
    boolean add(Channel channel);

    Connection getConnection();
}
