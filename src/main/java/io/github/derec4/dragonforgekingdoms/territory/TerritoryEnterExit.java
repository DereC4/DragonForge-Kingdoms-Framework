package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;
import java.util.UUID;

public class TerritoryEnterExit implements Listener {
    private final KingdomManager kingdomManager;

    public TerritoryEnterExit() {
        this.kingdomManager = KingdomManager.getInstance();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ChunkCoordinate fromChunk = PlayerUtils.getChunk(event.getFrom());
        ChunkCoordinate toChunk = PlayerUtils.getChunk(Objects.requireNonNull(event.getTo()));

        // Check if the player moved to a new chunk
        if (fromChunk.equals(toChunk)) {
            return;
        }

        UUID fromKingdomUUID = kingdomManager.getKingdomByChunk(fromChunk);
        UUID toKingdomUUID = kingdomManager.getKingdomByChunk(toChunk);

        if (fromKingdomUUID == null && toKingdomUUID == null) {
            return;
        }

        String titleMessage;
        String subtitleMessage = "";

        // 12/10/2024 remember to use .equals for object comparisons lol, idk how we got away with this for so long
        if (toKingdomUUID == null) {
            titleMessage = ChatColor.GREEN + "Wilderness";
        } else if (fromKingdomUUID == null || !fromKingdomUUID.equals(toKingdomUUID)) {
            Kingdom toKingdom = kingdomManager.getKingdomFromID(toKingdomUUID);
            titleMessage = ChatColor.BLUE + toKingdom.getName();
            subtitleMessage = ChatColor.GOLD + "Wealth: " + toKingdom.getWealth();
        } else {
            // Still in the same kingdom
            return;
        }

        player.sendTitle(titleMessage, subtitleMessage, 10, 70, 20);
        PlayerUtils.setSidebar(player,toKingdomUUID);
//        Bukkit.getServer().getConsoleSender().sendMessage("Title message to " + player.getName() + ": " + titleMessage);
    }
}
