import com.google.gson.Gson;
import com.rabbitmq.client.*;
import model.LiftRideMessage;
import model.LiftRidePayload;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

    private ConnectionFactory factory;
    private String newLiftRideQueueName = "newLiftRideQueue";
    private Connection connection;

    @Override
    public void init() {
        try {
            factory = new ConnectionFactory();
//            factory.setHost("localhost");
            factory.setUri("amqp://bo:passwordforrabbitmq@ec2-54-209-69-199.compute-1.amazonaws.com:5672/vhost");
            connection = factory.newConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//        res.setContentType("text/plain");
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing paramterers");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            if (urlParts.length == 8) {
                res.getWriter().write("34507");
                return;
            } else if (urlParts.length == 3) {
                res.getWriter().write("{\n" +
                        "  \"resorts\": [\n" +
                        "    {\n" +
                        "      \"seasonID\": \"string\",\n" +
                        "      \"totalVert\": 0\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}");
                return;
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

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else {
            // do any sophisticated processing with urlParts which contains all the url params
            if (urlParts.length == 8) {
                try (Channel channel = connection.createChannel()) {
                    channel.queueDeclare(newLiftRideQueueName, false, false, false, null);
                    String message = formatLiftRideJson(urlPath, req.getReader().lines().collect(Collectors.joining()));

                    channel.basicPublish("", newLiftRideQueueName,
                            MessageProperties.PERSISTENT_TEXT_PLAIN,
                            message.getBytes("UTF-8"));

                    res.setStatus(HttpServletResponse.SC_CREATED);
                    res.getWriter().write("{\n" +
                            "  \"message\": \"pushed a message to the rabbitmq\"\n" +
                            "}");
                    return;
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot create a channel to rabbitmq");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot publish message to the queue");
                    return;
                }

            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "Information Not Found");
                return;
            }
        }
    }

    private boolean isUrlValid(String[] urlPath) {
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

    private String formatLiftRideJson(String urlPath, String payload) {
        System.out.println(urlPath);
        System.out.println(payload);
        String[] urlParts = urlPath.split("/");
        Gson gson = new Gson();
        LiftRidePayload liftRidePayload = gson.fromJson(payload, LiftRidePayload.class);
        LiftRideMessage liftRideMessage = new LiftRideMessage(Integer.parseInt(urlParts[1]), urlParts[3], urlParts[5],
                Integer.parseInt(urlParts[7]), liftRidePayload.getTime(), liftRidePayload.getLiftID());

        return gson.toJson(liftRideMessage);
    }

    @Override
    public void destroy() {
        super.destroy();
//        try {
//            connection.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
