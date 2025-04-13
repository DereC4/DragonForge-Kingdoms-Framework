package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class EntityDeathListener implements Listener {
    private final KingdomManager kingdomManager;

    public EntityDeathListener() {
        this.kingdomManager = KingdomManager.getInstance();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof WitherSkeleton) {
            UUID kingdomID = ((CustomGuard) entity).getKingdomID();
            Kingdom kingdom = kingdomManager.getKingdomFromID(kingdomID);

            if (kingdom != null) {
                kingdom.decrementMobCount();
            }
        }
    }
}