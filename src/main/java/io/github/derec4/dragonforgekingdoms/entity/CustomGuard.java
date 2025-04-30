package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.entity.goals.ReturnToPointGoal;
import io.github.derec4.dragonforgekingdoms.entity.goals.TargetNonFactionPlayersGoal;
import io.github.derec4.dragonforgekingdoms.util.MobUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.UUID;

@Getter
public class CustomGuard extends Zombie {

    private final UUID kingdomID;
    private final BlockPos spawnPoint;

    public CustomGuard(Level world, UUID kingdomID, BlockPos spawnPoint) {
        super(EntityType.ZOMBIE, world);
        this.kingdomID = kingdomID;
        this.collides = true;
        this.expToDrop = 0;
        this.goalSelector = new GoalSelector(world.getProfilerSupplier());
        this.targetSelector = new GoalSelector(world.getProfilerSupplier());
        this.spawnPoint = spawnPoint;
        this.setInvulnerable(false);
        this.setCanPickUpLoot(false);
        this.setAggressive(false);
        this.setCustomNameVisible(true);
        this.setPersistenceRequired(true);
        this.setCustomName(Component.literal("Guard"));

        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.targetSelector.addGoal(1, new TargetNonFactionPlayersGoal<>(this, kingdomID));
        this.goalSelector.addGoal(2, new ReturnToPointGoal(this, spawnPoint));

        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CREEPER_HEAD));
        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
        diamondSword.enchant(Enchantments.SHARPNESS, 3);
        this.setItemSlot(EquipmentSlot.MAINHAND, diamondSword);

        MobUtils.applySpawnEffects(this);
        MobUtils.applyDefenseEnchants(this);
        MobUtils.removeDrops(this);
    }
}