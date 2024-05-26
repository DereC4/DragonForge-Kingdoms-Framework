package io.github.derec4.dragonforgekingdoms.kingdom;

import io.github.derec4.dragonforgekingdoms.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.EggData;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
//                System.out.println("Message is here " + k.getID());
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
                int x = (int) resultSet.getDouble("chunk_x");
                int z = (int) resultSet.getDouble("chunk_z");
                UUID worldID = UUID.fromString(resultSet.getString("world_id"));
                ChunkCoordinate chunkCoord = new ChunkCoordinate(x, z, worldID);
                UUID kingdomUUID = UUID.fromString(resultSet.getString("chunk_owner"));
                territoryMappings.put(chunkCoord, kingdomUUID);
            }
        }
    }

    public void loadPlayersFromDatabase(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM players")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                UUID playerUUID = UUID.fromString(resultSet.getString("id"));
                UUID kingdomUUID = resultSet.getString("kingdom") != null ?
                        UUID.fromString(resultSet.getString("kingdom")) : null;

                // You can do something with the loaded player and kingdom UUID, such as updating your data structures.
                // For example, you might want to associate the player UUID with the kingdom UUID in your data structures.

                // Assuming you have a method like addPlayerToKingdom in your KingdomManager class:
                playerMappings.put(playerUUID, kingdomUUID);
            }
        }
    }

    public void createKingdom(Kingdom kingdom, UUID playerID) {
        // Update the player's kingdom in the database
        CreateDB databaseManager = new CreateDB();
        try (Connection connection = databaseManager.getConnection()) {
            kingdom.saveToDatabase(connection);
            kingdoms.put(kingdom.getID(), kingdom);
            playerMappings.put(playerID, kingdom.getID());
            LuckPerms api = LuckPermsProvider.get();
            Group group = api.getGroupManager().getGroup("lord");
            api.getUserManager().modifyUser(playerID, (User user) -> {
                // Create a node to add to the player.
                assert group != null;
                Node node = InheritanceNode.builder(group).build();

                // Add the node to the user.
                user.data().add(node);
            });
            updatePlayerKingdom(connection, playerID, kingdom.getID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createHeartstone(Kingdom kingdom, Player player) {
        EggData.assignEggData(kingdom, player.getLocation());
    }

    /**
     * Adds a new kingdom to the list and associates the provided player to it in the
     * playerMappings list
     * @param kingdom
     * @param playerID
     */
    public void addKingdom(Kingdom kingdom, UUID playerID) {
        // Update the player's kingdom in the database
        CreateDB temp = new CreateDB();
        try {
            kingdoms.put(kingdom.getID(), kingdom);
            playerMappings.put(playerID, kingdom.getID());
            Connection connection = temp.getConnection();
            updatePlayerKingdom(connection, playerID, kingdom.getID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
            checkLevelUp(kingdoms.get(kingdomUUID));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Kingdom k = kingdoms.get(kingdomUUID);
        k.addPlayer(playerUUID);
    }

    public void updatePlayerKingdom(Connection connection, UUID playerUUID, UUID kingdomUUID) {
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

    public String getKingdomTerritory(UUID uuid) {
        StringBuilder message = new StringBuilder("Chunks owned by Kingdom " + uuid + ":\n");
        for (Map.Entry<ChunkCoordinate, UUID> entry : territoryMappings.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                ChunkCoordinate chunk = entry.getKey();
                message.append(ChatColor.YELLOW)
                        .append("Chunk (")
                        .append(chunk.getX())
                        .append(", ")
                        .append(chunk.getZ())
                        .append(")\n");
            }
        }
        return message.toString();
    }

    public boolean playerLeave(Player player) {
        // Check if the player has at least 8 Pufferfish

        if(!player.isOp()) {
            if (!hasRequiredItems(player, Material.PUFFERFISH, 8)) {
                // Player doesn't have enough Pufferfish, deny leaving
                player.sendMessage(ChatColor.RED + "You need at least 8 Pufferfish to leave.");
                return false;
            }
            removePufferfish(player);
        }
        Kingdom kingdom = kingdoms.get(playerMappings.get(player.getUniqueId()));
        boolean res = kingdom.removePlayer(player.getUniqueId());
        playerMappings.remove(player.getUniqueId());
        CreateDB temp = new CreateDB();
        try {
            Connection connection = temp.getConnection();
            removePlayerFromDatabase(connection, player.getUniqueId());
            // If the kingdom has no members left, delete it
            if (kingdom.getMembers().isEmpty()) {
                // Delete Kingdom
                kingdoms.remove(kingdom.getID());
                removeKingdom(player.getUniqueId(), connection);
            }
            LuckPerms api = LuckPermsProvider.get();
            Map<String, String> permissionToGroupMap = Map.of(
                    "group.vassal", "vassal",
                    "group.duke", "duke",
                    "group.lord", "lord"
            );

            Set<Group> groupsToRemove = new HashSet<>();

            for (Map.Entry<String, String> entry : permissionToGroupMap.entrySet()) {
                if (player.hasPermission(entry.getKey())) {
                    Group group = api.getGroupManager().getGroup(entry.getValue());
                    if (group != null) {
                        groupsToRemove.add(group);
                        System.out.println(group.getName());
                    }
                }
            }

            api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                for (Group groupToRemove : groupsToRemove) {
                    Node node = InheritanceNode.builder(groupToRemove).build();
                    user.data().remove(node);
                    System.out.println(groupToRemove.getName());
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private boolean hasRequiredItems(Player player, Material material, int requiredAmount) {
        int count = 0;

        // Count the number of the specified item in the player's inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }

        return count >= requiredAmount;
    }

    /**
     * Because Why Not?
     */
    public void removePufferfish(Player player) {
        removeItem(player, Material.PUFFERFISH, 8);
    }

    private void removeItem(Player player, Material material, int amount) {
        int remainingAmount = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remainingAmount) {
                    player.getInventory().remove(item);
                    remainingAmount -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remainingAmount);
                    break;
                }
                if (remainingAmount <= 0) {
                    break;
                }
            }
        }
    }

    /**
     * Directly removes a player; administrator type command
     * @param playerUUID UUID of the player to be removed
     */
    public void removePlayer(UUID playerUUID) {
        CreateDB temp = new CreateDB();
        try {
            Connection connection = temp.getConnection();
            removePlayerFromDatabase(connection, playerUUID);
            LuckPerms api = LuckPermsProvider.get();
            Player player = Bukkit.getPlayer(playerUUID);
            assert player != null;
            Map<String, String> permissionToGroupMap = Map.of(
                    "group.vassal", "vassal",
                    "group.duke", "duke",
                    "group.lord", "lord"
            );
            Set<Group> groupsToRemove = new HashSet<>();
            for (Map.Entry<String, String> entry : permissionToGroupMap.entrySet()) {
                if (player.hasPermission(entry.getKey())) {
                    Group group = api.getGroupManager().getGroup(entry.getValue());
                    if (group != null) {
                        groupsToRemove.add(group);
                        System.out.println(group.getName());
                    }
                }
            }
            api.getUserManager().modifyUser(playerUUID, user -> {
                for (Group groupToRemove : groupsToRemove) {
                    Node node = InheritanceNode.builder(groupToRemove).build();
                    user.data().remove(node);
                    System.out.println(groupToRemove.getName());
                }
            });
            kingdoms.get(playerMappings.get(playerUUID)).removePlayer(playerUUID);
            playerMappings.remove(playerUUID);
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Removed player " + playerUUID + " " +
                    "from kingdom");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a kingdom's UUID from a given name
     */
    public UUID getKingdomFromName(String name) {
        for (UUID u : kingdoms.keySet()) {
            if (kingdoms.get(u).getName().equals(name)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Deletes a kingdom completely, updating the database as well
     * @param kingdomUUID The provided ID of the kingdom to delete
     */
    public void removeKingdom(UUID kingdomUUID) {
        if (!kingdoms.containsKey(kingdomUUID)) {
            return;
        }
        CreateDB databaseManager = new CreateDB();
        try (Connection connection = databaseManager.getConnection()) {
            // Remove the kingdom from the in-memory map
            for(UUID uuid: kingdoms.get(kingdomUUID).getMembers()) {
                removePlayer(uuid);
            }
            // Use an iterator to avoid ConcurrentModificationException
            Iterator<ChunkCoordinate> iterator = territoryMappings.keySet().iterator();
            while (iterator.hasNext()) {
                ChunkCoordinate chunkCoordinate = iterator.next();
                if (territoryMappings.get(chunkCoordinate).equals(kingdomUUID)) {
                    removeTerritoryFromDatabase(connection, chunkCoordinate);
                    iterator.remove();
                    System.out.println("Removed chunk");
                }
            }
            kingdoms.remove(kingdomUUID);
            removeKingdomFromDatabase(connection, kingdomUUID);
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }

    /**
     * Deletes a kingdom completely, updating the database as well
     * @param playerUUID The provided ID of the player who's kingdom will be removed
     */
    public void removeKingdom(UUID playerUUID, Connection connection) {
        UUID kingdomUUID = playerMappings.get(playerUUID);
        if (kingdomUUID == null) {
            return;
        }

        // Remove the kingdom from the in-memory map
        for(UUID uuid: kingdoms.get(kingdomUUID).getMembers()) {
            removePlayer(uuid);
        }
        kingdoms.remove(kingdomUUID);
        for (Map.Entry<ChunkCoordinate, UUID> entry : territoryMappings.entrySet()) {
            if (entry.getValue().equals(kingdomUUID)) {
                removeTerritoryFromDatabase(connection, entry.getKey());
                territoryMappings.remove(entry.getKey());
            }
        }
        removeKingdomFromDatabase(connection, kingdomUUID);
    }

    public void removeKingdomFromDatabase(Connection connection, UUID kingdomUUID) {
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
     * Checks if a kingdom has met the criteria for leveling up, and then level up
     * @param kingdom The ID of the kingdom to check
     */
    public void checkLevelUp(Kingdom kingdom) {
        int memberCount = kingdom.getMembers().size();
//        int chunksClaimed = k.getClaimedChunks();
        if (memberCount >= 200) {
            kingdom.setLevel(7);
        } else if (memberCount >= 100) {
            kingdom.setLevel(6);
        } else if (memberCount >= 50) {
            kingdom.setLevel(5);
        } else if (memberCount >= 25) {
            kingdom.setLevel(4);
        } else if (memberCount >= 10 ) {
            kingdom.setLevel(3);
        } else if (memberCount >= 2) {
            kingdom.setLevel(2);
        } else if (memberCount >= 1) {
            kingdom.setLevel(1);
        }

        // Claim chunks in a ring pattern around the home location for each level up
        if (kingdom.getHome() != null) {
            claimChunksInRingPattern(kingdom);
        }
    }

    /**
     * Claim chunks in a ring pattern centered around a kingdom's home Chunk
     * @param kingdom The ID of the kingdom gaining territory
     */
    private void claimChunksInRingPattern(Kingdom kingdom) {
        // Determine the size of the ring for the current level
        int ringSize = (kingdom.getLevel() - 1) * 2 + 1;

        // Convert the home location to a ChunkCoordinate
        Location home = kingdom.getHome();
        int chunkX = home.getChunk().getX();
        int chunkZ = home.getChunk().getZ();
        ChunkCoordinate centerChunkCoord = new ChunkCoordinate(chunkX, chunkZ, home.getWorld().getUID());

        // Claim chunks in a ring pattern around the home location
        for (int dx = -ringSize / 2; dx <= ringSize / 2; dx++) {
            for (int dz = -ringSize / 2; dz <= ringSize / 2; dz++) {
                int newX = centerChunkCoord.getX() + dx;
                int newZ = centerChunkCoord.getZ() + dz;
                ChunkCoordinate newChunkCoord = new ChunkCoordinate(newX, newZ, centerChunkCoord.getWorldID());
                claimChunk(kingdom.getID(), newChunkCoord);
                kingdom.claimChunk();
            }
        }
    }

    /**
     * @param kingdomUUID The ID of the kingdom to check the Hash Table for
     * @return If the provided ID exists in the Hash Table
     */
    public Kingdom getKingdomFromID(UUID kingdomUUID) {
        return kingdoms.get(kingdomUUID);
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
        // First, check if the kingdom can even claim the chunk
        Kingdom kingdom = kingdoms.get(kingdomUUID);
//        if(!k.canClaimMoreChunks()) {
//            return false;
//        }
        CreateDB temp = new CreateDB();
        try {
            Connection connection = temp.getConnection();
            saveTerritoryToDatabase(connection, chunkCoord, kingdomUUID);
            if(territoryMappings.get(chunkCoord) == null) {
                territoryMappings.put(chunkCoord, kingdomUUID);
                kingdom.claimChunk();
                checkLevelUp(kingdom);
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