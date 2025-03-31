package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.UUID;

public class CustomSpawnEggListener implements Listener {
    private final KingdomManager kingdomManager;

    public CustomSpawnEggListener() {
        this.kingdomManager = KingdomManager.getInstance();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = Objects.requireNonNull(player.getEquipment()).getItemInMainHand();
//        System.out.println("TEMP TEMP " + itemStack.getItemMeta().getAsString());
        if (itemStack.getType() == Material.SKELETON_SPAWN_EGG && itemStack.getEnchantmentLevel(Enchantment.DURABILITY) == 5 && itemStack.getItemMeta().hasCustomModelData()) {
            UUID playerUUID = player.getUniqueId();
            Kingdom kingdom = kingdomManager.getPlayerKingdom(playerUUID);

            int customModelData = itemStack.getItemMeta().getCustomModelData();
            ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();
            UUID kingdomID = kingdomManager.getPlayerKingdom(player.getUniqueId()).getID();

            if (customModelData == 1) {
                CustomGuard customGuard = new CustomGuard(world, kingdomID);
                customGuard.setPos(event.getPlayer().getLocation().getX(), event.getPlayer().getLocation().getY(),
                        event.getPlayer().getLocation().getZ());
                world.addFreshEntity(customGuard);
            } else if (customModelData == 2) {
                CustomSoldier customSoldier = new CustomSoldier(world, kingdomID);
                customSoldier.setPos(event.getPlayer().getLocation().getX(), event.getPlayer().getLocation().getY(),
                        event.getPlayer().getLocation().getZ());
                world.addFreshEntity(customSoldier);
            }
            kingdom.incrementMobCount();
            itemStack.setAmount(itemStack.getAmount() - 1);
            event.setCancelled(true);
        }
    }
}