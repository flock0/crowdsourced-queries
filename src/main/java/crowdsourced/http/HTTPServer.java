package crowdsourced.http;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;



import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.IOException;

/** This class creates the webserver that will handle the requests of the user
 *  interface.
 * The communication with the rest of the application is done through the
 * QueryInterface interface.
 */
public class HTTPServer implements Runnable {

    private QueryInterface qif;
    private HttpServer server;
    private final static int SERVER_PORT = 8080;

    /* Set BUILTIN_RESOURCES to true to serve static files from the jar file
     * instead of looking for them in BASE_DIR. */
    private final static boolean BUILTIN_RESOURCES = false;
    private final static String BASE_DIR = "./src/main/resources";

    /** Create a new HTTP server.
     * @param _qif The QueryInterface used to answer requests.
     */
    public HTTPServer(QueryInterface _qif) {
        this.qif = _qif;
        this.server = HttpServer.createSimpleServer("/dev/null", SERVER_PORT);

        server.getServerConfiguration().addHttpHandler(new TimeHandler(), "/");
        server.getServerConfiguration().addHttpHandler(new TimeHandler(), "/time");

        /*
        if (BUILTIN_RESOURCES) {
            server.getServerConfiguration().addHttpHandler(
                    new CLStaticHttpHandler(HTTPServer.class.getClassLoader()),
                    "/");
        } else {
            server.getServerConfiguration().addHttpHandler(
                    new StaticHttpHandler(BASE_DIR), "/");
        }
        */
    }

    /** Starts the webserver. */
    public void run() {
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** HttpHandler for / */
    public class RootHandler extends HttpHandler {
        public void service(Request request, Response response) throws Exception {
            String date =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX").format(new Date());
            response.setContentType("text/plain");
            response.setContentLength(date.length());
            response.getWriter().write(date);
        }
    }

    /** Example handler that return a text page with current date and time */
    public class TimeHandler extends HttpHandler {
        public void service(Request request, Response response) throws Exception {
            String date =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX").format(new Date());
            response.setContentType("text/plain");
            response.setContentLength(date.length());
            response.getWriter().write(date);
        }
    }
}
