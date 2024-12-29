package io.github.derec4.dragonforgekingdoms;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;

public class CustomWitherSkeleton extends WitherSkeleton {

    public CustomWitherSkeleton(Level world) {
        super(EntityType.WITHER_SKELETON, world);
        this.collides = false;
        this.expToDrop = 0;
        this.goalSelector = new GoalSelector(world.getProfilerSupplier());
        this.targetSelector = new GoalSelector(world.getProfilerSupplier());
        this.setInvulnerable(true);
        this.setCanPickUpLoot(false);
        this.setAggressive(false);
        this.setCustomNameVisible(true);
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Cow.class, true));
    }

}