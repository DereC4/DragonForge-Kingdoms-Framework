package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.KingdomManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PlayerEffects {
    public static void applyEffects(Player player) {
        ChunkCoordinate playerChunk = getPlayerCurrentChunk(player);

        if (playerChunk != null) {
            KingdomManager km = KingdomManager.getInstance();
            UUID playerUUID = player.getUniqueId();
            UUID kingdomUUID = km.getKingdomByChunk(playerChunk);

            if (kingdomUUID != null && KingdomManager.getInstance().isPlayerMapped(playerUUID)) {
                UUID playerKingdomUUID = KingdomManager.getInstance().getPlayerKingdom(playerUUID).getID();

                if (kingdomUUID.equals(playerKingdomUUID)) {
                    // Player is in their own kingdom
                    applySpeedEffect(player);
                } else {
                    // Player is in an enemy kingdom
                    applyMiningFatigueEffect(player);
                }
            }
        }
    }

    private static ChunkCoordinate getPlayerCurrentChunk(Player player) {
        Location playerLocation = player.getLocation();
        int x = playerLocation.getBlockX() >> 4;
        int z = playerLocation.getBlockZ() >> 4;
        UUID worldID = playerLocation.getWorld().getUID();
        return new ChunkCoordinate(x, z, worldID);
    }

    private static void applySpeedEffect(Player player) {
        // Apply the speed effect to the player
        player.removePotionEffect(PotionEffectType.SLOW);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false));
    }

    private static void applyMiningFatigueEffect(Player player) {
        // Apply the mining fatigue effect to the player
        player.removePotionEffect(PotionEffectType.SPEED);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 1, true, false));
    }

}
