import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SpiderTrapServer {
    private static final int PORT = 8000;
    private static final int DELAY = 350;

    // Configuration Section
    private static final int MIN_LINKS_PER_PAGE = 5;
    private static final int MAX_LINKS_PER_PAGE = 10;
    private static final int MIN_LENGTH_OF_LINKS = 3;
    private static final int MAX_LENGTH_OF_LINKS = 20;
    private static final String CHAR_SPACE = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-/";
    // End Configuration Section

    private static Map<String, Integer> pageCount = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new SpiderTrapHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started. Use Ctrl+C to stop.");

        // Sleep to keep the program running
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.stop(0);
    }

    static class SpiderTrapHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String pageContent = generatePage(path);

            // Send response with delay
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            exchange.sendResponseHeaders(200, pageContent.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(pageContent.getBytes());
            outputStream.close();
        }

        private String generatePage(String seed) {
            StringBuilder html = new StringBuilder("<html>\n<body>\n");
            Random random = new Random(seed.hashCode());

            int numLinks = random.nextInt(MAX_LINKS_PER_PAGE - MIN_LINKS_PER_PAGE + 1) + MIN_LINKS_PER_PAGE;
            for (int i = 0; i < numLinks; i++) {
                int linkLength = random.nextInt(MAX_LENGTH_OF_LINKS - MIN_LENGTH_OF_LINKS + 1) + MIN_LENGTH_OF_LINKS;
                StringBuilder link = new StringBuilder();
                for (int j = 0; j < linkLength; j++) {
                    link.append(CHAR_SPACE.charAt(random.nextInt(CHAR_SPACE.length())));
                }
                html.append("<a href=\"").append(link).append("\">").append(link).append("</a><br>\n");
            }

            html.append("</body>\n</html>");

            // Increment page count and check if the page has been opened 100 times
            int count = pageCount.getOrDefault(seed, 0) + 1;
            pageCount.put(seed, count);
            if (count == 100) {
                // Here, you can try to get the client IP and MAC address
                // But retrieving MAC addresses is platform-dependent and might require external libraries or specific code.
                System.out.println("Page with seed '" + seed + "' opened 100 times.");
            }

            return html.toString();
        }
    }
}
