package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class TargetNonFactionPlayersGoal extends TargetGoal {
    private final CustomWitherSkeleton customWitherSkeleton;
    private final UUID kingdomID;
    private final KingdomManager kingdomManager;

    public TargetNonFactionPlayersGoal(CustomWitherSkeleton customWitherSkeleton, UUID kingdomID) {
        super(customWitherSkeleton, false);        System.out.println("TEMPTEMPTEMP" + kingdomID);

        this.customWitherSkeleton = customWitherSkeleton;
        this.kingdomID = kingdomID;
        this.kingdomManager = KingdomManager.getInstance();
    }

    @Override
    public boolean canUse() {
        for (Player player : this.customWitherSkeleton.level().players()) {
            UUID playerKingdomID = kingdomManager.getPlayerKingdom(player.getUUID()).getID();
            // CHANGE LATER TMEP TEMPT EMPTEMPE
            if (kingdomID.equals(playerKingdomID)) {
                this.customWitherSkeleton.setTarget(player);
                return true;
            }
        }
        return false;
    }
}