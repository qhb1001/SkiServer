import com.google.gson.Gson;
import com.rabbitmq.client.*;
import consumer.resortmicroservice.model.SkiDayVerticalMessage;
import consumer.resortmicroservice.model.TotalVerticalMessage;
import model.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import util.RabbitMQChannelPool;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

    private ConnectionFactory factory;
    public static final String liftRideExchange = "liftRideExchange";
    private Connection connection;
    private ObjectPool<Channel> channelPool;

    @Override
    public void init() {
        try {
            factory = new ConnectionFactory();
//            factory.setHost("localhost");
//            factory.setUri("amqp://bo:passwordforrabbitmq@54.208.30.94:5672/vhost");
            factory.setUri(System.getProperty("RABBITMQ_URI"));
            connection = factory.newConnection();

            channelPool = new GenericObjectPool<Channel>(new RabbitMQChannelPool(factory, connection));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//        res.setContentType("text/plain");
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo().replaceAll("\\r\\n|\\r|\\n", "");
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing paramterers");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isGetUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            if (urlParts.length == 8) {
                try (RPCClient getRequestRpc = new RPCClient(connection, channelPool.borrowObject())) {

                    Gson gson = new Gson();
                    // urlParts = [ , {resortID}, seasons, {seasonID}, days, {dayID}, skiers, {skierID}]
                    SkiDayVerticalMessage uniqueSkierMessage = new SkiDayVerticalMessage(
                            "SkiDayVertical",
                            Integer.valueOf(urlParts[1]),
                            Integer.valueOf(urlParts[3]),
                            Integer.valueOf(urlParts[5]),
                            Integer.valueOf(urlParts[7])
                    );
                    String messageToQueue = gson.toJson(uniqueSkierMessage);
                    String result = getRequestRpc.call(messageToQueue);
                    res.setStatus(HttpServletResponse.SC_OK);

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
            } else if (urlParts.length == 3) {
                try (RPCClient getRequestRpc = new RPCClient(connection, channelPool.borrowObject())) {

                    Gson gson = new Gson();
                    // urlParts = urlParts = [, 1, vertical]
                    Integer resort = Integer.parseInt(req.getParameter("resort"));
                    Integer season = req.getParameter("season")!= null ? Integer.parseInt(req.getParameter("season")) : -1;
                    TotalVerticalMessage totalVerticalMessage = new TotalVerticalMessage(
                            "TotalVertical",
                            Integer.valueOf(urlParts[1]),
                            resort,
                            season
                    );
                    String messageToQueue = gson.toJson(totalVerticalMessage);
                    String result = getRequestRpc.call(messageToQueue);
                    res.setStatus(HttpServletResponse.SC_OK);

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
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || "".equals(urlPath)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("missing paramterers");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isPostUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else {
            // do any sophisticated processing with urlParts which contains all the url params
            if (urlParts.length == 8) {
                Channel channel = null;
                try {
                    channel = channelPool.borrowObject();
                    channel.exchangeDeclare(liftRideExchange, "fanout");
                    String message = formatLiftRideJson(urlPath, req.getReader().lines().collect(Collectors.joining()));

                    channel.basicPublish(liftRideExchange, "",
                            null,
                            message.getBytes("UTF-8"));

                    res.setStatus(HttpServletResponse.SC_CREATED);
                    res.getWriter().write("{\n" +
                            "  \"message\": \"pushed a message to the rabbitmq\"\n" +
                            "}");
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot create a channel to rabbitmq");
                } catch (IOException e) {
                    e.printStackTrace();
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot publish message to the queue");
                } catch (Exception e) {
                    e.printStackTrace();
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to borrow a channel from pool");
                } finally {
                    if (channel != null) {
                        try {
                            channelPool.returnObject(channel);
//                            System.out.println("channel returned to the pool.");
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new ServletException("Unable to return a borrowed channel to the pool");
                        }
                    }
                }

            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "Information Not Found");
            }
        }
    }

    private boolean isPostUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if (urlPath.length == 8 && "seasons".equals(urlPath[2]) && "days".equals(urlPath[4]) && "skiers".equals(urlPath[6])) {
            try {
                Integer.parseInt(urlPath[1]);
                Integer.parseInt(urlPath[7]);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        // urlPath  = "/1/vertical"
        // urlParts = [, 1, vertical]
        else if (urlPath.length == 3 && "vertical".equals(urlPath[2])) {
            try {
                Integer.parseInt(urlPath[1]);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isGetUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}"
        // urlParts = [ , {resortID}, seasons, {seasonID}, days, {dayID}, skiers, {skierID}]
        if (urlPath.length == 8 && "seasons".equals(urlPath[2]) && "days".equals(urlPath[4]) && "skiers".equals(urlPath[6])) {
            try {
                Integer.valueOf(urlPath[1]);
                Integer.valueOf(urlPath[3]);
                Integer.valueOf(urlPath[5]);
                Integer.valueOf(urlPath[7]);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        // urlPath  = "/1/vertical"
        // urlParts = [, 1, vertical]
        else if (urlPath.length == 3 && "vertical".equals(urlPath[2])) {
            try {
                Integer.parseInt(urlPath[1]);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    private String formatLiftRideJson(String urlPath, String payload) {
//        System.out.println(urlPath);
//        System.out.println(payload);
        String[] urlParts = urlPath.split("/");
        Gson gson = new Gson();
        LiftRidePayload liftRidePayload = gson.fromJson(payload, LiftRidePayload.class);
        LiftRideMessage liftRideMessage = new LiftRideMessage(Integer.parseInt(urlParts[1]), Integer.parseInt(urlParts[3]), Integer.parseInt(urlParts[5]),
                Integer.parseInt(urlParts[7]), liftRidePayload.getTime(), liftRidePayload.getLiftID());

        return gson.toJson(liftRideMessage);
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            if (connection != null) {connection.close();}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
