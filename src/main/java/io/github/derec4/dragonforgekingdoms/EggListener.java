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

public class EggListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();

        if (clickedBlock == null) {
            return;
        }

        EggData eggData = getKingdomEgg(clickedBlock);

        KingdomManager kingdomManager = KingdomManager.getInstance();
//                byte[] encodedData = loadEncodedData(chunk); // HOW????

                // Decode and check ownership
    }

    

    private EggData getKingdomEgg(Block block) {

        if(block.getType() != Material.DRAGON_EGG) {
            return null;
        }

        Chunk chunk = block.getChunk();
        EggData data = EggData.getChunkEggData(chunk);

        if(data == null) {
            return null;
        }

        return (data.getX() == block.getX() && data.getY() == block.getY() && data.getZ() == block.getZ())
                ? data : null;
    }
}
