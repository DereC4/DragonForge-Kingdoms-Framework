package io.github.derec4.dragonforgekingdoms.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
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
    private final BlockPos spawnPoint;

    public CustomSoldier(Level world, UUID kingdomID, BlockPos spawnPoint) {
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
        this.spawnPoint = spawnPoint;

        setDropChance();

        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.targetSelector.addGoal(1, new TargetNonFactionPlayersGoal<>(this, kingdomID));
        this.goalSelector.addGoal(2, new ReturnToPointGoal(this, spawnPoint));

        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
        diamondSword.enchant(Enchantments.SHARPNESS, 1);
        this.setItemSlot(EquipmentSlot.MAINHAND, diamondSword);
    }

    private void setDropChance() {
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
        this.setDropChance(EquipmentSlot.LEGS, 0.0F);
        this.setDropChance(EquipmentSlot.FEET, 0.0F);
    }
}