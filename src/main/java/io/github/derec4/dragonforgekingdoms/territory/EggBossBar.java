package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class KingdomBossBarListener implements Listener {

    private final KingdomManager kingdomManager;

    public KingdomBossBarListener(KingdomManager kingdomManager) {
        this.kingdomManager = kingdomManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (kingdomManager.isPlayerMapped(playerUUID)) {
            int chunkX = player.getLocation().getChunk().getX();
            int chunkZ = player.getLocation().getChunk().getZ();
            UUID worldUUID = player.getWorld().getUID();
            ChunkCoordinate playerChunkCoord = new ChunkCoordinate(chunkX, chunkZ, worldUUID);
            UUID kingdomUUID = kingdomManager.getKingdomByChunk(playerChunkCoord);

            if (kingdomUUID != null) {
                Kingdom kingdom = kingdomManager.getKingdomFromID(kingdomUUID);
                displayEggHealthBossBar(player, kingdom);
            }
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
}
