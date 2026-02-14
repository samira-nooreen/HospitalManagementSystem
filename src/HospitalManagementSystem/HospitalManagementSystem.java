package HospitalManagementSystem;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.sql.*;
import java.util.Scanner;

public class HospitalManagementSystem {

    public static void main(String[] args) {
        // 1. START WEB SERVER (Required for Render to stay "Live")
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", (exchange) -> {
                String response = "Hospital Management System is Running!";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getOutputStream();
                os.write(response.getBytes());
                os.close();
            });
            server.setExecutor(null);
            server.start();
            System.out.println("Web Server started on port " + port);
        } catch (Exception e) {
            System.out.println("Web Server failed: " + e.getMessage());
        }

        // 2. Load the Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
            return;
        }

        // 3. Database Credentials from Environment Variables
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Connected to Aiven MySQL successfully!");

            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);

            while (true) {
                System.out.println("--- HOSPITAL MANAGEMENT SYSTEM ---");
                System.out.println("1. Add Patient");
                System.out.println("2. View Patient");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");

                if (!scanner.hasNextInt()) {
                    if (!scanner.hasNext()) break; // Exit if no more input (common on cloud)
                    scanner.next(); 
                    continue;
                }
                
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1: patient.addPatient(); break;
                    case 2: patient.viewPatients(); break;
                    case 3: doctor.viewDoctor(); break;
                    case 4: bookAppointment(patient, doctor, connection, scanner); break;
                    case 5:
                        System.out.println("Thank you for using the system!");
                        return;
                    default:
                        System.out.println("Enter a valid choice!");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner) {
        System.out.println("Enter Patient Id:");
        int patientID = scanner.nextInt();
        System.out.println("Enter Doctor Id:");
        int doctorID = scanner.nextInt();
        System.out.println("Enter Appointment Date (yyyy-mm-dd):");
        String appointmentDate = scanner.next();

        if (patient.getPatientById(patientID) && doctor.getDoctorById(doctorID)) {
            if (checkDoctorAvailability(doctorID, appointmentDate, connection)) {
                String query = "INSERT INTO appointments (patient_id, doctor_id, appointment_date) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setInt(1, patientID);
                    pstmt.setInt(2, doctorID);
                    pstmt.setString(3, appointmentDate);
                    if (pstmt.executeUpdate() > 0) System.out.println("Appointment Booked!");
                } catch (SQLException e) { e.printStackTrace(); }
            } else {
                System.out.println("Doctor not available on this date.");
            }
        } else {
            System.out.println("Invalid IDs.");
        }
    }

    public static boolean checkDoctorAvailability(int doctorID, String date, Connection connection) {
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id=? AND appointment_date=?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, doctorID);
            pstmt.setString(2, date);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) == 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
