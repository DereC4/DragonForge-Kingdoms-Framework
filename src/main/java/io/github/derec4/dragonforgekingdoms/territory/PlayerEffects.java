package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PlayerEffects {

    public static void applyEffects(Player player) {
        ChunkCoordinate playerChunk = getPlayerCurrentChunk(player);

        KingdomManager km = KingdomManager.getInstance();
        UUID playerUUID = player.getUniqueId();
        UUID kingdomUUID = km.getKingdomByChunk(playerChunk);
        Kingdom kingdom = km.getPlayerKingdom(playerUUID);
        UUID playerKingdomUUID = kingdom != null ? kingdom.getID() : null;
        if (kingdomUUID != null && playerKingdomUUID != null) {
            int level = km.getKingdomFromID(kingdomUUID).getLevel();
            if (kingdomUUID.equals(playerKingdomUUID)) {
                // Player is in their own kingdom
                applyBuffs(player, level);
            } else {
                // Player is in an enemy kingdom
                applyDebuffs(player, level);
            }
        } else if (kingdomUUID != null) {
            // Player is not in any kingdom but on another kingdom's territory
            int level = km.getKingdomFromID(kingdomUUID).getLevel();
            applyDebuffs(player, level);
        }
    }


    private static ChunkCoordinate getPlayerCurrentChunk(Player player) {
        Location playerLocation = player.getLocation();
        int x = playerLocation.getBlockX() >> 4;
        int z = playerLocation.getBlockZ() >> 4;
        UUID worldID = playerLocation.getWorld().getUID();
        return new ChunkCoordinate(x, z, worldID);
    }

    private static void applyBuffs(Player player, int level) {
        // Apply the speed effect to the player
        switch (level) {
            case 1 -> player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 1, true, false));
            case 2 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 1, true, false));
            }
            case 3 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 2, true, false));
            }
            case 4 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, true, false));
            }
            case 5 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 1, true, false));
            }
            case 6 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1, true, false));
            }
            case 7 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 3, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 1, true, false));
            }
            default -> {
            }
            // No buffs for other levels
        }
    }

    private static void applyDebuffs(Player player, int level) {
        switch (level) {
            case 1 -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1, true, false));
            case 2 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 1, true, false));
            }
            case 3 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 2, true, false));
            }
            case 4 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, true, false));
            }
            case 5 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 40, 3, true, false));
            }
            case 6 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 40, 3, true, false));
            }
            case 7 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 3, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 1, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 40, 3, true, false));
            }
            default -> {
            }
            // No buffs for other levels
        }
    }
}
