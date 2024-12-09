package io.github.derec4.dragonforgekingdoms;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;

public class CustomWitherSkeleton extends WitherSkeleton {

    public CustomWitherSkeleton(Level world) {
        super(EntityType.WITHER_SKELETON, world);
        this.collides = false;
        this.expToDrop = 0;
        this.setInvulnerable(true);
        this.setCanPickUpLoot(false);
        this.setAggressive(false);
        this.setCustomNameVisible(true);
    }

}