package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.entity.CustomGuard;
import io.github.derec4.dragonforgekingdoms.entity.CustomSoldier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = Objects.requireNonNull(player.getEquipment()).getItemInMainHand();

        if (itemStack.getType() == Material.DRAGON_EGG && itemStack.getItemMeta().hasCustomModelData()) {
            int customModelData = itemStack.getItemMeta().getCustomModelData();
            Block clickedBlock = event.getClickedBlock();
            Level world = (Level) clickedBlock.getWorld();
            UUID kingdomID = UUID.randomUUID(); // Replace with actual kingdom ID

            if (customModelData == 1) {
                // Spawn CustomGuard
                CustomGuard customGuard = new CustomGuard(world, kingdomID);
                world.addFreshEntity(customGuard);
            } else if (customModelData == 2) {
                // Spawn CustomSoldier
                CustomSoldier customSoldier = new CustomSoldier(world, kingdomID);
                world.addFreshEntity(customSoldier);
            }

            itemStack.setAmount(itemStack.getAmount() - 1);
            event.setCancelled(true);
        }
    }

    public static ItemStack createCustomSpawnEgg(int customModelData) {
        ItemStack spawnEgg = new ItemStack(Material.SKELETON_SPAWN_EGG);
        ItemMeta meta = spawnEgg.getItemMeta();
        assert meta != null;
        meta.setCustomModelData(customModelData);
        spawnEgg.setItemMeta(meta);
        return spawnEgg;
    }
}