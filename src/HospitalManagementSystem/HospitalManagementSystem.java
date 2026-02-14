package HospitalManagementSystem;

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
                // FIX: Changed getOutputStream() to getResponseBody()
                OutputStream os = exchange.getResponseBody(); 
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

        if (url == null || username == null) {
            System.err.println("ERROR: Missing DB environment variables (DB_URL, DB_USER, DB_PASSWORD)");
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database Connected!");

            // Note: Scanner will not be interactive on Render logs, 
            // but we keep it to prevent immediate exit.
            Patient patient = new Patient(connection, new Scanner(System.in));
            Doctor doctor = new Doctor(connection);
            
            System.out.println("System initialized. Waiting for connections...");
            
            // Keep the main thread alive so the container doesn't stop
            Thread.currentThread().join(); 

        } catch (Exception e) {
            System.err.println("Initialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
