package io.github.derec4.dragonforgekingdoms.database;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateDB {
    private static Connection connection;
//    private final String databasePath = "plugins/YourPluginName/database.db";

    public boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
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

    public void createLogTable() {
        try {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS log_data (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "timestamp TIMESTAMP," +
                            "message TEXT" +
                            ")"
            );
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }

    public void createKingdomTable() {
        try {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS kingdoms (" +
                            "ID INTEGER," +
                            "name TEXT," +
                            "description TEXT," +
                            "open INTEGER," +
                            "leader TEXT," +
                            "home_location_x DOUBLE," +
                            "home_location_y DOUBLE," +
                            "home_location_z DOUBLE," +
                            "home_location_world TEXT," +
                            "creationTime TEXT" +
                            ")"
            );
            Bukkit.getServer().getConsoleSender().sendMessage("Kingdoms Database Created!");
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Reconnect or create a new connection if it's closed or doesn't exist
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
        }
        return connection;
    }
}
