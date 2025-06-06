package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.DragonForgeKingdoms;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.territory.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.util.EntityTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

public class CustomSpawnEggListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Bukkit.getLogger().info("[DEBUG] onPlayerInteract called");

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
//            Bukkit.getLogger().info("[DEBUG] Clicked block is null, returning");

            return;
        }

        Player player = event.getPlayer();
        ItemStack itemStack = Objects.requireNonNull(player.getEquipment()).getItemInMainHand();

        if (itemStack.getType() != Material.SKELETON_SPAWN_EGG
                || itemStack.getEnchantmentLevel(Enchantment.DURABILITY) != 5
                || !itemStack.getItemMeta().hasCustomModelData()) {
//            Bukkit.getLogger().info("Not a custom spawn egg, returning");
            return;
        }

        int customModelData = itemStack.getItemMeta().getCustomModelData();

        if (customModelData != 1 && customModelData != 2 && customModelData != 3) {
//            Bukkit.getLogger().info("No valid customModelData, returning");
            return;
        }

        // From here we confirm it is a kingdom spawn egg (99% chance)

        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
//            Bukkit.getLogger().info("[DEBUG] Player is in spectator mode, returning");
            return;
        }
//        Bukkit.getLogger().info("[DEBUG] Reached here");

//        System.out.println("TEMP TEMP " + itemStack.getItemMeta().getAsString());
        KingdomManager kingdomManager = KingdomManager.getInstance();
        UUID playerUUID = player.getUniqueId();
        Kingdom playerKingdom = kingdomManager.getPlayerKingdom(playerUUID);

        if (playerKingdom == null) {
            player.sendMessage("You are not part of a kingdom.");
            event.setCancelled(true);
            return;
        }

        UUID worldUUID = player.getWorld().getUID();
        ChunkCoordinate chunkCoordinate = new ChunkCoordinate(clickedBlock.getChunk().getX(), clickedBlock.getChunk().getZ(), worldUUID);

        if (!kingdomManager.isWithinKingdomTerritory(playerKingdom.getID(), chunkCoordinate)) {
            player.sendMessage(ChatColor.RED + "You can only use spawn eggs within your kingdom's territory.");
            event.setCancelled(true);
            return;
        }

        if (!playerKingdom.canSpawnMoreMobs()) {
            player.sendMessage("Your kingdom has reached the maximum number of mobs for its level.");
            event.setCancelled(true);
            return;
        }

        ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();
        UUID kingdomID = kingdomManager.getPlayerKingdom(player.getUniqueId()).getID();
        double spawnX = clickedBlock.getX() + 0.5;
        double spawnY = clickedBlock.getY() + 1.0;
        double spawnZ = clickedBlock.getZ() + 0.5;

        if (customModelData == 1) { // CustomGuard
            CustomGuard customGuard = new CustomGuard(world, kingdomID, new BlockPos((int) spawnX, (int) spawnY, (int) spawnZ));
            customGuard.setPos(spawnX, spawnY, spawnZ);
            world.addFreshEntity(customGuard);
            customGuard.getBukkitEntity().getPersistentDataContainer().set(EntityTags.MOB_TYPE_KEY, PersistentDataType.INTEGER, 0);
            customGuard.getBukkitEntity().getPersistentDataContainer().set(EntityTags.KINGDOM_ID_KEY,
                    PersistentDataType.STRING, kingdomID.toString());
        } else if (customModelData == 2) { // CustomSoldier
            CustomSoldier customSoldier = new CustomSoldier(world, kingdomID, new BlockPos((int) spawnX, (int) spawnY, (int) spawnZ));
            customSoldier.setPos(spawnX, spawnY, spawnZ);
            world.addFreshEntity(customSoldier);
            customSoldier.getBukkitEntity().getPersistentDataContainer().set(EntityTags.MOB_TYPE_KEY, PersistentDataType.INTEGER, 1);
            customSoldier.getBukkitEntity().getPersistentDataContainer().set(EntityTags.KINGDOM_ID_KEY,
                    PersistentDataType.STRING, kingdomID.toString());
        } else if (customModelData == 3) { // CustomArcher
            CustomArcher customArcher = new CustomArcher(world, kingdomID, new BlockPos((int) spawnX, (int) spawnY, (int) spawnZ));
            customArcher.setPos(spawnX, spawnY, spawnZ);
            world.addFreshEntity(customArcher);
            customArcher.getBukkitEntity().getPersistentDataContainer().set(EntityTags.MOB_TYPE_KEY, PersistentDataType.INTEGER, 2);
            customArcher.getBukkitEntity().getPersistentDataContainer().set(EntityTags.KINGDOM_ID_KEY,
                    PersistentDataType.STRING, kingdomID.toString());
        } else {
            return;
        }

        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }

        playerKingdom.incrementMobCount();
        event.setCancelled(true);
    }
}