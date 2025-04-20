package io.github.derec4.dragonforgekingdoms.territory;

import io.github.derec4.dragonforgekingdoms.EggData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public class EggExplosionListener implements Listener {

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Block explodedBlock = event.getBlock();

        if (!(explodedBlock.getType() == Material.DRAGON_EGG)) {
            return;
        }

        EggData eggData = getKingdomEgg(explodedBlock);
        if (eggData != null) {
            event.setCancelled(true);
        }

    }

    private EggData getKingdomEgg(Block block) {
        return EggData.getChunkEggData(block.getChunk());
    }
}