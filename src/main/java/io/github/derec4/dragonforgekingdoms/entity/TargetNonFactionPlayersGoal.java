package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
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
        this.customMob = customMob;
        this.kingdomID = kingdomID;
        this.kingdomManager = KingdomManager.getInstance();
    }

    @Override
    public boolean canUse() {
        for (Player player : this.customMob.level().players()) {
            Kingdom playerKingdom = kingdomManager.getPlayerKingdom(player.getUUID());
            UUID playerKingdomID = (playerKingdom != null) ? playerKingdom.getID() : null;

            if (!player.isCreative() && !player.isSpectator() && (playerKingdomID == null || !kingdomID.equals(playerKingdomID))) {
                this.customMob.setTarget(player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                return true;
            }
        }
        return false;
    }
}