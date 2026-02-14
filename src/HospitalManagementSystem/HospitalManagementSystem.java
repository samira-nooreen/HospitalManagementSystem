import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.sql.*;
import java.util.Scanner;

public class HospitalManagementSystem {

    public static void main(String[] args) {
        // --- 1. START WEB SERVER FOR RENDER ---
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", (exchange) -> {
                String response = "Hospital Management System is Live!";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getOutputStream();
                os.write(response.getBytes());
                os.close();
            });
            server.start();
            System.out.println("Web monitoring server started on port " + port);
        } catch (Exception e) {
            System.err.println("Web server failed: " + e.getMessage());
        }

        // --- 2. DATABASE CONNECTION ---
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database Connected!");

            // Note: Ensure Patient and Doctor classes are also in the root folder without package names
            Patient patient = new Patient(connection, new Scanner(System.in));
            Doctor doctor = new Doctor(connection);
            
            // Render logs usually don't support interactive Scanner input well,
            // but keeping this here so the app doesn't close immediately.
            System.out.println("System initialized. Waiting for requests...");
            
            // Keep the main thread alive
            Thread.currentThread().join(); 

        } catch (Exception e) {
            System.err.println("Initialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
