package HospitalManagementSystem;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.sql.*;

public class HospitalManagementSystem {

    public static void main(String[] args) {
        // 1. Load Environment Variables for Database
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || username == null) {
            System.err.println("ERROR: Missing DB environment variables!");
            return;
        }

        // 2. Start the Web Server
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            Connection connection = DriverManager.getConnection(url, username, password);

            // Home Page Route
            server.createContext("/", (exchange) -> {
                String response = "<h1>Hospital System Live</h1>" +
                                 "<ul><li><a href='/patients'>View Patients</a></li>" +
                                 "<li><a href='/doctors'>View Doctors</a></li></ul>";
                sendResponse(exchange, response);
            });

            // View Patients Route
            server.createContext("/patients", (exchange) -> {
                String response = getTableData(connection, "SELECT * FROM patients", "Patient List");
                sendResponse(exchange, response);
            });

            // View Doctors Route
            server.createContext("/doctors", (exchange) -> {
                String response = getTableData(connection, "SELECT * FROM doctors", "Doctor List");
                sendResponse(exchange, response);
            });

            server.start();
            System.out.println("Web server started on port " + port);
            Thread.currentThread().join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper to send HTML back to the browser
    private static void sendResponse(HttpExchange exchange, String response) throws Exception {
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody(); // Corrected method name
        os.write(response.getBytes());
        os.close();
    }

    private static String getTableData(Connection conn, String query, String title) {
        StringBuilder html = new StringBuilder("<h2>" + title + "</h2><table border='1'>");
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            html.append("<tr>");
            for (int i = 1; i <= columnCount; i++) html.append("<th>").append(metaData.getColumnName(i)).append("</th>");
            html.append("</tr>");

            while (rs.next()) {
                html.append("<tr>");
                for (int i = 1; i <= columnCount; i++) html.append("<td>").append(rs.getString(i)).append("</td>");
                html.append("</tr>");
            }
            html.append("</table><br><a href='/'>Back Home</a>");
        } catch (SQLException e) { return "Error fetching data: " + e.getMessage(); }
        return html.toString();
    }
}
