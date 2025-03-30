package io.github.derec4.dragonforgekingdoms.entity;


import lombok.Getter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.UUID;
package io.github.derec4.dragonforgekingdoms.listeners;

import com.gypopo.economyshopgui.api.events.PostTransactionEvent;
import com.gypopo.economyshopgui.api.objects.ShopItem;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

public class EconomyShopGUIListener implements Listener {

    private final KingdomManager kingdomManager;

    public EconomyShopGUIListener(KingdomManager kingdomManager) {
        this.kingdomManager = kingdomManager;
    }

    @EventHandler
    public void onPostTransaction(PostTransactionEvent event) {
        Player player = event.getPlayer();
        ShopItem shopItem = event.getShopItem();

        // Check if the item is from the "EGGS" section
        if (shopItem.getShopSection().equalsIgnoreCase("EGGS")) {
            Kingdom kingdom = kingdomManager.getPlayerKingdom(player.getUniqueId());
            if (kingdom != null) {
                double cost = event.getTotalCost();

                // Check if the kingdom has enough wealth
                if (kingdom.getWealth() >= cost) {
                    kingdom.giveWealth((int) -cost); // Deduct cost from kingdom wealth
                    player.sendMessage("The cost of the spawn egg has been deducted from your kingdom's wealth.");
                } else {
                    event.setCancelled(true); // Cancel the transaction
                    player.sendMessage("Your kingdom does not have enough wealth to purchase this item.");
                }
            } else {
                event.setCancelled(true); // Cancel the transaction
                player.sendMessage("You are not part of a kingdom.");
            }
        }
    }
}
@Getter
public class CustomGuard extends WitherSkeleton {

    private final UUID kingdomID;

    public CustomGuard(Level world, UUID kingdomID) {
        super(EntityType.WITHER_SKELETON, world);
        this.kingdomID = kingdomID;
        this.collides = true;
        this.expToDrop = 0;
        this.goalSelector = new GoalSelector(world.getProfilerSupplier());
        this.targetSelector = new GoalSelector(world.getProfilerSupplier());
        this.setInvulnerable(false);
        this.setCanPickUpLoot(false);
        this.setAggressive(false);
        this.setCustomNameVisible(true);
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.targetSelector.addGoal(1, new TargetNonFactionPlayersGoal<>(this, kingdomID));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
        diamondSword.enchant(Enchantments.SHARPNESS, 3);
        this.setItemSlot(EquipmentSlot.MAINHAND, diamondSword);

    }
}