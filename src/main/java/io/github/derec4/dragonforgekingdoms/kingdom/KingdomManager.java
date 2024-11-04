package io.github.derec4.dragonforgekingdoms.kingdom;

import io.github.derec4.dragonforgekingdoms.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.DragonForgeKingdoms;
import io.github.derec4.dragonforgekingdoms.EggData;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.derec4.dragonforgekingdoms.util.DatabaseUtils.*;
import static io.github.derec4.dragonforgekingdoms.util.PlayerUtils.clearPlayerPermissions;

public class KingdomManager {
    private static KingdomManager instance;
    @Getter
    private final Map<UUID, Kingdom> kingdoms; // Maps UUID to a Kingdom Object
    @Getter
    private final Map<UUID, UUID> playerMappings; // Maps player UUID to their kingdom UUID
    @Getter
    private final Map<ChunkCoordinate, UUID> territoryMappings; // Maps chunk coordinates to a Kingdom UUID
    @Getter
    private final Map<UUID, UUID> pendingInvites; // Maps invites: a player to a kingdom UUID

    private KingdomManager() {
        kingdoms = new ConcurrentHashMap<>();
        playerMappings = new ConcurrentHashMap<>();
        territoryMappings = new ConcurrentHashMap<>();
        pendingInvites = new ConcurrentHashMap<>();
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
                int health = resultSet.getInt("health");

                // Create a Kingdom object and add it to the map
                Kingdom k = new Kingdom(kingdomID, name, leader, home, description, open, creationTime, level,
                        claimedChunks, health);
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

    /**
     * Kingdom creation function without a callback
     * @param kingdom
     * @param playerID
     */
    public void createKingdom(Kingdom kingdom, UUID playerID) {
        // Update the player's kingdom in the database
        kingdoms.put(kingdom.getID(), kingdom);
        playerMappings.put(playerID, kingdom.getID());

        // Run LuckPerms API calls on the main thread
        Bukkit.getScheduler().runTaskAsynchronously(DragonForgeKingdoms.getInstance(), () -> {
            LuckPerms api = LuckPermsProvider.get();
            Group group = api.getGroupManager().getGroup("lord");
            api.getUserManager().modifyUser(playerID, (User user) -> {
                // Create a node to add to the player.
                assert group != null;
                Node node = InheritanceNode.builder(group).build();

                // Add the node to the user.
                user.data().add(node);
            });

        });
    }

    public void createKingdom(Kingdom kingdom, UUID playerID, Consumer<Kingdom> callback) {
        // Update the player's kingdom in the database
        final Kingdom kingdom1 = kingdom;
        final UUID playerID1 = playerID;
        Bukkit.getScheduler().runTaskAsynchronously(DragonForgeKingdoms.getInstance(), () -> {
            CreateDB databaseManager = new CreateDB();
            try (Connection connection = databaseManager.getConnection()) {
                kingdom1.saveToDatabase(connection);
                kingdoms.put(kingdom1.getID(), kingdom1);
                playerMappings.put(playerID1, kingdom1.getID());
                updatePlayerKingdom(connection, playerID1, kingdom1.getID());

                // Run LuckPerms API calls on the main thread
                Bukkit.getScheduler().runTaskAsynchronously(DragonForgeKingdoms.getInstance(), () -> {
                    LuckPerms api = LuckPermsProvider.get();
                    Group group = api.getGroupManager().getGroup("lord");
                    api.getUserManager().modifyUser(playerID1, (User user) -> {
                        // Create a node to add to the player.
                        assert group != null;
                        Node node = InheritanceNode.builder(group).build();

                        // Add the node to the user.
                        user.data().add(node);
                    });

                    callback.accept(kingdom1);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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
        final Kingdom kingdom1 = kingdom;
        final UUID playerID1 = playerID;
        Bukkit.getScheduler().runTaskAsynchronously(DragonForgeKingdoms.getInstance(), () -> {
            CreateDB temp = new CreateDB();
            try (Connection connection = temp.getConnection()) {
                kingdoms.put(kingdom1.getID(), kingdom1);
                playerMappings.put(playerID1, kingdom1.getID());
                updatePlayerKingdom(connection, playerID1, kingdom1.getID());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Method to add player to a kingdom, as well as update the player table in the database. If the player has
     * pending invites, the invite is removed. Player is then given the Vassal Luckperms permission group.
     * @param playerUUID
     * @param kingdomUUID
     */
    public void addPlayerToKingdom(UUID playerUUID, UUID kingdomUUID) {
        if(playerMappings.containsKey(playerUUID)) {
            return;
        }

        // Update the player's kingdom in the database and then in the hashmap
//        Bukkit.getScheduler().runTaskAsynchronously(DragonForgeKingdoms.getInstance(), () -> {
//            CreateDB temp = new CreateDB();
//            try (Connection connection = temp.getConnection()) {
//                updatePlayerKingdom(connection, playerID1, kingdom1);
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        });

        playerMappings.put(playerUUID, kingdomUUID);
        pendingInvites.remove(playerUUID);
        checkLevelUp(kingdoms.get(kingdomUUID));
        Kingdom k = kingdoms.get(kingdomUUID);
        k.addPlayer(playerUUID);
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(playerUUID);

        if (user == null) {
            luckPerms.getUserManager().loadUser(playerUUID).thenAcceptAsync(loadedUser -> {
                if (loadedUser != null) {
                    addVassalGroup(luckPerms, loadedUser);
                }
            });
        } else {
            addVassalGroup(luckPerms, user);
        }
    }

    private void addVassalGroup(LuckPerms luckPerms, User user) {
        Node node = Node.builder("group.vassal").build();
        user.data().add(node);
        luckPerms.getUserManager().saveUser(user);
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
        if(playerMappings.getOrDefault(playerUUID, null) == null) {
            return null;
        }
        return kingdoms.getOrDefault(playerMappings.get(playerUUID), null);
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

    public boolean handlePlayerLeaving(Player player) {
        // Check if the player has at least 8 Pufferfish
        if(!player.isOp()) {
            if (!hasRequiredPufferfish(player)) {
                // Player doesn't have enough Pufferfish, deny leaving
                player.sendMessage(ChatColor.RED + "You need at least 8 Pufferfish to leave.");
                return false;
            }
            removePufferfish(player);
        }
        Kingdom kingdom = kingdoms.get(playerMappings.get(player.getUniqueId()));
        boolean res = kingdom.removePlayer(player.getUniqueId());
        playerMappings.remove(player.getUniqueId());

        if (kingdom.getMembers().isEmpty()) {
            kingdoms.remove(kingdom.getID());
            removeKingdom(kingdom.getID());
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
        System.out.println(res + " ABCDEFG");
        return res;
    }

    private boolean hasRequiredPufferfish(Player player) {
        int count = 0;

        // Count the number of the specified item in the player's inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.PUFFERFISH) {
                count += item.getAmount();
            }
        }

        return count >= 8;
    }

    /**
     * Because Why Not?
     * @param player
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

    public void removePlayer(UUID playerUUID) {
        clearPlayerPermissions(playerUUID);
//        kingdoms.get(playerMappings.get(playerUUID)).removePlayer(playerUUID);
        playerMappings.remove(playerUUID);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Removed player " + playerUUID + " " +
                "from kingdom");
    }

    /**
     * Directly removes a player; administrator type command
     * @param playerUUID UUID of the player to be removed
     */
    public void removePlayerAsync(UUID playerUUID) {
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
     * Deletes a kingdom completely, without updating the database
     * @param kingdomUUID The provided ID of the kingdom to delete
     */
    public void removeKingdom(UUID kingdomUUID) {
        if (!kingdoms.containsKey(kingdomUUID)) {
            return;
        }

        // Clear remaining players from the kingdom just in case
        for(UUID uuid: kingdoms.get(kingdomUUID).getMembers()) {
            removePlayer(uuid);
        }

        kingdoms.get(kingdomUUID).clearMembers();

        // Use an iterator to avoid ConcurrentModificationException
        Iterator<ChunkCoordinate> iterator = territoryMappings.keySet().iterator();
        while (iterator.hasNext()) {
            ChunkCoordinate chunkCoordinate = iterator.next();
            if (territoryMappings.get(chunkCoordinate).equals(kingdomUUID)) {
                iterator.remove();
                System.out.println("Removed chunk");
            }
        }

        kingdoms.remove(kingdomUUID);
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
            removePlayerAsync(uuid);
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



    /**
     * Helper function for kingdom deletion to remove the egg and the bedrock.
     * @param kingdom
     */
    private void cleanUpKingdomEgg(Kingdom kingdom) {
        EggData eggData = kingdom.getEggData();

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
     * Claims a chunk for a kingdom and adds it to the set of Chunk - Kingdom Mappings
     */
    public boolean claimChunk(UUID kingdomUUID, ChunkCoordinate chunkCoord) {
        // First, check if the kingdom can even claim the chunk
        Kingdom kingdom = kingdoms.get(kingdomUUID);

        if(territoryMappings.get(chunkCoord) == null) {
            territoryMappings.put(chunkCoord, kingdomUUID);
            kingdom.claimChunk();
            checkLevelUp(kingdom);
        } else {
            return false;
        }
        return true;
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
    public boolean claimChunkAsync(UUID kingdomUUID, ChunkCoordinate chunkCoord) {
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

    /**
     * Gets the kingdom that claims the chunkcoord
     * @param chunkCoord
     * @return
     */
    public UUID getKingdomByChunk(ChunkCoordinate chunkCoord) {
        return territoryMappings.get(chunkCoord);
    }

    public void invitePlayerToKingdom(Player inviter, String recipient) {
        UUID inviterID = inviter.getUniqueId();
        Kingdom inviterKingdom = getPlayerKingdom(inviterID);
        if (inviterKingdom == null) {
            inviter.sendMessage(ChatColor.RED + "You are not in a kingdom.");
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(recipient);
        UUID targetPlayerID = targetPlayer.getUniqueId();

        if (pendingInvites.containsKey(targetPlayerID)) {
            inviter.sendMessage(ChatColor.RED + "That player already has a pending invite.");
            return;
        }

        pendingInvites.put(targetPlayerID, inviterKingdom.getID());

        if (targetPlayer.isOnline()) {
            Player onlineTargetPlayer = targetPlayer.getPlayer();
            onlineTargetPlayer.sendMessage(ChatColor.GREEN + "You have been invited to join the kingdom " + inviterKingdom.getName() + "." +
                    "\nJoin using /k join [Kingdom Name]");
        }

        inviter.sendMessage(ChatColor.GREEN + "Invitation sent to " + recipient + ".");
    }






}