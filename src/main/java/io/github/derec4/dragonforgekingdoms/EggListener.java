package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class EggListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            Player player = event.getPlayer();
            if (clickedBlock != null && isKingdomEgg(clickedBlock)) {
                KingdomManager kingdomManager = KingdomManager.getInstance();
                Chunk chunk = clickedBlock.getChunk();
//                byte[] encodedData = loadEncodedData(chunk); // HOW????

                // Decode and check ownership
                UUID uuid = null;
                if (!kingdomManager.getPlayerKingdom(player.getUniqueId()).equals(uuid)) {
                    // Implement logic on damaging the egg
                }
            }
        }
    }

    private boolean isKingdomEgg(Block block) {
        // Implement your logic to check if the block is the kingdom egg
        // This could involve checking block type, metadata, etc.
        // For simplicity, let's assume it's a specific material, e.g., DIAMOND_BLOCK
        return block.getType() == Material.DIAMOND_BLOCK;
    }
}
