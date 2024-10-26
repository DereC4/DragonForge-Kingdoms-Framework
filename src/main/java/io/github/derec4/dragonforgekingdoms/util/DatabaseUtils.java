package io.github.derec4.dragonforgekingdoms.util;

import io.github.derec4.dragonforgekingdoms.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.DragonForgeKingdoms;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseUtils {
    public static void addInviteToDatabase(UUID playerUUID, UUID senderUUID, UUID kingdomUUID) {
        Bukkit.getScheduler().runTaskAsynchronously(DragonForgeKingdoms.getInstance(), () -> {
            CreateDB databaseManager = new CreateDB();
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO invites (player_id, sender_id, kingdom_id) VALUES (?, ?, ?)")) {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, senderUUID.toString());
                statement.setString(3, kingdomUUID.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void removePlayerFromDatabase(Connection connection, UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM players WHERE id = ?")) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeKingdomFromDatabase(Connection connection, UUID kingdomUUID) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM kingdoms WHERE ID = ?")) {
            statement.setString(1, kingdomUUID.toString());
            statement.executeUpdate();
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Kingdom " + kingdomUUID + " has been" +
                    " removed");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveTerritoryToDatabase(Connection connection, ChunkCoordinate chunkCoord, UUID ID) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO chunks (chunk_owner, chunk_x, chunk_z, world_id)" +
                        "VALUES (?, ?, ?, ?)")) {
            statement.setString(1, ID.toString());
            statement.setInt(2, chunkCoord.getX());
            statement.setInt(3, chunkCoord.getZ());
            statement.setString(4, chunkCoord.getWorldID().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void removeTerritoryFromDatabase(Connection connection, ChunkCoordinate chunkCoord) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM chunks WHERE chunk_x = ? AND chunk_z = ? AND world_id = ?")) {
            statement.setDouble(1, chunkCoord.getX());
            statement.setDouble(2, chunkCoord.getZ());
            statement.setString(3, chunkCoord.getWorldID().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
