package io.github.derec4.dragonforgekingdoms.util;

import com.earth2me.essentials.api.Economy;
import io.github.derec4.dragonforgekingdoms.DragonForgeKingdoms;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.territory.ChunkCoordinate;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

import java.math.BigDecimal;
import java.util.*;

public class PlayerUtils {
    /**
     * Gets highest permission level (Kingdoms context) of a player
     * Warning: Anyone with server operator status or * permissions will also have these permissions
     * @param player Player to check
     * @return The highest kingdom permission level of a player, from Lord to Adventurer
     */
    public static PermissionLevel getPlayerRank(Player player) {
        if (player == null) {
            return PermissionLevel.ADVENTURE;
        }

        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(player.getUniqueId());

        if (user == null) {
            return PermissionLevel.ADVENTURE;
        }

        // CachedPermissionData for efficient permission checks
        CachedPermissionData permissionData = user.getCachedData().getPermissionData();

        if (permissionData.checkPermission("group.lord").asBoolean()) {
            return PermissionLevel.LORD;
        } else if (permissionData.checkPermission("group.duke").asBoolean()) {
            return PermissionLevel.DUKE;
        } else if (permissionData.checkPermission("group.vassal").asBoolean()) {
            return PermissionLevel.VASSAL;
        } else {
            return PermissionLevel.ADVENTURE;
        }
    }

    /**
     * Promotes a player to the next rank within the kingdom.
     * If the player is a vassal, they will be promoted to duke.
     * If the player is already a duke, they cannot be promoted further.
     *
     * @param player       The player issuing the promotion command.
     * @param targetPlayer The player to be promoted.
     */
    public static void promotePlayer(Player player, Player targetPlayer) {
        LuckPerms api = LuckPermsProvider.get();

        if (targetPlayer.hasPermission("group.vassal") && !targetPlayer.hasPermission("group.duke")) {
            // Promote vassal to duke
//            targetPlayer.addAttachment(DragonForgeKingdoms.getInstance(), "kingdom.role.duke", true, 1);
            Group group = api.getGroupManager().getGroup("duke");

            if (group == null) {
                player.sendMessage(ChatColor.RED + " group does not exist!");
                return;
            }

            api.getUserManager().modifyUser(player.getUniqueId(), (User user) -> {
                // Create a node to add to the player.
                Node node = InheritanceNode.builder(group).build();

                // Add the node to the user.
                user.data().add(node);
                targetPlayer.sendMessage(ChatColor.GREEN + "You have been promoted to Duke. " +
                        "You can now add, remove, and banish players, as well as access the kingdom store.");
                player.sendMessage(ChatColor.GREEN + "Player has been successfully promoted.");
            });
        } else if (targetPlayer.hasPermission("group.duke")) {
            player.sendMessage(ChatColor.YELLOW + targetPlayer.getName() + " cannot be promoted any higher than Duke.");
        } else {
            player.sendMessage(ChatColor.RED + targetPlayer.getName() + " could not be promoted.");
        }
    }

    /**
     * This method removes the player from the vassal, duke, and lord groups.
     *
     * @param playerUUID The UUID of the player whose permissions are to be cleared.
     */
    public static void clearPlayerPermissions(UUID playerUUID) {
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
    }

    public static void addPlayerToGroupAsync(UUID playerID, String groupName) {
        Bukkit.getScheduler().runTaskAsynchronously(DragonForgeKingdoms.getInstance(), () -> {
            LuckPerms api = LuckPermsProvider.get();
            Group group = api.getGroupManager().getGroup(groupName);
            api.getUserManager().modifyUser(playerID, (User user) -> {
                // Create a node to add to the player.
                assert group != null;
                Node node = InheritanceNode.builder(group).build();

                // Add the node to the user.
                user.data().add(node);
            });
        });
    }

    public static void addPlayerToVassalGroup(UUID playerUUID) {
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

    public static void addVassalGroup(LuckPerms luckPerms, User user) {
        Node node = Node.builder("group.vassal").build();
        user.data().add(node);
        luckPerms.getUserManager().saveUser(user);
    }

    public static void teleportPlayer(Player player, Location location, String message) {
        player.sendMessage(ChatColor.GREEN + message);
        player.teleport(location);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    public static ChunkCoordinate getPlayerCurrentChunk(Player player) {
        Location location = player.getLocation();
        return new ChunkCoordinate(
                location.getChunk().getX(),
                location.getChunk().getZ(),
                Objects.requireNonNull(location.getWorld()).getUID()
        );
    }

    public static ChunkCoordinate getChunk(Location location) {
        return new ChunkCoordinate(
                location.getChunk().getX(),
                location.getChunk().getZ(),
                Objects.requireNonNull(location.getWorld()).getUID()
        );
    }

    /**
     * Initializes the in-game sidebar as a HUD for the current kingdom the player is in
     * @param player Player to display sidebar
     * @param uuid Current kingdom the player is standing in
     */
    public static void setSidebar(Player player, UUID uuid) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard board = manager.getNewScoreboard();
        Objective objective;

        KingdomManager kingdomManager = KingdomManager.getInstance();
        objective = board.registerNewObjective("sidebar", Criteria.DUMMY, ChatColor.GOLD + "Kingdom Status");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        if (uuid != null) {
            Kingdom kingdom = kingdomManager.getKingdomFromID(uuid);

            Score kingdomName = objective.getScore(ChatColor.BLUE + "Kingdom: " + kingdom.getName());
            kingdomName.setScore(6);

            Score leader = objective.getScore(ChatColor.YELLOW + "Leader: " + Bukkit.getOfflinePlayer(kingdom.getLeader()).getName());
            leader.setScore(5);

            Score wealth = objective.getScore(ChatColor.GOLD + "Wealth: " + kingdom.getWealth());
            wealth.setScore(4);

            Score members = objective.getScore(ChatColor.AQUA + "Members: " + kingdom.getMembers().size());
            members.setScore(3);

            Score level = objective.getScore(ChatColor.RED + "Level: " + kingdom.getLevel());
            level.setScore(2);
        }

        Score divider = objective.getScore(ChatColor.GRAY + "----------------");
        divider.setScore(1);

        Score playerStatsTitle = objective.getScore(ChatColor.GOLD + "Player Stats");
        playerStatsTitle.setScore(0);

        String rank = PlayerUtils.getPlayerRank(player).name();
        Score playerRank = objective.getScore(ChatColor.LIGHT_PURPLE + "Rank: " + rank);
        playerRank.setScore(-1);

        double playerWealth;
        try {
            playerWealth = Economy.getMoneyExact(player.getUniqueId()).doubleValue();
        } catch (Exception e) {
            playerWealth = -1;
        }

        Score playerWealthScore = objective.getScore(ChatColor.GREEN + "Wealth: " + playerWealth);
        playerWealthScore.setScore(-2);

        player.setScoreboard(board);
    }

    /**
     * Updates sidebars for all players
     * There is probably a better way to do this...
     */
    public static void updateAllSidebars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ChunkCoordinate chunkCoordinate = getChunk(player.getLocation());
            UUID uuid = KingdomManager.getInstance().getKingdomByChunk(chunkCoordinate);
            setSidebar(player, uuid);
        }
    }

    /**
     * Strips a player of all LuckPerms groups and assigns them to the "adventurer" group.
     *
     * @param playerUUID The UUID of the player to modify.
     */
    public static void resetPlayerToAdventurer(UUID playerUUID) {
        LuckPerms api = LuckPermsProvider.get();

        Bukkit.getScheduler().runTaskAsynchronously(DragonForgeKingdoms.getInstance(), () -> {
            User user = api.getUserManager().getUser(playerUUID);

            if (user == null) {
                api.getUserManager().loadUser(playerUUID).thenAcceptAsync(loadedUser -> {
                    if (loadedUser != null) {
                        stripGroupsAndSetAdventurer(api, loadedUser);
                    }
                });
            } else {
                stripGroupsAndSetAdventurer(api, user);
            }
        });
    }

    /**
     * Helper method to strip all groups from a user and assign them to the "adventurer" group.
     *
     * @param api  The LuckPerms API instance.
     * @param user The LuckPerms user to modify.
     */
    private static void stripGroupsAndSetAdventurer(LuckPerms api, User user) {
        user.data().clear(NodeType.INHERITANCE::matches);

        Group adventurerGroup = api.getGroupManager().getGroup("adventurer");
        if (adventurerGroup != null) {
            Node adventurerNode = InheritanceNode.builder(adventurerGroup).build();
            user.data().add(adventurerNode);
        } else {
            Bukkit.getLogger().warning("Group 'adventurer' does not exist in LuckPerms.");
        }

        api.getUserManager().saveUser(user);
    }

}
