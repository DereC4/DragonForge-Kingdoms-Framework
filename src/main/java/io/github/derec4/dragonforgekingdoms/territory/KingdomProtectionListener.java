package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public class KingdomProtectionListener implements Listener {

    private final KingdomManager kingdomManager; // Assuming you have a KingdomManager instance

    public KingdomProtectionListener() {
        this.kingdomManager = KingdomManager.getInstance();
    }

//    @EventHandler
//    public void onBlockBreak(BlockBreakEvent event) {
//        Player player = event.getPlayer();
//        ChunkCoordinate chunk = new ChunkCoordinate(event.getBlock().getChunk().getX(),
//                event.getBlock().getChunk().getZ(), event.getBlock().getWorld().getUID());
//
//        if (!canPlayerModifyChunk(player, chunk)) {
//            event.setCancelled(true);
//            player.sendMessage("You can't break blocks in this kingdom's territory!");
//        }
//    }

//    @EventHandler
//    public void onPlayerInteract(PlayerInteractEvent event) {
//        Player player = event.getPlayer();
//        ChunkCoordinate chunk = new ChunkCoordinate(event.getClickedBlock().getChunk().getX(),
//                event.getClickedBlock().getChunk().getZ(), event.getClickedBlock().getWorld().getUID());
//
//        if (!canPlayerModifyChunk(player, chunk)) {
//            event.setCancelled(true);
//            player.sendMessage("You can't interact with blocks in this kingdom's territory!");
//        }
//    }

    private boolean canPlayerModifyChunk(Player player, ChunkCoordinate chunk) {
        UUID playerUUID = player.getUniqueId();
        UUID playerKingdomUUID = kingdomManager.getPlayerKingdom(playerUUID).getID();
        UUID chunkKingdomUUID = kingdomManager.getKingdomByChunk(chunk);
        if(chunkKingdomUUID == null) {
            return true;
        }

        // Not wild territory; is owned by kingdom. Now check if player is homeless
        if(playerKingdomUUID == null) {
            return false;
        }
        return playerKingdomUUID.equals(chunkKingdomUUID);
    }
}
