package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Iterator;
import java.util.UUID;

public class EggBossBar implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        KingdomManager kingdomManager = KingdomManager.getInstance();
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        int chunkX = player.getLocation().getChunk().getX();
        int chunkZ = player.getLocation().getChunk().getZ();
        UUID worldUUID = player.getWorld().getUID();
        ChunkCoordinate playerChunkCoord = new ChunkCoordinate(chunkX, chunkZ, worldUUID);
        UUID kingdomUUID = kingdomManager.getKingdomByChunk(playerChunkCoord);

        if (kingdomUUID != null) {
            Kingdom kingdom = kingdomManager.getKingdomFromID(kingdomUUID);
            displayEggHealthBossBar(player, kingdom);
        } else {
            removeBossBar(player);
        }
    }

    private void displayEggHealthBossBar(Player player, Kingdom kingdom) {
        int eggHealth = (int) kingdom.getEggData().getHealth();
        BossBar bossBar = Bukkit.createBossBar(
                ChatColor.GREEN + "Egg Health: " + eggHealth,
                BarColor.GREEN,
                BarStyle.SOLID
        );
        bossBar.setProgress((double) eggHealth / kingdom.getMaxHealth());
        bossBar.addPlayer(player);
    }

    private void removeBossBar(Player player) {
        // Retrieve any existing boss bar from the player and remove it
        for (Iterator<KeyedBossBar> it = Bukkit.getBossBars(); it.hasNext(); ) {
            BossBar bossBar = it.next();
            if (bossBar.getPlayers().contains(player)) {
                bossBar.removePlayer(player);
                break;
            }
        }
    }
}
