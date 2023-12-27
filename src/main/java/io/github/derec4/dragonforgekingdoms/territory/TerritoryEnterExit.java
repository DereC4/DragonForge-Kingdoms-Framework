package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerritoryEnterExit implements Listener {
    private final KingdomManager kingdomManager;
    private final Map<UUID, ChunkCoordinate> playerLastChunk;

    public TerritoryEnterExit(KingdomManager kingdomManager) {
        this.kingdomManager = kingdomManager;
        this.playerLastChunk = new HashMap<>();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ChunkCoordinate currentChunk = new ChunkCoordinate(event.getTo().getChunk().getX(),
                event.getTo().getChunk().getX(), event.getTo().getWorld().getUID());

        // Check if the player has moved to a new chunk
        if (!currentChunk.equals(playerLastChunk.getOrDefault(player.getUniqueId(), null))) {
            playerLastChunk.put(player.getUniqueId(), currentChunk);

            // Check if the player is in occupied territory
            UUID kingdomUUID = kingdomManager.getKingdomByChunk(currentChunk);
            if (kingdomUUID != null) {
                // Display title for occupied territory
                player.sendTitle("Occupied Territory", "You are in the territory of a kingdom!", 10, 70, 20);
            }
        }
    }
}
