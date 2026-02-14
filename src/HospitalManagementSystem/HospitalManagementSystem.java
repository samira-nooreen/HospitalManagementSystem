package HospitalManagementSystem;

import java.sql.*;
import java.util.Scanner;

public class HospitalManagementSystem {

    // Example of how to use these in your Java code
String url = System.getenv("DB_URL");
String user = System.getenv("DB_USER");
String password = System.getenv("DB_PASSWORD");
Connection conn = DriverManager.getConnection(url, user, password);

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);

            while (true) {
                System.out.println("HOSPITAL MANAGEMENT SYSTEM");
                System.out.println("1. Add Patient");
                System.out.println("2. View Patient");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. Exit");
                System.out.println("Enter your choice:");

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        patient.addPatient();
                        System.out.println();
                        break;

                    case 2:
                        patient.viewPatients();
                        System.out.println();
                        break;
                    case 3:
                        doctor.viewDoctor();
                        System.out.println();
                        break;

                    case 4:
                        bookAppointment(patient, doctor, connection, scanner);
                        System.out.println();
                        break;
                    case 5:
                        System.out.println("Thank you for using HospitalManagementSystem!!");
                        return;

                    default:
                        System.out.println("Enter a valid choice!!");
                        break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bookAppointment(Patient patient, Doctor doctor,
                                       Connection connection, Scanner scanner) {

        System.out.println("Enter Patient Id:");
        int patientID = scanner.nextInt();

        System.out.println("Enter Doctor Id:");
        int doctorID = scanner.nextInt();

        System.out.println("Enter Appointment Date (yyyy-mm-dd):");
        String appointmentDate = scanner.next();

        if (patient.getPatientById(patientID) && doctor.getDoctorById(doctorID)) {

            if (checkDoctorAvailability(doctorID, appointmentDate, connection)) {

                String appointmentQuery =
                        "INSERT INTO appointments (patient_id, doctor_id, appointment_date) VALUES (?, ?, ?)";

                try {
                    PreparedStatement preparedStatement =
                            connection.prepareStatement(appointmentQuery);

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

    public static boolean checkDoctorAvailability(int doctorID,
                                                  String appointmentDate,
                                                  Connection connection) {

        String query =
                "SELECT COUNT(*) FROM appointments WHERE doctor_id=? AND appointment_date=?";

        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(query);

            preparedStatement.setInt(1, doctorID);
            preparedStatement.setString(2, appointmentDate);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count == 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
