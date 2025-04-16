package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.util.EntityTags;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class EntityDeathListener implements Listener {
    private final KingdomManager kingdomManager;

    public EntityDeathListener() {
        this.kingdomManager = KingdomManager.getInstance();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        PersistentDataContainer persistentDataContainer = entity.getPersistentDataContainer();

        if (persistentDataContainer.has(EntityTags.MOB_TYPE_KEY, PersistentDataType.INTEGER)) {
            int mobType = persistentDataContainer.get(EntityTags.MOB_TYPE_KEY, PersistentDataType.INTEGER);

            switch(mobType) {
                case 0 -> Bukkit.getLogger().info("A CustomGuard has died.");
                case 1 -> Bukkit.getLogger().info("A CustomSoldier has died.");
                case 2 -> Bukkit.getLogger().info("A CustomArcher has died.");
                default -> Bukkit.getLogger().info("Unknown custom mob type.");
            }
        }
        System.out.println("REACHED ONE");
        if (entity instanceof CustomGuard guard) {
            System.out.println("REACHED THREE");

            UUID kingdomID = guard.getKingdomID();
            Kingdom kingdom = kingdomManager.getKingdomFromID(kingdomID);
            if (kingdom != null) {
                System.out.println("REACHED TWO");

                kingdom.decrementMobCount();
            }
        }

        if (entity instanceof CustomSoldier soldier) {
            UUID kingdomID = soldier.getKingdomID();
            Kingdom kingdom = kingdomManager.getKingdomFromID(kingdomID);
            if (kingdom != null) {
                kingdom.decrementMobCount();
            }
        }

        if (entity instanceof CustomArcher archer) {
            UUID kingdomID = archer.getKingdomID();
            Kingdom kingdom = kingdomManager.getKingdomFromID(kingdomID);
            if (kingdom != null) {
                kingdom.decrementMobCount();
            }
        }
    }
}