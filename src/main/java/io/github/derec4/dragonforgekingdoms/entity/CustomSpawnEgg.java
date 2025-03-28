package io.github.derec4.dragonforgekingdoms.entity;

import net.minecraft.world.item.Item;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomSpawnEgg extends Item {

    public CustomSpawnEgg(Properties properties) {
        super(properties);
    }

    public static ItemStack createCustomSpawnEgg(int customModelData) {
        ItemStack spawnEgg = new ItemStack(Material.SKELETON_SPAWN_EGG);
        ItemMeta meta = spawnEgg.getItemMeta();
        assert meta != null;
        meta.addEnchant(Enchantment.DURABILITY, 5, true);
        meta.setCustomModelData(customModelData);
        spawnEgg.setItemMeta(meta);
        return spawnEgg;
    }
}