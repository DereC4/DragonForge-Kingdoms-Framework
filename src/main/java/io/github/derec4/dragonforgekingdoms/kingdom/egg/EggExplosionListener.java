package io.github.derec4.dragonforgekingdoms.kingdom.egg;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.Iterator;

public class EggExplosionListener implements Listener {

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
//        Block explodedBlock = event.getBlock();

        // Mistake here was assuming the block explode was for each individual block omg - 4/19/2025
        Iterator<Block> iterator = event.blockList().iterator();

        while (iterator.hasNext()) {
            Block block = iterator.next();

            if (block.getType() == Material.DRAGON_EGG) {
                System.out.println("Explosion detected dragon egg");
                EggData eggData = getKingdomEgg(block);

                if (eggData != null) {
                    System.out.println("Explosion detected kingdom egg");
                    iterator.remove();
                }
            }
        }

//        if (!(explodedBlock.getType() == Material.DRAGON_EGG)) {
//            return;
//        }
    }

    private EggData getKingdomEgg(Block block) {
        Chunk chunk = block.getChunk();
        EggData data = EggData.getChunkEggData(chunk);

        if(data == null) {
            return null;
        }
//        System.out.println(data.getX());
//        System.out.println(data.getY());
//        System.out.println(data.getZ());
        return (data.getX() == block.getX() && data.getY() == block.getY() && data.getZ() == block.getZ())
                ? data : null;
    }
}