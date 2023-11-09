package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

    // Get a kingdom by ID
    public Kingdom getKingdomByName(UUID id) {
        return kingdoms.get(id);
    }

    // Get all kingdoms
    public List<Kingdom> getAllKingdoms() {
        return new ArrayList<>(kingdoms.values());
    }

    // Method to add player to a kingdom
    public boolean addPlayerToKingdom(UUID playerUUID, UUID kingdomUUID) {
        if(playerMappings.containsKey(playerUUID)) {
            return false;
        }
        playerMappings.put(playerUUID, kingdomUUID);
        return true;
    }

    /**
     * Returns the Kingdom a player UUID belongs to
     * @param playerUUID
     * @return
     */
    public Kingdom getPlayerKingdom(UUID playerUUID) {
        return kingdoms.get(playerMappings.get(playerUUID));
    }

    public boolean isPlayerMapped(UUID playerUUID) {
        return playerMappings.get(playerUUID) != null;
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
        playerMappings.put(playerUUID, null);
        return res;
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
                    Bukkit.getServer().getConsoleSender().sendMessage("Kingdom has been removed");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Admin command to directly remove a kingdom based on name. Not to be used normally.
     * Does not update the existing manager too, so may have to restart server.
     * @param name
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
     * @param kingdomUUID
     * @param chunkCoord
     * @return
     */
    public boolean claimChunk(UUID kingdomUUID, ChunkCoordinate chunkCoord) {
        CreateDB temp = new CreateDB();
        try {
            Connection connection = temp.getConnection();
            saveTerritoryToDatabase(connection, chunkCoord, kingdomUUID);
            if(territoryMappings.get(chunkCoord) == null) {
                territoryMappings.put(chunkCoord, kingdomUUID);
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
                "INSERT INTO chunks (chunk_owner, chunk_x, chunk_z, world_id)")) {
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