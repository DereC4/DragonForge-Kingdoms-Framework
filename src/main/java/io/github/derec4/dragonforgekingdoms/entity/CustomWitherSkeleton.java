package io.github.derec4.dragonforgekingdoms.entity;


import lombok.Getter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;

import java.util.UUID;

@Getter
public class CustomWitherSkeleton extends WitherSkeleton {

    private final UUID kingdomID;

    public CustomWitherSkeleton(Level world, UUID kingdomID) {
        super(EntityType.WITHER_SKELETON, world);
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
        this.targetSelector.addGoal(1, new TargetNonFactionPlayersGoal(this, kingdomID));
    }

}