package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;
import java.util.UUID;

public class RangedTargetNonFactionPlayersGoal extends Goal {
    private final CustomArcher archer;
    private final UUID kingdomID;
    private final KingdomManager kingdomManager;

    public RangedTargetNonFactionPlayersGoal(CustomArcher archer, UUID kingdomID) {
        this.archer = archer;
        this.kingdomID = kingdomID;
        this.kingdomManager = KingdomManager.getInstance();
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        for (Player player : this.archer.level().players()) {
            Kingdom playerKingdom = kingdomManager.getPlayerKingdom(player.getUUID());
            UUID playerKingdomID = (playerKingdom != null) ? playerKingdom.getID() : null;

            if (!player.isCreative() && !player.isSpectator() && (playerKingdomID == null || !kingdomID.equals(playerKingdomID))) {
                archer.setTarget(player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        LivingEntity target = archer.getTarget();

        if (target != null && target instanceof Player) {
            archer.performRangedAttack(target, 1.0F);
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = archer.getTarget();

        if (target == null || !(target instanceof Player player)) {
            return false;
        }

        Kingdom playerKingdom = kingdomManager.getPlayerKingdom(player.getUUID());
        UUID playerKingdomID = (playerKingdom != null) ? playerKingdom.getID() : null;

        if (player.isCreative() || player.isSpectator() || (playerKingdomID != null && kingdomID.equals(playerKingdomID))) {
            archer.setTarget(null);
            return false;
        }

        return true;
    }
}