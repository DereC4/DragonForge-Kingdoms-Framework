package io.github.derec4.dragonforgekingdoms.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerUtils {
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

    public static void teleportPlayer(Player player, Location location, String message) {
        player.sendMessage(ChatColor.GREEN + message);
        player.teleport(location);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }


    /**
     * Because Why Not?
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
