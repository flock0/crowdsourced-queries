package crowdsourced.http;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/** This class creates the webserver that will handle the requests of the user
 *  interface.
 * The communication with the rest of the application is done through the
 * QueryInterface interface.
 */
public class HTTPServer implements Runnable {

    private QueryInterface qif;
    private HttpServer server;
    private final static Logger LOGGER = Logger.getLogger("HttpServer");
    private final static int SERVER_BIND_PORT = 8080;
    private final static String SERVER_BIND_HOST = "localhost";

    /* Set BUILTIN_RESOURCES to true to serve static files from the jar file
     * instead of looking for them in BASE_DIR. */
    private final static boolean BUILTIN_RESOURCES = false;
    private final static String BASE_DIR = "./src/main/resources";

    /** Create a new HTTP server.
     * @param _qif The QueryInterface used to answer requests.
     */
    public HTTPServer(QueryInterface _qif) {
        this.qif = _qif;
        this.server = new HttpServer();
        NetworkListener nl = new NetworkListener(
                "http-listener", SERVER_BIND_HOST, SERVER_BIND_PORT);
        server.addListener(nl);

        ServerConfiguration conf = server.getServerConfiguration();
        conf.addHttpHandler(new RootHandler(), "/");
        conf.addHttpHandler(new TimeHandler(), "/time");
        conf.addHttpHandler(new QueryHandler(), "/query/*");

        if (BUILTIN_RESOURCES) {
            conf.addHttpHandler(
                    new CLStaticHttpHandler(HTTPServer.class.getClassLoader()),
                    "/*");
        } else {
            conf.addHttpHandler(new StaticHttpHandler(BASE_DIR), "/*");
        }
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
            response.sendRedirect("/index.html");
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

    /** Handle request to the application through the QueryInterface object.*/
    public class QueryHandler extends HttpHandler {
        public void service(Request request, Response response) throws Exception {
            String json = null;
            String path = request.getPathInfo();

            if (path.equals("/all")) {
                LOGGER.info("All queries infos requested");
                json = qif.queriesInfo();
            } else if (path.equals("/new")) {
                String queryString = request.getParameter("question");
                LOGGER.info("New query: " + queryString);
                json = qif.newQuery(queryString);
            } else if (path.equals("/abort")) {
                String queryId = request.getParameter("query");
                LOGGER.info("Abort query: " + queryId);
                json = qif.abortQuery(queryId);
            } else {
                LOGGER.info("Unknown QueryHandler request. PathInfo: " + path);
                response.setStatus(HttpStatus.NOT_FOUND_404);
                response.setContentType("text/html");
                response.getWriter().write("<h1>Not found</h1>");
            }

            if (json != null) {
                response.setContentType("application/json");
                response.setContentLength(json.length());
                response.getWriter().write(json);
            }
        }
    }
}
