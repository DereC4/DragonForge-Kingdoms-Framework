package io.github.derec4.dragonforgekingdoms.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ReturnToPointGoal extends Goal {
    private final PathfinderMob mob;
    private final BlockPos spawnPoint;
    private final double speed;

    public ReturnToPointGoal(PathfinderMob mob, BlockPos spawnPoint) {
        this.mob = mob;
        this.spawnPoint = spawnPoint;
        this.speed = 1.0D;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !mob.blockPosition().closerThan(spawnPoint, 5.0D);
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ(), speed);
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.blockPosition().closerThan(spawnPoint, 2.0D);
    }
}