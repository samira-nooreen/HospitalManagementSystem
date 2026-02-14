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
            
            // Establish Connection
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database Connected Successfully!");

            // Home Page Route
            server.createContext("/", (exchange) -> {
                try {
                    String response = "<h1>Hospital System Live</h1>" +
                                     "<ul><li><a href='/patients'>View Patients</a></li>" +
                                     "<li><a href='/doctors'>View Doctors</a></li></ul>";
                    sendResponse(exchange, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // View Patients Route
            server.createContext("/patients", (exchange) -> {
                try {
                    String response = getTableData(connection, "SELECT * FROM patients", "Patient List");
                    sendResponse(exchange, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // View Doctors Route
            server.createContext("/doctors", (exchange) -> {
                try {
                    String response = getTableData(connection, "SELECT * FROM doctors", "Doctor List");
                    sendResponse(exchange, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            server.start();
            System.out.println("Web server started on port " + port);
            
            // Keep the main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Failed to start system: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper to send HTML back to the browser
    private static void sendResponse(HttpExchange exchange, String response) throws Exception {
        byte[] bytes = response.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String getTableData(Connection conn, String query, String title) {
        StringBuilder html = new StringBuilder("<h2>" + title + "</h2><table border='1'>");
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Headers
            html.append("<tr>");
            for (int i = 1; i <= columnCount; i++) {
                html.append("<th>").append(metaData.getColumnName(i)).append("</th>");
            }
            html.append("</tr>");

            // Data
            while (rs.next()) {
                html.append("<tr>");
                for (int i = 1; i <= columnCount; i++) {
                    html.append("<td>").append(rs.getString(i)).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</table><br><a href='/'>Back Home</a>");
        } catch (SQLException e) { 
            return "Error fetching data: " + e.getMessage(); 
        }
        return html.toString();
    }
}
