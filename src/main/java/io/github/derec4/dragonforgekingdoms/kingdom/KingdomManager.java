package io.github.derec4.dragonforgekingdoms.kingdom;

import io.github.derec4.dragonforgekingdoms.kingdom.egg.EggData;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import io.github.derec4.dragonforgekingdoms.territory.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.util.DatabaseUtils;
import io.github.derec4.dragonforgekingdoms.util.ItemUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.derec4.dragonforgekingdoms.util.DatabaseUtils.removePlayerFromDatabase;
import static io.github.derec4.dragonforgekingdoms.util.PlayerUtils.*;

/**
 * [Server thread/INFO]: CommandBlock at -253,22,-165 issued server command: /tp Simpin4Netherite -253 24 -166
 */
@Getter
public class KingdomManager {
    private static KingdomManager instance;
    private final Map<UUID, Kingdom> kingdoms; // Maps UUID to a Kingdom Object
    private final Map<UUID, UUID> playerMappings; // Maps player UUID to their kingdom UUID
    private final Map<ChunkCoordinate, UUID> territoryMappings; // Maps chunk coordinates to a Kingdom UUID
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
        DatabaseUtils.loadKingdomsFromDatabase(connection, kingdoms);
    }

    public void loadTerritoryMappingsFromDatabase(Connection connection) throws SQLException {
        DatabaseUtils.loadTerritoryMappingsFromDatabase(connection, territoryMappings);
    }

    public void loadPlayersFromDatabase(Connection connection) throws SQLException {
        DatabaseUtils.loadPlayersFromDatabase(connection, playerMappings);
    }

    /**
     * Kingdom creation function without a callback
     *
     * @param kingdom
     * @param playerID
     */
    public void createKingdom(Kingdom kingdom, UUID playerID) {
        // Update the player's kingdom in the database
        kingdoms.put(kingdom.getID(), kingdom);
        playerMappings.put(playerID, kingdom.getID());

        // Promote kingdom creator to lord
        addPlayerToGroupAsync(playerID, "lord");
    }

    public void createHeartstone(Kingdom kingdom, Player player) {
        EggData.assignEggData(kingdom, player.getLocation());
    }

    /**
     * Method to add player to a kingdom, as well as update the player table in the database. If the player has
     * pending invites, the invite is removed. Player is then given the Vassal Luckperms permission group.
     *
     * @param playerUUID
     * @param kingdomUUID
     */
    public void addPlayerToKingdom(UUID playerUUID, UUID kingdomUUID) {
        if (playerMappings.containsKey(playerUUID)) {
            return;
        }

        playerMappings.put(playerUUID, kingdomUUID);
        pendingInvites.remove(playerUUID);
        checkLevelUp(kingdoms.get(kingdomUUID));
        Kingdom k = kingdoms.get(kingdomUUID);
        k.addPlayer(playerUUID);
        addPlayerToVassalGroup(playerUUID);
    }

    /**
     * Returns the Kingdom a player UUID belongs to
     */
    public Kingdom getPlayerKingdom(UUID playerUUID) {
        UUID kingdomID = playerMappings.getOrDefault(playerUUID, null);
        return kingdomID != null ? kingdoms.get(kingdomID) : null;
    }

    /**
     * Checks if a player is in a kingdom
     */
    public boolean isPlayerMapped(UUID playerUUID) {
        return playerMappings.containsKey(playerUUID);
    }

    public boolean containsName(String name) {
        for (UUID uuid : kingdoms.keySet()) {
            if (kingdoms.get(uuid).getName().equals(name)) {
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
        if (!player.isOp()) {
            if (!hasRequiredPufferfish(player)) {
                // Player doesn't have enough Pufferfish, deny leaving
                player.sendMessage(ChatColor.RED + "You need at least 8 Pufferfish to leave.");
                return false;
            }

            ItemUtils.removePufferfish(player);
        }

        UUID playerUUID = player.getUniqueId();
        Kingdom kingdom = kingdoms.get(playerMappings.get(playerUUID));

        if (kingdom.getLeader().equals(playerUUID)) {
            if (!kingdom.successionCrisis()) {
                cleanUpKingdomEgg(kingdom);
                kingdoms.remove(kingdom.getID());
                removeKingdom(kingdom.getID());
                territoryMappings.entrySet().removeIf(entry -> entry.getValue().equals(kingdom.getID()));
            }
        }

        boolean res = kingdom.removePlayer(playerUUID);
        playerMappings.remove(playerUUID);

        clearPlayerPermissions(playerUUID);

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
     * Helper function for kingdom deletion to remove the egg and the bedrock.
     *
     * @param kingdom
     */
    private void cleanUpKingdomEgg(Kingdom kingdom) {
        EggData eggData = kingdom.getEggData();
        if (eggData != null) {
            Block clickedBlock = new Location(Bukkit.getWorld(UUID.fromString(eggData.getWorld())), eggData.getX(), eggData.getY(), eggData.getZ()).getBlock();
            clickedBlock.setType(Material.AIR);
            clickedBlock.setType(Material.FIRE);
            Block bedrockBase = clickedBlock.getRelative(BlockFace.DOWN);
            bedrockBase.setType(Material.GRASS_BLOCK);

            // Iterate through all players and send a destruction notification to those that are online
            for (UUID uuid : kingdom.getMembers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendTitle(ChatColor.RED + "EGG DESTROYED", "Your kingdom has fallen!", 10, 70, 20);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f);
                }
            }
        }
    }

    public void removePlayer(UUID playerUUID) {
        getPlayerKingdom(playerUUID).removePlayer(playerUUID);
        clearPlayerPermissions(playerUUID);
//        kingdoms.get(playerMappings.get(playerUUID)).removePlayer(playerUUID);
        playerMappings.remove(playerUUID);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Removed player " + playerUUID + " " +
                "from kingdom");
    }

    /**
     * Directly removes a player; administrator type command
     *
     * @param playerUUID UUID of the player to be removed
     */
    public void removePlayerAsync(UUID playerUUID) {
        CreateDB temp = new CreateDB();
        try {
            Connection connection = temp.getConnection();
            removePlayerFromDatabase(connection, playerUUID);

            // Use PlayerUtils to clear player permissions
            clearPlayerPermissions(playerUUID);

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
     *
     * @param kingdomUUID The provided ID of the kingdom to delete
     */
    public void removeKingdom(UUID kingdomUUID) {
        if (!kingdoms.containsKey(kingdomUUID)) {
            return;
        }

        // Clear remaining players from the kingdom just in case
        for (UUID uuid : kingdoms.get(kingdomUUID).getMembers()) {
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
        cleanUpKingdomEgg(kingdoms.get(kingdomUUID));
        kingdoms.remove(kingdomUUID);
    }

    /**
     * Checks if a kingdom has met the criteria for leveling up, and then level up
     *
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
        } else if (memberCount >= 10) {
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
     *
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

        if (territoryMappings.get(chunkCoord) == null) {
            territoryMappings.put(chunkCoord, kingdomUUID);
            kingdom.incrementChunks();
            checkLevelUp(kingdom);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Gets the kingdom that claims the chunkcoord
     *
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

    public boolean isWithinKingdomTerritory(UUID kingdomID, BlockPos position, UUID worldID) {
        Kingdom kingdom = kingdoms.get(kingdomID);
        if (kingdom == null) return false;

        ChunkCoordinate chunkCoord = new ChunkCoordinate(position.getX() >> 4, position.getZ() >> 4, worldID);
        return territoryMappings.containsKey(chunkCoord);
    }
}
