package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.UUID;

public class TargetNonFactionPlayersGoal<T extends Mob> extends TargetGoal {
    private final T customMob;
    private final UUID kingdomID;
    private final KingdomManager kingdomManager;

    public TargetNonFactionPlayersGoal(T customMob, UUID kingdomID) {
        super(customMob, false);
        System.out.println("TEMPTEMPTEMP " + kingdomID);
        this.customMob = customMob;
        this.kingdomID = kingdomID;
        this.kingdomManager = KingdomManager.getInstance();
    }

    @Override
    public boolean canUse() {
        for (Player player : this.customMob.level().players()) {
            UUID playerKingdomID = kingdomManager.getPlayerKingdom(player.getUUID()).getID();
            // CHANGE LATER TEMP TEMP TEMP
            if (!player.isCreative() && !player.isSpectator() && kingdomID.equals(playerKingdomID)) {
                this.customMob.setTarget(player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                return true;
            }
        }
        return false;
    }
}