package io.github.derec4.dragonforgekingdoms.entity;

import lombok.Getter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.UUID;

@Getter
public class CustomSoldier extends Skeleton {

    private final UUID kingdomID;

    public CustomSoldier(Level world, UUID kingdomID) {
        super(EntityType.SKELETON, world);
        this.kingdomID = kingdomID;
        this.collides = true;
        this.expToDrop = 0;
        this.goalSelector = new GoalSelector(world.getProfilerSupplier());
        this.targetSelector = new GoalSelector(world.getProfilerSupplier());
        this.setInvulnerable(false);
        this.setCanPickUpLoot(false);
        this.setAggressive(false);
        this.setCustomNameVisible(true);
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.targetSelector.addGoal(1, new TargetNonFactionPlayersGoal<>(this, kingdomID));

        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
        diamondSword.enchant(Enchantments.SHARPNESS, 3);
        this.setItemSlot(EquipmentSlot.MAINHAND, diamondSword);
    }
}