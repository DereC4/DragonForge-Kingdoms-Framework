package io.github.derec4.dragonforgekingdoms.entity.goals;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.UUID;

public class GuardTargetingGoal<T extends Mob> extends TargetGoal {
    private final T customMob;
    private final UUID kingdomID;
    private final KingdomManager kingdomManager;
    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;
    private static final double MAX_TRACKING_RANGE = 10.0;

    public GuardTargetingGoal(T customMob, UUID kingdomID, BlockPos spawnPoint) {
        super(customMob, false);
        this.customMob = customMob;
        this.kingdomID = kingdomID;
        this.kingdomManager = KingdomManager.getInstance();
        this.spawnX = spawnPoint.getX();
        this.spawnY = spawnPoint.getY();
        this.spawnZ = spawnPoint.getZ();
    }

    @Override
    public boolean canUse() {
        Player closestPlayer = null;
        double closestDistance = MAX_TRACKING_RANGE;

        for (Player player : this.customMob.level().players()) {
            if (player.isCreative() || player.isSpectator()) continue;

            Kingdom playerKingdom = kingdomManager.getPlayerKingdom(player.getUUID());
            UUID playerKingdomID = (playerKingdom != null) ? playerKingdom.getID() : null;

            if (playerKingdomID == null || !kingdomID.equals(playerKingdomID)) {
                double distanceToMob = player.position().distanceTo(this.customMob.position());
                double distanceToSpawn = player.position().distanceTo(new net.minecraft.world.phys.Vec3(spawnX, spawnY, spawnZ));

                if (distanceToMob <= MAX_TRACKING_RANGE && distanceToSpawn <= MAX_TRACKING_RANGE) {
                    closestPlayer = player;
                    closestDistance = distanceToMob;
                }
            }
        }

        if (closestPlayer != null) {
            this.customMob.setTarget(closestPlayer, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.customMob.getTarget() == null) return false;

        double distanceToTarget = this.customMob.position().distanceTo(this.customMob.getTarget().position());
        double distanceToSpawn = this.customMob.getTarget().position().distanceTo(new net.minecraft.world.phys.Vec3(spawnX, spawnY, spawnZ));

        return distanceToSpawn <= MAX_TRACKING_RANGE && distanceToTarget <= MAX_TRACKING_RANGE;
    }
}