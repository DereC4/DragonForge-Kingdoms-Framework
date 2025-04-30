package io.github.derec4.dragonforgekingdoms.kingdom.egg;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class EggEntityExplosionListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
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
    }

    private EggData getKingdomEgg(Block block) {
        Chunk chunk = block.getChunk();
        EggData data = EggData.getChunkEggData(chunk);

        if (data == null) {
            return null;
        }
        return (data.getX() == block.getX() && data.getY() == block.getY() && data.getZ() == block.getZ())
                ? data : null;
    }
}