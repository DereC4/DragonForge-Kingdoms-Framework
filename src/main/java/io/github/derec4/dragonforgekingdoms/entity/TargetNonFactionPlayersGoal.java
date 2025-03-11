package io.github.derec4.dragonforgekingdoms.entity;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class TargetNonFactionPlayersGoal extends NearestAttackableTargetGoal<Player> {

    private final UUID factionId;

    public TargetNonFactionPlayersGoal(CustomWitherSkeleton mob, UUID factionId) {
        super(mob, Player.class, true);
        this.factionId = factionId;
    }

    @Override
    public boolean canUse() {
        Player nearestPlayer = this.mob.level().getNearestPlayer(this.mob, 16.0D);

        if (nearestPlayer == null) {
            return false;
        }

        KingdomManager kingdomManager = KingdomManager.getInstance();
        Kingdom playerKingdom = kingdomManager.getPlayerKingdom(nearestPlayer.getUUID());

        return playerKingdom == null || !playerKingdom.getID().equals(factionId);
    }
}