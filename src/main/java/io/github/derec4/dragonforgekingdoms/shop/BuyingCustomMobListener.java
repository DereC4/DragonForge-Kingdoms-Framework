
package io.github.derec4.dragonforgekingdoms.shop;

import com.gypopo.economyshopgui.api.events.PostTransactionEvent;
import com.gypopo.economyshopgui.api.objects.ShopItem;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

public class BuyingCustomMobListener implements Listener {

    private final KingdomManager kingdomManager;

    public BuyingCustomMobListener(KingdomManager kingdomManager) {
        this.kingdomManager = kingdomManager;
    }

    @EventHandler
    public void onPostTransaction(PostTransactionEvent event) {
        Player player = event.getPlayer();
        ShopItem shopItem = event.getShopItem();

        if (shopItem.getShopSection().equalsIgnoreCase("EGGS")) {
            Kingdom kingdom = kingdomManager.getPlayerKingdom(player.getUniqueId());
            if (kingdom != null) {
                double cost = event.getTotalCost();

                if (kingdom.getWealth() >= cost) {
                    kingdom.giveWealth((int) -cost);
                    player.sendMessage("The cost of the spawn egg has been deducted from your kingdom's wealth.");
                } else {
                    event.setCancelled(true);
                    player.sendMessage("Your kingdom does not have enough wealth to purchase this item.");
                }
            } else {
                event.setCancelled(true);
                player.sendMessage("You are not part of a kingdom.");
            }
        }
    }
}