package io.github.derec4.dragonforgekingdoms.entity.goals;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Random;

public class PatrolGoal extends Goal {
    private final Mob mob;
    private final Vec3 patrolCenter;
    private final double speedModifier;
    private final double patrolRadius;
    private final Random random;

    public PatrolGoal(Mob mob, Vec3 patrolCenter, double speedModifier, double patrolRadius) {
        this.mob = mob;
        this.patrolCenter = patrolCenter;
        this.speedModifier = speedModifier;
        this.patrolRadius = patrolRadius;
        this.random = new Random();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() == null && mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        double offsetX = (random.nextDouble() * 2 - 1) * patrolRadius;
        double offsetZ = (random.nextDouble() * 2 - 1) * patrolRadius;
        Vec3 patrolTarget = new Vec3(
                patrolCenter.x() + offsetX,
                patrolCenter.y(),
                patrolCenter.z() + offsetZ
        );

        mob.getNavigation().moveTo(patrolTarget.x(), patrolTarget.y(), patrolTarget.z(), speedModifier);
    }
}