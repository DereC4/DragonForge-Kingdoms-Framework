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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class EggBossBar implements Listener {

    private final Map<UUID, BossBar> playerBossBars;
    private final KingdomManager kingdomManager;

    public EggBossBar() {
        this.playerBossBars = new HashMap<>();
        this.kingdomManager = KingdomManager.getInstance();
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
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
        int eggHealth = kingdom.getHealth();
        BarColor barColor;
        UUID playerKingdomUUID = KingdomManager.getInstance().getPlayerKingdom(player.getUniqueId()).getID();
        BossBar bossBar = playerBossBars.get(player.getUniqueId());

        if (bossBar == null) {
            if (playerKingdomUUID.equals(kingdom.getID())) {
                barColor = BarColor.GREEN;
            } else {
                boolean isAllied = kingdomManager.getPlayerKingdom(player.getUniqueId()).getID().equals(kingdom.getID());
                if (isAllied) {
                    // Allied kingdom
                    barColor = BarColor.BLUE;
                } else {
                    // Enemy kingdom
                    barColor = BarColor.RED;
                }
            }
            bossBar = Bukkit.createBossBar(
                    ChatColor.GREEN + "Egg Health: " + eggHealth,
                    barColor,
                    BarStyle.SOLID
            );
            bossBar.setProgress((double) (Math.max(eggHealth, 0)) / kingdom.getMaxHealth());
            bossBar.addPlayer(player);
            playerBossBars.put(player.getUniqueId(), bossBar);
        } else {
//            Bukkit.getLogger().info("Updating bossbar for " + player.getName());
            bossBar.setTitle(ChatColor.GREEN + "Egg Health: " + eggHealth);
            bossBar.setProgress((double) (Math.max(eggHealth, 0)) / kingdom.getMaxHealth());
        }
    }

    private void removeBossBar(Player player) {
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removePlayer(player);
            bossBar.setVisible(false);
        }
    }
}