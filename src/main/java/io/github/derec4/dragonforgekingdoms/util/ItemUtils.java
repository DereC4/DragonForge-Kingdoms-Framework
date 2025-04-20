package io.github.derec4.dragonforgekingdoms.util;

import io.github.derec4.dragonforgekingdoms.entity.CustomSpawnEgg;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtils {
    public static void getDamageModifiers(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null && itemMeta.getPersistentDataContainer().has(Attribute.GENERIC_ATTACK_DAMAGE.getKey(), PersistentDataType.STRING)) {
            itemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE);
        }
    }

    /**
     * Because Why Not?
     *
     * @param player
     */
    public static void removePufferfish(Player player) {
        removeItem(player, Material.PUFFERFISH, 8);
    }

    public static void removeItem(Player player, Material material, int amount) {
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

    public static void giveCustomEgg(Player player, int customModelData, int quantity) {
        if (quantity <= 0) {
            player.sendMessage(ChatColor.RED + "Quantity must be a positive number.");
            return;
        }

        ItemStack customEgg = CustomSpawnEgg.createCustomSpawnEgg(customModelData);
        customEgg.setAmount(quantity);
        player.getInventory().addItem(customEgg);

        String eggName;
        switch (customModelData) {
            case 1 -> eggName = "Guard Spawn Egg";
            case 2 -> eggName = "Soldier Spawn Egg";
            case 3 -> eggName = "Archer Spawn Egg";
            default -> eggName = "Unknown Spawn Egg";
        }

        player.sendMessage(ChatColor.GREEN + "[ADMIN] " + quantity + " " + eggName + "(s) have been given.");
    }
}
