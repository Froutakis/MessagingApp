import java.sql.*;

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

}

    

