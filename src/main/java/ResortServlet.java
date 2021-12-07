import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import consumer.resortmicroservice.model.UniqueSkierMessage;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import util.RabbitMQChannelPool;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "ResortServlet", value = "/ResortServlet")
public class ResortServlet extends HttpServlet {

    private ConnectionFactory factory;
    private Connection connection;
    private ObjectPool<Channel> channelPool;

    @Override
    public void init() {
        try {
            factory = new ConnectionFactory();
            factory.setHost("localhost");
//            factory.setUri("amqp://bo:passwordforrabbitmq@54.208.30.94:5672/vhost");
            connection = factory.newConnection();

            channelPool = new GenericObjectPool<Channel>(new RabbitMQChannelPool(factory, connection));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\n" +
                    "  \"error\": \"urlPath is empty.\"\n" +
                    "}");
            return;
        }

        // urlPath  = "/{resortID}/seasons/{seasonID}/day/{dayID}/skiers"
        // urlParts = [ , {resortID}, seasons, {seasonID}, day, {dayID}, skiers]
        String[] urlParts = urlPath.split("/");

        if (!isGetUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else {
            try (RPCClient getRequestRpc = new RPCClient(connection, channelPool.borrowObject())) {


                Gson gson = new Gson();
                UniqueSkierMessage uniqueSkierMessage = new UniqueSkierMessage(
                        "UniqueSkier",
                        Integer.parseInt(urlParts[1]),
                        Integer.parseInt(urlParts[3]),
                        Integer.parseInt(urlParts[5])
                );
                String messageToQueue = gson.toJson(uniqueSkierMessage);
                String result = getRequestRpc.call(messageToQueue);
                res.setStatus(HttpServletResponse.SC_OK);

//                {
//                    "numSkiers": 123
//                }
                res.getWriter().write(result);
                return;
            } catch (IOException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }



        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_CREATED);
            res.getWriter().write("{\n" +
                    "  \"message\": \"string\"\n" +
                    "}");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isPostUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\n" +
                    "  \"message\": \"new season created\"\n" +
                    "}");
            return;
        }
    }

    private boolean isGetUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons"
        // urlParts = [, 1, seasons]
        if (urlPath.length == 3 && "seasons".equals(urlPath[2])) {
            try {
                Integer.parseInt(urlPath[1]);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        // urlPath  = "/{resortID}/seasons/{seasonID}/day/{dayID}/skiers"
        // urlParts = [ , {resortID}, seasons, {seasonID}, day, {dayID}, skiers]
        if (urlPath.length == 7 && "seasons".equals(urlPath[2])
        && "day".equals(urlPath[4]) && "skiers".equals(urlPath[6])) {
            return true;
        }
        return false;
    }

    private boolean isPostUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons"
        // urlParts = [, 1, seasons]
        if (urlPath.length == 3 && "seasons".equals(urlPath[2])) {
            try {
                Integer.parseInt(urlPath[1]);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
