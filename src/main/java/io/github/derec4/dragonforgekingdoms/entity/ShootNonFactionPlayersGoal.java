package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.UUID;

public class ShootNonFactionPlayersGoal extends Goal {
    private final CustomArcher archer;
    private final UUID kingdomID;
    private final KingdomManager kingdomManager;

    public ShootNonFactionPlayersGoal(CustomArcher archer, UUID kingdomID) {
        this.archer = archer;
        this.kingdomID = kingdomID;
        this.kingdomManager = KingdomManager.getInstance();
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        Level level = archer.level();
        for (Player player : level.players()) {
            Kingdom playerKingdom = kingdomManager.getPlayerKingdom(player.getUUID());
            UUID playerKingdomID = (playerKingdom != null) ? playerKingdom.getID() : null;

            if (!player.isCreative() && !player.isSpectator() && (!kingdomID.equals(playerKingdomID))) {
                archer.setTarget(player);
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

        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }
}