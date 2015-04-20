package crowdsourced;

import crowdsourced.http.HTTPServer;
import crowdsourced.http.QueryInterface;
import java.io.IOException;
import queryExecutor.QueryPool;

/**
 * Main class lauched when running the application.
 *
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello");
        QueryPool qp = new QueryPool();
        HTTPServer httpd = new HTTPServer(qp);
        httpd.run();

        System.out.println("Press any key to exit.");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /** A dummy QueryInterface that provides empty answers. */
    /*static class DummyQueryInterface implements QueryInterface {

        @Override
        public String queriesInfo() {
            return "";
        }

        @Override
        public String newQuery(String question) {
            return "";
        }

        @Override
        public void abortQuery(String queryId) {
            // Intentionally left empty
        }
    }*/
}
