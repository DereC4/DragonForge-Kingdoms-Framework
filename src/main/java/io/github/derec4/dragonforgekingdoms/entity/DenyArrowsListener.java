package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.util.EntityTags;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DenyArrowsListener implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damagedEntity = event.getEntity();

        if (!(damagedEntity instanceof LivingEntity)) {
            return;
        }

        PersistentDataContainer persistentDataContainer = damagedEntity.getPersistentDataContainer();

        if (!persistentDataContainer.has(EntityTags.MOB_TYPE_KEY, PersistentDataType.INTEGER)) {
            return;
        }

        if (!persistentDataContainer.has(EntityTags.KINGDOM_ID_KEY, PersistentDataType.STRING)) {
            return;
        }

        if (event.getDamager() instanceof Arrow arrow) {
            if (!(arrow.getShooter() instanceof Player player)) {
                return;
            }

            // oh... we don't even have to calculate it ;-;
            double distance = player.getLocation().distance(damagedEntity.getLocation());

            if (distance > 10) {
                event.setCancelled(true);
                arrow.remove();
            }
        }
    }
}
