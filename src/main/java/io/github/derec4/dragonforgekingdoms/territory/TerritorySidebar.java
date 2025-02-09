package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;

import java.util.UUID;

public class TerritorySidebar {
    private final KingdomManager kingdomManager;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ChunkCoordinate chunkCoordinate = PlayerUtils.getChunk(player.getLocation());
        UUID uuid = kingdomManager.getKingdomByChunk(chunkCoordinate);


    }

    /**
     * Initializes the in-game sidebar as a HUD for the current kingdom the player is in
     * @param player Player to display sidebar
     * @param uuid Current kingdom the player is standing in
     */
    public void setSidebar(Player player, UUID uuid) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("sidebar", Criteria.DUMMY,ChatColor.GOLD + "Kingdom Status");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
}
