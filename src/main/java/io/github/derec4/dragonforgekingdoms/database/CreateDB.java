import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateSQLiteDatabase {
    public static void main(String[] args) {
        // Define the SQLite database URL (e.g., a file path)
        String url = "jdbc:sqlite:/path/to/your/database.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Connected to the database");

                // If the database does not exist, it will be created

            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
