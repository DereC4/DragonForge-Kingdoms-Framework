package io.github.derec4.dragonforgekingdoms.util;

import io.github.derec4.dragonforgekingdoms.DragonForgeKingdoms;
import io.github.derec4.dragonforgekingdoms.territory.ChunkCoordinate;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerUtils {
    public static PermissionLevel getPlayerRank(Player player) {
        LuckPerms api = LuckPermsProvider.get();

        if (player == null) {
            return PermissionLevel.NONE;
        }

        if (player.hasPermission("group.lord")) {
            return PermissionLevel.LORD;
        } else if (player.hasPermission("group.duke")) {
            return PermissionLevel.DUKE;
        } else if (player.hasPermission("group.vassal")) {
            return PermissionLevel.VASSAL;
        } else {
            return PermissionLevel.NONE;
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
     * Because Why Not?
     *
     * @param player
     */
    public static void removePufferfish(Player player) {
        removeItem(player, Material.PUFFERFISH, 8);
    }

    private static void removeItem(Player player, Material material, int amount) {
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

}
