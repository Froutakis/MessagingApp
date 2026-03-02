import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:nexus.db";
    

    public DatabaseManager() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            
            if (conn != null) {
                createTables(conn);
                System.out.println("--- Loading Database: nexus.db ---");
                printAccounts(conn);
            }

        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private void createTables(Connection conn) throws SQLException {
        String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts (" +
                "username TEXT PRIMARY KEY," +
                "authToken INTEGER UNIQUE NOT NULL" +
                ");";

        String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "sender TEXT NOT NULL," +
                "recipient TEXT NOT NULL," +
                "body TEXT NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "isRead BOOLEAN DEFAULT 0," +
                "FOREIGN KEY(sender) REFERENCES accounts(username)," +
                "FOREIGN KEY(recipient) REFERENCES accounts(username)" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createAccountsTable);
            stmt.execute(createMessagesTable);
        }
    }

    

    public int createAccount(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (isUsernameTaken(username)) {
                return -1; 
            }

            int authToken = (int) (Math.random() * 1000000);
            String insert = "INSERT INTO accounts(username, authToken) VALUES(?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insert)) {
                pstmt.setString(1, username);
                pstmt.setInt(2, authToken);
                pstmt.executeUpdate();
                return authToken;
            }
        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
            return -1;
        }
    }
 
    public boolean isUsernameTaken(String username) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        String query = "SELECT username FROM accounts WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }


    public void printAccounts(Connection conn) throws SQLException {
    String query = "SELECT username FROM accounts";
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        
        System.out.println("--- Database Initialization ---");
        boolean found = false;
        while (rs.next()) {
            System.out.println("Existing User: " + rs.getString("username"));
            found = true;
        }
        if (!found) {
            System.out.println("Database is currently empty.");
        }
        System.out.println("-------------------------------");
    }
}
    public java.util.Map<String, Integer> getAllAccountsRaw() throws SQLException {
    java.util.Map<String, Integer> data = new java.util.HashMap<>();
    String query = "SELECT username, authToken FROM accounts";
    try (Connection conn = DriverManager.getConnection(DB_URL);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        while (rs.next()) {
            data.put(rs.getString("username"), rs.getInt("authToken"));
        }
    }
    return data;
}

    public int createMessage(String sender, String recipient, String body) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String insert = "INSERT INTO messages(sender, recipient, body) VALUES(?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, sender);
                pstmt.setString(2, recipient);
                pstmt.setString(3, body);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); 
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating message: " + e.getMessage());
        }
        return -1;
    }

    public List<Message> getMessagesForUser(String username) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT id, sender, recipient, body, timestamp, isRead FROM messages WHERE recipient = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Message msg = new Message(
                        rs.getInt("id"),
                        rs.getString("sender"),
                        rs.getString("recipient"),
                        rs.getString("body")
                );
                messages.add(msg);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving messages: " + e.getMessage());
        }
        return messages;
    }
    public void deleteMessage(int messageId) {
        String query = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, messageId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting message: " + e.getMessage());
        }
    }

    public void markMessageAsRead(int messageId) {
        String query = "UPDATE messages SET isRead = 1 WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, messageId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error marking message as read: " + e.getMessage());
        }
    }


}

    

