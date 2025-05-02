package io.github.derec4.dragonforgekingdoms.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
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

    public static void applySpawnEffects (LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MobEffectInstance.INFINITE_DURATION, 2));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, MobEffectInstance.INFINITE_DURATION, 1));
    }

    public static void applyDefenseEnchants(LivingEntity entity) {
        for (ItemStack armor : entity.getArmorSlots()) {
            if (armor != null) {
                armor.enchant(Enchantments.PROJECTILE_PROTECTION, 2); // Projectile Protection 2
                armor.enchant(Enchantments.FIRE_PROTECTION, 3);       // Fire Protection 3
                armor.enchant(Enchantments.FALL_PROTECTION, 5);       // Feather Falling 5
                armor.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1); // Protection 1
            }
        }
    }

    public static void removeDrops(Mob mob) {
        mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
        mob.setDropChance(EquipmentSlot.CHEST, 0.0F);
        mob.setDropChance(EquipmentSlot.LEGS, 0.0F);
        mob.setDropChance(EquipmentSlot.FEET, 0.0F);
    }
}

