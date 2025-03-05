import java.sql.*;
import java.util.Scanner;

public class FeedbackSystemWithoutHash {
    static Connection conn;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            connect();
            // createTables();
            while (true) {
                System.out.println("\n1. Register\n2. Login\n3. Exit");
                System.out.print("Choose: ");
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 1) register();
                else if (choice == 2) login();
                else break;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void connect() throws SQLException {
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/feedback_db", 
            "postgres", 
            "postgre123"
        );
    }

    // static void createTables() throws Exception {
    //     Statement stmt = conn.createStatement();
    //     stmt.execute("CREATE TABLE IF NOT EXISTS users ("
    //                   + "id SERIAL PRIMARY KEY, "
    //                   + "username VARCHAR(255) UNIQUE NOT NULL, "
    //                   + "email VARCHAR(255), "
    //                   + "password VARCHAR(255), "
    //                   + "role VARCHAR(50) DEFAULT 'user')");

    //     stmt.execute("CREATE TABLE IF NOT EXISTS feedback ("
    //                   + "id SERIAL PRIMARY KEY, "
    //                   + "user_id INTEGER REFERENCES users(id), "
    //                   + "feedback TEXT)");

    //     ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username = 'admin'");
    //     if (!rs.next()) {
    //         String adminPass = PasswordUtils.hashPassword("admin123");
    //         PreparedStatement ps = conn.prepareStatement(
    //             "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, 'admin');"
    //         );
    //         ps.setString(1, "admin");
    //         ps.setString(2, "admin@example.com");
    //         ps.setString(3, adminPass);
    //         ps.executeUpdate();
    //         System.out.println("Default admin created with password 'admin123'");
    //     }
    // }


    static void register() {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            // String hashedPassword = PasswordUtils.hashPassword(password);

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES (?, ?, ?);"
            );
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);

            ps.executeUpdate();
            System.out.println("Registration successful!");
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                System.out.println("Username already exists.");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void login() {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            // String hashedPassword = PasswordUtils.hashPassword(password);

            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password = ?"
            );
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String role = rs.getString("user_role");
                int userId = rs.getInt("id");
                System.out.println("Login successful! Welcome, " + username);

                if ("admin".equals(role)) {
                    adminMenu();
                } else {
                    userMenu(userId);
                }
            } else {
                System.out.println("Invalid credentials.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void adminMenu() throws SQLException {
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. View Feedbacks");
            System.out.println("2. Delete Feedback");
            System.out.println("3. Logout");
            System.out.print("Choose: ");
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 1) viewFeedback();
            else if (choice == 2) deleteFeedback();
            else break;
        }
    }

    static void userMenu(int userId) throws SQLException {
        while (true) {
            System.out.println("\nUser Menu:");
            System.out.println("1. Add Feedback");
            System.out.println("2. Update Feedback");
            System.out.println("3. View Feedback");
            System.out.println("4. Logout");
            System.out.print("Choose: ");
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 1) addFeedback(userId);
            else if (choice == 2) updateFeedback(userId);
            else if (choice == 3) viewFeedback();
            else break;
        }
    }

    static void addFeedback(int userId) throws SQLException {
        System.out.print("Enter feedback: ");
        String feedback = scanner.nextLine();
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO feedback (user_id, feedback_text) VALUES (?, ?)"
        );
        ps.setInt(1, userId);
        ps.setString(2, feedback);
        ps.executeUpdate();
        System.out.println("Feedback added.");
    }


    static void updateFeedback(int userId) throws SQLException {
        System.out.print("Enter feedback ID to update: ");
        int feedbackId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter new feedback: ");
        String feedback = scanner.nextLine();
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE feedback SET feedback_text = ? WHERE id = ? AND user_id = ?"
        );
        ps.setString(1, feedback);
        ps.setInt(2, feedbackId);
        ps.setInt(3, userId);
        int rows = ps.executeUpdate();
        if (rows > 0) System.out.println("Feedback updated.");
        else System.out.println("Feedback not found or not yours.");
    }

    static void deleteFeedback() throws SQLException {
        System.out.print("Enter feedback ID to delete: ");
        int feedbackId = Integer.parseInt(scanner.nextLine());
        PreparedStatement ps = conn.prepareStatement(
            "DELETE FROM feedback WHERE id = ?"
        );
        ps.setInt(1, feedbackId);
        int rows = ps.executeUpdate();
        if (rows > 0) System.out.println("Feedback deleted.");
        else System.out.println("Feedback not found.");
    }


    static void viewFeedback() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT f.id, u.username, f.feedback_text" 
                                            + " FROM feedback f"
                                            + " JOIN users u ON f.user_id = u.id");
        System.out.println("\nFeedbacks:");
        while (rs.next()) {
            int id = rs.getInt("id");
            String username = rs.getString("username");
            String feedback = rs.getString("feedback_text");
            System.out.println( id + " | " + username + ": " + feedback) ;}}
}
