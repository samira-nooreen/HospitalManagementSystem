package HospitalManagementSystem;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.sql.*;
import java.util.Scanner;

public class HospitalManagementSystem {

    public static void main(String[] args) {
        // 1. Get DB Credentials from Environment Variables
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || username == null) {
            System.err.println("ERROR: Missing DB environment variables!");
            return;
        }

        // 2. Start Web Server
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Create a Connection to use inside the web handlers
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database Connected!");

            // ROUTE: Home Page
            server.createContext("/", (exchange) -> {
                String response = "<h1>Hospital System Live</h1>" +
                                 "<ul><li><a href='/patients'>View Patients</a></li>" +
                                 "<li><a href='/doctors'>View Doctors</a></li></ul>";
                sendResponse(exchange, response);
            });

            // ROUTE: View Patients
            server.createContext("/patients", (exchange) -> {
                String response = getPatientsData(connection);
                sendResponse(exchange, response);
            });

            // ROUTE: View Doctors
            server.createContext("/doctors", (exchange) -> {
                String response = getDoctorsData(connection);
                sendResponse(exchange, response);
            });

            server.start();
            System.out.println("Web server started on port " + port);

            // Keep alive
            Thread.currentThread().join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper to send HTML to the browser
    private static void sendResponse(HttpExchange exchange, String response) throws Exception {
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static String getPatientsData(Connection conn) {
        StringBuilder html = new StringBuilder("<h2>Patient List</h2><table border='1'><tr><th>ID</th><th>Name</th><th>Age</th></tr>");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM patients")) {
            while (rs.next()) {
                html.append("<tr><td>").append(rs.getInt("id"))
                    .append("</td><td>").append(rs.getString("name"))
                    .append("</td><td>").append(rs.getInt("age")).append("</td></tr>");
            }
            html.append("</table><br><a href='/'>Back</a>");
        } catch (SQLException e) { return "Error: " + e.getMessage(); }
        return html.toString();
    }

    private static String getDoctorsData(Connection conn) {
        StringBuilder html = new StringBuilder("<h2>Doctor List</h2><table border='1'><tr><th>ID</th><th>Name</th><th>Specialization</th></tr>");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM doctors")) {
            while (rs.next()) {
                html.append("<tr><td>").append(rs.getInt("id"))
                    .append("</td><td>").append(rs.getString("name"))
                    .append("</td><td>").append(rs.getString("specialization")).append("</td></tr>");
            }
            html.append("</table><br><a href='/'>Back</a>");
        } catch (SQLException e) { return "Error: " + e.getMessage(); }
        return html.toString();
    }
}
