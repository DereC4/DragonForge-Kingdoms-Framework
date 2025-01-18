package io.github.derec4.dragonforgekingdoms.util;

import io.github.derec4.dragonforgekingdoms.DragonForgeKingdoms;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.territory.ChunkCoordinate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DatabaseUtils {
    public static void updatePlayerKingdom(Connection connection, UUID playerUUID, UUID kingdomUUID) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO players (id, kingdom) VALUES (?,?)"
        )) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, kingdomUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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

    public static void loadKingdomsFromDatabase(Connection connection, Map<UUID, Kingdom> kingdoms) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM kingdoms")) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                UUID kingdomID = UUID.fromString(resultSet.getString("ID"));
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                boolean open = resultSet.getBoolean("open");
                String creationTime = resultSet.getString("creationTime");
                UUID leader = UUID.fromString(resultSet.getString("leader"));
                int level = resultSet.getInt("level");
                int claimedChunks = resultSet.getInt("claimedChunks");
                Location home = new Location(
                        Bukkit.getWorld(UUID.fromString(resultSet.getString("home_world_id"))),
                        resultSet.getInt("home_x"),
                        resultSet.getInt("home_y"),
                        resultSet.getInt("home_z")
                );
                int health = resultSet.getInt("health");
                int wealth = resultSet.getInt("wealth");

                Kingdom k = new Kingdom(kingdomID, name, leader, home, description, open, creationTime, level, claimedChunks, health, wealth);
                kingdoms.put(kingdomID, k);

                Bukkit.getLogger().info(String.format("Loaded kingdom: %s with attributes: ID=%s, description=%s, open=%b, creationTime=%s, leader=%s, level=%d, claimedChunks=%d, home=%s, health=%d",
                        name, kingdomID, description, open, creationTime, leader, level, claimedChunks, home, health));
                // doesn't include wealth ^
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to load kingdoms from database: " + e.getMessage());
            throw e;
        }
    }

    public static void loadTerritoryMappingsFromDatabase(Connection connection, Map<ChunkCoordinate, UUID> territoryMappings) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM chunks")) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int x = (int) resultSet.getDouble("chunk_x");
                int z = (int) resultSet.getDouble("chunk_z");
                UUID worldID = UUID.fromString(resultSet.getString("world_id"));
                ChunkCoordinate chunkCoord = new ChunkCoordinate(x, z, worldID);
                UUID kingdomUUID = UUID.fromString(resultSet.getString("chunk_owner"));
                territoryMappings.put(chunkCoord, kingdomUUID);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to load territory mappings from database: " + e.getMessage());
            throw e;
        }
    }

    public static void loadPlayersFromDatabase(Connection connection, Map<UUID, UUID> playerMappings) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM players")) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                UUID playerUUID = UUID.fromString(resultSet.getString("id"));
                UUID kingdomUUID = resultSet.getString("kingdom") != null ?
                        UUID.fromString(resultSet.getString("kingdom")) : null;
                playerMappings.put(playerUUID, kingdomUUID);
                Bukkit.getLogger().info("Added player UUID: " + playerUUID + ", Kingdom UUID: " + kingdomUUID);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to load players from database: " + e.getMessage());
            throw e;
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
                "INSERT OR REPLACE INTO kingdoms (ID, name, description, open, creationTime, leader, level, claimedChunks, home_world_id, home_x, home_y, home_z, health, wealth) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
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
            statement.setString(9, Objects.requireNonNull(kingdom.getHome().getWorld()).getUID().toString());
            // 12/10/2024 scored a big debugging victory tonight woooo the UUID was saving; it was just our constructor
            statement.setInt(10, kingdom.getHome().getBlockX());
            statement.setInt(11, kingdom.getHome().getBlockY());
            statement.setInt(12, kingdom.getHome().getBlockZ());
            statement.setInt(13, kingdom.getHealth());
            statement.setInt(14, kingdom.getWealth());
            statement.executeUpdate();
        }
    }

    public static void saveTerritoryMapping(Connection connection, ChunkCoordinate chunk, UUID kingdomUUID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO chunks (chunk_owner, chunk_x, chunk_z, world_id) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT(chunk_x, chunk_z, world_id) " +
                        "DO UPDATE SET chunk_owner = EXCLUDED.chunk_owner " +
                        "WHERE chunks.chunk_owner != EXCLUDED.chunk_owner")) {
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
                            "chunk " + chunk.getX() + ", " + chunk.getZ() + " to kingdom " + kingdomUUID);
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
