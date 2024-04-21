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

    private final Map<ChunkCoordinate, BossBar> chunkBossBars;
    private final KingdomManager kingdomManager;

    public EggBossBar() {
        this.chunkBossBars = new HashMap<>();
        this.kingdomManager = KingdomManager.getInstance();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        ChunkCoordinate playerChunkCoord = new ChunkCoordinate(
                player.getLocation().getChunk().getX(),
                player.getLocation().getChunk().getZ(),
                player.getWorld().getUID()
        );

        UUID kingdomUUID = kingdomManager.getKingdomByChunk(playerChunkCoord);
        Kingdom kingdom = kingdomUUID != null ? kingdomManager.getKingdomFromID(kingdomUUID) : null;

        if (kingdom != null && kingdom.getEggData() != null) {
            ChunkCoordinate eggChunkCoord = new ChunkCoordinate(kingdom.getEggData().getX(),
                    kingdom.getEggData().getZ(), UUID.fromString(kingdom.getEggData().getWorld()));
            if (eggChunkCoord.equals(playerChunkCoord)) {
                displayOrRefreshBossBar(player, kingdom, eggChunkCoord);
            } else {
                removeBossBar(player, eggChunkCoord);
            }
        } else {
            removeBossBar(player, null);
        }
    }

    private void displayOrRefreshBossBar(Player player, Kingdom kingdom, ChunkCoordinate eggChunkCoord) {
        BossBar bossBar = chunkBossBars.get(eggChunkCoord);

        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(
                    ChatColor.GREEN + "Egg Health: " + kingdom.getEggData().getHealth(),
                    determineBarColor(player, kingdom),
                    BarStyle.SOLID
            );
            bossBar.setProgress((double) kingdom.getEggData().getHealth() / kingdom.getMaxHealth());
            bossBar.addPlayer(player);
            chunkBossBars.put(eggChunkCoord, bossBar);
        } else {
            bossBar.setColor(determineBarColor(player, kingdom));
            bossBar.setProgress((double) kingdom.getEggData().getHealth() / kingdom.getMaxHealth());
        }
    }

    private BarColor determineBarColor(Player player, Kingdom kingdom) {
        UUID playerKingdomUUID = kingdomManager.getPlayerKingdom(player.getUniqueId()).getID();
        UUID kingdomUUID = kingdom.getID();
        if (playerKingdomUUID.equals(kingdomUUID)) {
            return BarColor.GREEN; // Player's kingdom
        } else {
            return BarColor.RED; // Enemy kingdom
        }
    }

    private void removeBossBar(Player player, ChunkCoordinate eggChunkCoord) {
        if (eggChunkCoord != null) {
            BossBar bossBar = chunkBossBars.get(eggChunkCoord);
            if (bossBar != null) {
                bossBar.removePlayer(player);
                if (bossBar.getPlayers().isEmpty()) {
                    bossBar.setVisible(false);
                    chunkBossBars.remove(eggChunkCoord);
                }
            }
        } else {
            // Remove all boss bars associated with the player
            BossBar bossBar = chunkBossBars.remove(player.getUniqueId());
            if (bossBar != null) {
                bossBar.removePlayer(player);
                bossBar.setVisible(false);
            }
        }
    }
}
