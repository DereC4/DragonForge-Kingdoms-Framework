package io.github.derec4.dragonforgekingdoms;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class KingdomManager {
    private static KingdomManager instance;
    private final Map<UUID, Kingdom> kingdoms; // Maps UUID to a Kingdom Object
    private final Map<UUID, UUID> playerMappings; // Maps player UUID to their kingdom UUID

    private KingdomManager() {
        kingdoms = new HashMap<>();
        playerMappings = new HashMap<>();
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

    // Method to get a player's kingdom UUID
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
                    Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
                }
            }
        }
    }
}