
package io.github.derec4.dragonforgekingdoms.shop;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
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

        if (shopItem.getSubSection().equalsIgnoreCase("kingdom")) {
            Kingdom kingdom = kingdomManager.getPlayerKingdom(player.getUniqueId());

            if (kingdom != null) {
                double cost = event.getAmount();

                if (kingdom.getWealth() >= cost) {
                    kingdom.giveWealth((int) -cost);
                    player.sendMessage("The cost of the spawn egg has been deducted from your kingdom's wealth.");
                } else {
                    player.sendMessage("Your kingdom does not have enough wealth to purchase this item.");
                }
            } else {
                player.sendMessage("You are not part of a kingdom.");
            }
        }
    }
}