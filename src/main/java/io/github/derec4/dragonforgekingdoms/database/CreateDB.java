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
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT," +
                            "description TEXT," +
                            "open INTEGER," +
                            "creationTime TEXT," +
                            "leader TEXT," +
                            "home TEXT" +
                            ")"
            );
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }
}
