package crowdsourced;

import crowdsourced.http.HTTPServer;
import crowdsourced.http.QueryInterface;
import java.io.IOException;
//import queryExecutor.QueryPool;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

/**
 * Main class lauched when running the application.
 *
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello");
        //QueryPool qp = new QueryPool();
        //HTTPServer httpd = new HTTPServer(qp);
        HTTPServer httpd = new HTTPServer(new DummyQueryInterface());
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
    static class DummyQueryInterface implements QueryInterface {

        /** Get a resource against Main.class.
         * @param name The resource name
         * @return The corresponding InputStream.
         */
        private InputStream getResourceAsStream(String name) {
            InputStream is = Main.class.getClassLoader().getResourceAsStream(name);
            if (is == null) {
                try {
                    throw new RuntimeException("Couldn't find resource: " + name);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            return is;
        }

        @Override
        public String queriesInfo() {
            String s;
            try {
                s = IOUtils.toString(getResourceAsStream("result.json"), "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
                s = "{ status = \"error\", message = \"IOException\" }";
            }
            return s;
        }

        @Override
        public String newQuery(String question) {
            return "{ status = \"ok\"}";
        }

        @Override
        public String abortQuery(String queryId) {
            return "{ status = \"error\", message: \"Not implemented\"}";
        }
    }
}
