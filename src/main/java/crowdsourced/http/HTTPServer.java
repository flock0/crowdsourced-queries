package crowdsourced.http;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

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

    /** Create a new HTTP server.
     * @param _qif The QueryInterface used to answer requests.
     */
    public HTTPServer(QueryInterface _qif) {
        this.qif = _qif;
        this.server = HttpServer.createSimpleServer("/dev/null", SERVER_PORT);

        server.getServerConfiguration().addHttpHandler(new RootHandler(), "/");
    }

    /** Starts the webserver. */
    public void run() {
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** The HttpHandler for / */
    public class RootHandler extends HttpHandler {
        public void service(Request request, Response response) throws Exception {
            String date =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX").format(new Date());
            response.setContentType("text/plain");
            response.setContentLength(date.length());
            response.getWriter().write(date);
        }
    }
}
