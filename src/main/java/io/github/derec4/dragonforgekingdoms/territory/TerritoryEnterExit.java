package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class TerritoryEnterExit implements Listener {
    private final KingdomManager kingdomManager;

    public TerritoryEnterExit() {
        this.kingdomManager = KingdomManager.getInstance();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ChunkCoordinate fromChunk = new ChunkCoordinate(
                event.getFrom().getChunk().getX(),
                event.getFrom().getChunk().getZ(),
                event.getFrom().getWorld().getUID()
        );
        ChunkCoordinate toChunk = new ChunkCoordinate(
                event.getTo().getChunk().getX(),
                event.getTo().getChunk().getZ(),
                event.getTo().getWorld().getUID()
        );

        // Check if the player moved to a new chunk
        if (fromChunk.equals(toChunk)) {
            return;
        }

        UUID fromKingdomUUID = kingdomManager.getKingdomByChunk(fromChunk);
        UUID toKingdomUUID = kingdomManager.getKingdomByChunk(toChunk);

        if (fromKingdomUUID == null && toKingdomUUID == null) {
            return;
        }

        String message;

        // 12/10/2024 remember to use .equals for object comparisons lol, idk how we got away with this for so long
        if (toKingdomUUID == null) {
            message = ChatColor.GREEN + "Wilderness";
        } else if (fromKingdomUUID == null || !fromKingdomUUID.equals(toKingdomUUID)) {
            Kingdom toKingdom = kingdomManager.getKingdomFromID(toKingdomUUID);
            message = ChatColor.BLUE + toKingdom.getName();
        } else {
            // Still in the same kingdom
            return;
        }

        player.sendTitle(message, "", 10, 70, 20);
        Bukkit.getServer().getConsoleSender().sendMessage("Title message to " + player.getName() + ": " + message);
    }
}
