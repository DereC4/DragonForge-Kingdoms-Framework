package io.github.derec4.dragonforgekingdoms.entity.goals;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.UUID;

public class SoldierTargetingGoal<T extends Mob> extends TargetGoal {
    private final T customMob;
    private final UUID kingdomID;
    private final KingdomManager kingdomManager;
    private static final double MAX_TRACKING_RANGE = 16.0;

    public SoldierTargetingGoal(T customMob, UUID kingdomID) {
        super(customMob, false);
        this.customMob = customMob;
        this.kingdomID = kingdomID;
        this.kingdomManager = KingdomManager.getInstance();
    }

    @Override
    public boolean canUse() {
        Player closestPlayer = null;
        double closestDistance = MAX_TRACKING_RANGE;

        for (Player player : this.customMob.level().players()) {
            if (player.isCreative() || player.isSpectator()) continue;

            Kingdom playerKingdom = kingdomManager.getPlayerKingdom(player.getUUID());
            UUID playerKingdomID = (playerKingdom != null) ? playerKingdom.getID() : null;
            BlockPos targetPosition = player.blockPosition();
            UUID worldID = this.customMob.level().getWorld().getUID();

            if ((playerKingdomID == null || !kingdomID.equals(playerKingdomID)) &&
                    kingdomManager.isWithinKingdomTerritory(playerKingdomID, targetPosition, worldID)) {
                double distance = player.position().distanceTo(this.customMob.position());

                if (distance < closestDistance) {
                    closestPlayer = player;
                    closestDistance = distance;
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
        if (distanceToTarget > MAX_TRACKING_RANGE) return false;

        // Check if the target is within the kingdom's territory
        BlockPos targetPosition = this.customMob.getTarget().blockPosition();
        UUID worldID = this.customMob.level().getWorld().getUID();

        return kingdomManager.isWithinKingdomTerritory(kingdomID, targetPosition, worldID);
    }

}