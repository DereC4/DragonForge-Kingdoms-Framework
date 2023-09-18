package io.github.derec4.dragonforgekingdoms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAccess {
    private Connection connection;

    public DatabaseAccess(String url, String username, String password) throws SQLException {
        // Establish the database connection when the class is instantiated
        this.connection = DriverManager.getConnection(url, username, password);
    }
}
