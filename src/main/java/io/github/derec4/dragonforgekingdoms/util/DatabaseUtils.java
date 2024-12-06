package io.github.derec4.dragonforgekingdoms.util;

import io.github.derec4.dragonforgekingdoms.territory.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.DragonForgeKingdoms;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
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

    public static void savePlayerMapping(Connection connection, UUID playerUUID, UUID kingdomUUID) throws SQLException {
        try (PreparedStatement statment = connection.prepareStatement(
                "INSERT OR REPLACE INTO players (id, kingdom) VALUES (?, ?)")) {
//            "CREATE TABLE IF NOT EXISTS players (" +
//                    "id TEXT," +
//                    "kingdom TEXT" +
//                    ")"
            statment.setString(1, playerUUID.toString());
            statment.setString(2, kingdomUUID.toString());
            statment.executeUpdate();
        }
    }

    public static void saveKingdom(Connection connection, UUID kingdomUUID, Kingdom kingdom) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO kingdoms (ID, name, description, open, creationTime, leader, level, claimedChunks, home_world_id, home_x, home_y, home_z, health) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"))
            {
//            "CREATE TABLE IF NOT EXISTS kingdoms (" +
//                    "ID TEXT," +
//                    "name TEXT," +
//                    "description TEXT," +
//                    "open BOOLEAN," +
//                    "creationTime TEXT," +
//                    "leader TEXT," +
//                    "level INT," +
//                    "claimedChunks INT," +
//                    "home_world_id TEXT," +
//                    "home_x INT," +
//                    "home_y INT," +
//                    "home_z INT," +
//                    "health INT" +
//                    ")"
            statement.setString(1, kingdomUUID.toString());
            statement.setString(2, kingdom.getName());
            statement.setString(3, kingdom.getDescription());
            statement.setBoolean(4, kingdom.isOpen());
            statement.setString(5, kingdom.getCreationTime());
            statement.setString(6, kingdom.getLeader().toString());
            statement.setInt(7, kingdom.getLevel());
            statement.setInt(8, kingdom.getClaimedChunks());
            statement.setString(9, kingdom.getHome().getWorld().getUID().toString());
            statement.setInt(10, kingdom.getHome().getBlockX());
            statement.setInt(11, kingdom.getHome().getBlockY());
            statement.setInt(12, kingdom.getHome().getBlockZ());
            statement.setInt(13, kingdom.getHealth());
            statement.executeUpdate();
        }
    }

    public static void saveTerritoryMapping(Connection connection, ChunkCoordinate chunk, UUID kingdomUUID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO chunks (chunk_owner, chunk_x, chunk_z, world_id) VALUES (?, ?, " +
                        "?, ?)")) {
//            "CREATE TABLE IF NOT EXISTS chunks (" +
//                    "chunk_owner INTEGER," +
//                    "chunk_x DOUBLE," +
//                    "chunk_z DOUBLE," +
//                    "world_id TEXT" +
//                    ")"
            statement.setString(1, kingdomUUID.toString());
            statement.setDouble(2, chunk.getX());
            statement.setDouble(3, chunk.getZ());
            statement.setString(4, chunk.getWorldID().toString());
            statement.executeUpdate();
        }
    }

    public static void saveAll() {
        CreateDB db = new CreateDB();
        try (Connection connection = db.getConnection()) {
            KingdomManager kingdomManager = KingdomManager.getInstance();

            // Save player mappings
            for (Map.Entry<UUID, UUID> entry : kingdomManager.getPlayerMappings().entrySet()) {
                UUID playerUUID = entry.getKey();
                UUID kingdomUUID = entry.getValue();
                try {
                    savePlayerMapping(connection, playerUUID, kingdomUUID);
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Saved player mapping for player " + playerUUID + " to kingdom " + kingdomUUID);
                } catch (SQLException e) {
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to save player mapping for player " + playerUUID + ": " + e.getMessage());
                }
            }

            // Save kingdom data
            for (Map.Entry<UUID, Kingdom> entry : kingdomManager.getKingdoms().entrySet()) {
                UUID kingdomUUID = entry.getKey();
                Kingdom kingdom = entry.getValue();
                try {
                    saveKingdom(connection, kingdomUUID, kingdom);
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Saved kingdom " + kingdomUUID);
                } catch (SQLException e) {
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to save kingdom " + kingdomUUID + ": " + e.getMessage());
                }
            }

            // Save territory mappings
            for (Map.Entry<ChunkCoordinate, UUID> entry : kingdomManager.getTerritoryMappings().entrySet()) {
                ChunkCoordinate chunk = entry.getKey();
                UUID kingdomUUID = entry.getValue();
                try {
                    saveTerritoryMapping(connection, chunk, kingdomUUID);
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Saved territory mapping for " +
                            "chunk " + chunk.getX() +", " + chunk.getZ() + " to kingdom " + kingdomUUID);
                } catch (SQLException e) {
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to save territory mapping for chunk " + chunk + ": " + e.getMessage());
                }
            }

            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "All data has been saved to the database.");

        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }

    }
}
