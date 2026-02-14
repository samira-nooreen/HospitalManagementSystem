package HospitalManagementSystem;

import java.sql.*;
import java.util.Scanner;

public class HospitalManagementSystem {

    public static void main(String[] args) {
        // 1. Load the Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
            return;
        }

        // 2. Get credentials from Environment Variables (Render/Aiven)
        // These will be null if running locally without setup, so we add fallback values
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        Scanner scanner = new Scanner(System.in);

        try {
            // 3. Establish Connection
            Connection connection = DriverManager.getConnection(url, username, password);
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
                    scanner.next(); // clear invalid input
                    continue;
                }
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        patient.addPatient();
                        break;
                    case 2:
                        patient.viewPatients();
                        break;
                    case 3:
                        doctor.viewDoctor();
                        break;
                    case 4:
                        bookAppointment(patient, doctor, connection, scanner);
                        break;
                    case 5:
                        System.out.println("Thank you for using Hospital Management System!");
                        connection.close();
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

    // Your bookAppointment and checkDoctorAvailability methods remain the same below...
    public static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner) {
        System.out.println("Enter Patient Id:");
        int patientID = scanner.nextInt();

        System.out.println("Enter Doctor Id:");
        int doctorID = scanner.nextInt();

        System.out.println("Enter Appointment Date (yyyy-mm-dd):");
        String appointmentDate = scanner.next();

        if (patient.getPatientById(patientID) && doctor.getDoctorById(doctorID)) {
            if (checkDoctorAvailability(doctorID, appointmentDate, connection)) {
                String appointmentQuery = "INSERT INTO appointments (patient_id, doctor_id, appointment_date) VALUES (?, ?, ?)";
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(appointmentQuery);
                    preparedStatement.setInt(1, patientID);
                    preparedStatement.setInt(2, doctorID);
                    preparedStatement.setString(3, appointmentDate);

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Appointment Booked Successfully");
                    } else {
                        System.out.println("Failed to Book Appointment");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Doctor is not available for this Date");
            }
        } else {
            System.out.println("Either Doctor ID or Patient ID is invalid");
        }
    }

    public static boolean checkDoctorAvailability(int doctorID, String appointmentDate, Connection connection) {
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id=? AND appointment_date=?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, doctorID);
            preparedStatement.setString(2, appointmentDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
