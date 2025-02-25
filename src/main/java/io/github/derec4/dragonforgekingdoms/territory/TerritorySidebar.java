package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;

import java.util.UUID;

public class TerritorySidebar implements Listener {
    private final KingdomManager kingdomManager;

    public TerritorySidebar() {
        this.kingdomManager = KingdomManager.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ChunkCoordinate chunkCoordinate = PlayerUtils.getChunk(player.getLocation());
        UUID uuid = kingdomManager.getKingdomByChunk(chunkCoordinate);

        PlayerUtils.setSidebar(player,uuid);
    }
}
