package io.github.derec4.dragonforgekingdoms.database;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateDB {
    private static Connection connection;
//    private final String databasePath = "plugins/YourPluginName/database.db";

    /**
     * Gets the database connection. If the connection is null or closed, a new connection is created.
     *
     * @return the database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/DragonForge_Kingdoms/kingdoms.db");
        }
        return connection;
    }

    public boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/DragonForge_Kingdoms/kingdoms.db");
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }

    public void createPlayerTable() {
        try {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS players (" +
                            "id TEXT PRIMARY KEY," +
                            "kingdom TEXT" +
                            ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createLogTable() {
        try {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS log_data (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "timestamp TIMESTAMP," +
                            "message TEXT" +
                            ")"
            );
            Bukkit.getServer().getConsoleSender().sendMessage("Player Database Code Reached!");
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }

    public void createChunkTable() {
        try {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS chunks (" +
                            "chunk_owner TEXT," +
                            "chunk_x DOUBLE," +
                            "chunk_z DOUBLE," +
                            "world_id TEXT," +
                            "PRIMARY KEY (chunk_x, chunk_z, world_id)" + // Composite primary key
                            ")"
            );
            Bukkit.getServer().getConsoleSender().sendMessage("Chunk Database Code Reached!");
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }

    public void createKingdomTable() {
        try {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS kingdoms (" +
                            "ID TEXT PRIMARY KEY," +
                            "name TEXT," +
                            "description TEXT," +
                            "open BOOLEAN," +
                            "creationTime TEXT," +
                            "leader TEXT," +
                            "level INT," +
                            "claimedChunks INT," +
                            "home_world_id TEXT," +
                            "home_x INT," +
                            "home_y INT," +
                            "home_z INT," +
                            "health INT" +
                            ")"
            );
            Bukkit.getServer().getConsoleSender().sendMessage("Kingdoms Database Code Reached!");
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }

    public void createInvitesTable() {
        try {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS invites (" +
                    "player TEXT," +
                    "sender TEXT," +
                    "kingdom TEXT)");
            Bukkit.getServer().getConsoleSender().sendMessage("Invites Database Code Reached!");
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }
}
