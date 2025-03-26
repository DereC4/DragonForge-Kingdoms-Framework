package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.entity.CustomGuard;
import io.github.derec4.dragonforgekingdoms.entity.CustomSoldier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.UUID;

public class CustomSpawnEgg extends Item implements Listener {

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