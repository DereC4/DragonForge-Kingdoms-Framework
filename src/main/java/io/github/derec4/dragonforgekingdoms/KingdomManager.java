package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class KingdomManager {
    private static KingdomManager instance;
    private final Map<UUID, Kingdom> kingdoms; // Maps UUID to a Kingdom Object
    private final Map<UUID, UUID> playerMappings; // Maps player UUID to their kingdom UUID
    private final Map<ChunkCoordinate, UUID> territoryMappings; // Maps chunk coordinates to a Kingdom UUID

    private KingdomManager() {
        kingdoms = new HashMap<>();
        playerMappings = new HashMap<>();
        territoryMappings = new HashMap<>();

    }

    public static synchronized KingdomManager getInstance() {
        if (instance == null) {
            instance = new KingdomManager();
        }
        return instance;
    }

    public void loadKingdomsFromDatabase(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM kingdoms")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                UUID kingdomID = UUID.fromString(resultSet.getString("ID"));
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                boolean open = resultSet.getBoolean("open");
//                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
                String creationTime = resultSet.getString("creationTime");
                UUID leader = UUID.fromString(resultSet.getString("leader"));
                int level = resultSet.getInt("level");
                int claimedChunks = resultSet.getInt("claimedChunks");
                Location home = new Location(
                        Bukkit.getWorld(resultSet.getString("home_world_id")),
                        resultSet.getInt("home_x"),
                        resultSet.getInt("home_y"),
                        resultSet.getInt("home_z")
                );

                // Create a Kingdom object and add it to the map
                Kingdom k = new Kingdom(kingdomID, name, leader, home, description, open, creationTime, level, claimedChunks);
                // You may want to set other properties of the Kingdom based on your design
                kingdoms.put(kingdomID, k);
                System.out.println("Message is here " + k.getID());
            }
        }
    }


    /**
     * Load in all chunks kingdoms own from the database
     */
    public void loadTerritoryMappingsFromDatabase(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM chunks")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int x = resultSet.getInt("chunk_x");
                int z = resultSet.getInt("chunk_z");
                UUID worldID = UUID.fromString(resultSet.getString("world_id"));
                ChunkCoordinate chunkCoord = new ChunkCoordinate(x, z, worldID);
                UUID kingdomUUID = UUID.fromString(resultSet.getString("chunk_owner"));
                territoryMappings.put(chunkCoord, kingdomUUID);
            }
        }
    }

    // Add methods to manage factions (e.g., addFaction, getFactions, etc.)

    /**
     * Adds a new kingdom to the list and associates the provided player to it in the
     * playerMappings list
     * @param kingdom
     * @param playerID
     */
    public void addKingdom(Kingdom kingdom, UUID playerID) {
        kingdoms.put(kingdom.getID(), kingdom);
        playerMappings.put(playerID, kingdom.getID());
    }

    // Method to add player to a kingdom
    public void addPlayerToKingdom(UUID playerUUID, UUID kingdomUUID) {
        if(playerMappings.containsKey(playerUUID)) {
            return;
        }

        // Update the player's kingdom in the database and then in the hashmap
        CreateDB temp = new CreateDB();
        try {
            Connection connection = temp.getConnection();
            updatePlayerKingdom(connection, playerUUID, kingdomUUID);
            playerMappings.put(playerUUID, kingdomUUID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Kingdom k = kingdoms.get(kingdomUUID);
        k.addPlayer(playerUUID);
    }

    public void updatePlayerKingdom(Connection connection, UUID playerUUID, UUID kingdomUUID) {
//        try (PreparedStatement statement = connection.prepareStatement(
//                "UPDATE players SET kingdom = ? WHERE id = ?")) {
//            statement.setString(1, kingdomUUID.toString());
//            statement.setString(2, playerUUID.toString());
//            statement.executeUpdate();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO players (id, kingdom) VALUES (?,?) ON DUPLICATE KEY UPDATE kingdom = VALUES(kingdom)"
        )) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, kingdomUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * Returns the Kingdom a player UUID belongs to
     * @param playerUUID
     * @return
     */
    public Kingdom getPlayerKingdom(UUID playerUUID) {
        return kingdoms.get(playerMappings.get(playerUUID));
    }

    /**
     * Checks if a player is in a kingdom
     */
    public boolean isPlayerMapped(UUID playerUUID) {
        return playerMappings.containsKey(playerUUID);
    }

    public boolean containsName(String name) {
        for(UUID uuid: kingdoms.keySet()) {
            if(kingdoms.get(uuid).getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean removePlayer(UUID playerUUID) {
        boolean res = kingdoms.get(playerMappings.get(playerUUID)).removePlayer(playerUUID);
        playerMappings.remove(playerUUID);
        CreateDB temp = new CreateDB();
        try {
            Connection connection = temp.getConnection();
            removePlayerFromDatabase(connection, playerUUID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public void removePlayerFromDatabase(Connection connection, UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM players WHERE id = ?")) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a kingdom's UUID from a given name
     */
    public UUID getKingdomFromName(String name) {
        for(UUID u : kingdoms.keySet()) {
            if(kingdoms.get(u).getName().equals(name)) {
                return u;
            }
        }
        return null;
    }

    public void removeKingdom(UUID playerUUID, Connection connection) {
        UUID kingdomUUID = playerMappings.get(playerUUID);

        if (kingdomUUID != null) {
            // Remove the kingdom from the in-memory map
            kingdoms.remove(kingdomUUID);

            // Connect to the database and delete the corresponding row
            if (connection != null) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM kingdoms WHERE ID = ?")) {
                    statement.setString(1, kingdomUUID.toString());
                    statement.executeUpdate();
                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Kingdom has been removed");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Gets a kingdom corresponding to the given UUID, or null if none
     */
    public Kingdom getKingdomFromID(UUID id) {
        return kingdoms.get(id);
    }

    /**
     * Admin command to directly remove a kingdom based on name. Not to be used normally.
     * Does not update the existing manager too, so may have to restart server.
     */
    public void removeKingdomAdmin(String name, Connection connection) {
        if (connection != null) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM kingdoms WHERE name = ?")) {
                statement.setString(1, name);
                statement.executeUpdate();
                Bukkit.getServer().getConsoleSender().sendMessage("Kingdom has been removed");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Claims a chunk for a kingdom and adds it to the set of Chunk - Kingdom Mappings
     */
    public boolean claimChunk(UUID kingdomUUID, ChunkCoordinate chunkCoord) {
        CreateDB temp = new CreateDB();
        try {
            Connection connection = temp.getConnection();
            saveTerritoryToDatabase(connection, chunkCoord, kingdomUUID);
            if(territoryMappings.get(chunkCoord) == null) {
                territoryMappings.put(chunkCoord, kingdomUUID);
                kingdoms.get(kingdomUUID).claimChunk();
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    // Remove a chunk from a kingdom's territory
    public boolean removeChunkFromKingdom(ChunkCoordinate chunkCoord) {
        territoryMappings.remove(chunkCoord);
        // Remove the chunk from the database
        CreateDB temp = new CreateDB();
        try {
            Connection connection = temp.getConnection();
            removeTerritoryFromDatabase(connection, chunkCoord);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public Map<UUID, Kingdom> getKingdoms() {
        return kingdoms;
    }

    /**
     * Gets the kingdom that claims the chunkcoord
     * @param chunkCoord
     * @return
     */
    public UUID getKingdomByChunk(ChunkCoordinate chunkCoord) {
        return territoryMappings.get(chunkCoord);
    }

    public void saveTerritoryToDatabase(Connection connection, ChunkCoordinate chunkCoord, UUID ID) {
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
    public void removeTerritoryFromDatabase(Connection connection, ChunkCoordinate chunkCoord) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM territory WHERE chunk_x = ? AND chunk_z = ? AND world_id = ?")) {
            statement.setInt(1, chunkCoord.getX());
            statement.setInt(2, chunkCoord.getZ());
            statement.setString(3, chunkCoord.getWorldID().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



}