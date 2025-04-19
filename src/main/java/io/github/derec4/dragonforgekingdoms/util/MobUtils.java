package io.github.derec4.dragonforgekingdoms.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class MobUtils {
    public static void despawnAllCustomKingdomMobs() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                PersistentDataContainer dataContainer = entity.getPersistentDataContainer();

                if (dataContainer.has(EntityTags.MOB_TYPE_KEY, PersistentDataType.INTEGER) &&
                        dataContainer.has(EntityTags.KINGDOM_ID_KEY, PersistentDataType.STRING)) {
                    entity.remove();
                }
            }
        }

        Bukkit.getLogger().info("All custom kingdom mobs have been despawned.");
    }

    /**
     * Despawns all custom kingdom mobs that match the provided kingdomID.
     *
     * @param kingdomID The UUID of the kingdom whose mobs should be despawned.
     */
    public static void despawnCustomKingdomMobs(UUID kingdomID) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                PersistentDataContainer dataContainer = entity.getPersistentDataContainer();

                if (dataContainer.has(EntityTags.KINGDOM_ID_KEY, PersistentDataType.STRING)) {
                    String entityKingdomID = dataContainer.get(EntityTags.KINGDOM_ID_KEY, PersistentDataType.STRING);

                    if (kingdomID.toString().equals(entityKingdomID)) {
                        entity.remove();
                    }
                }
            }
        }

        Bukkit.getLogger().info("All custom kingdom mobs for kingdom " + kingdomID + " have been despawned.");
    }
}

