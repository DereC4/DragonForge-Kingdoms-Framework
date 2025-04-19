package io.github.derec4.dragonforgekingdoms.shop;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.util.PermissionLevel;
import io.github.derec4.dragonforgekingdoms.util.PlayerUtils;
import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.math.BigDecimal;

public class KingdomShopListener implements Listener {

    private final KingdomManager kingdomManager;

    public KingdomShopListener() {
        this.kingdomManager = KingdomManager.getInstance();
    }

    /**
     * We're merging the previous listener into this one.
     * Use PreTransactionEvent, check if the player is allowed to buy kingdom eggs (and any future items added
     * under the section titled "kingdom" (case insensitive). If not a duke or lord in LuckPerms, OR in a kingdom, deny the transaction
     * @param event Event to listen for
     */
    @EventHandler
    public void onPreTransaction(PreTransactionEvent event) throws MaxMoneyException, UserDoesNotExistException, NoLoanPermittedException {
        Player player = event.getPlayer();
        ShopItem shopItem = event.getShopItem();

        String sectionTitle = shopItem.getSubSection();

        if (sectionTitle == null) {
            return;
        }

        if (sectionTitle.equalsIgnoreCase("kingdom")) {
            Kingdom kingdom = kingdomManager.getPlayerKingdom(player.getUniqueId());

            if (kingdom == null) {
                player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                Bukkit.getLogger().info("Player " + player.getUniqueId() + " was denied a purchase in Kingdom shop (not in a kingdom)");
                event.setCancelled(true);
                return;
            }

            PermissionLevel rank = PlayerUtils.getPlayerRank(player);

            if (rank != PermissionLevel.LORD && rank != PermissionLevel.DUKE) {
                player.sendMessage(ChatColor.RED + "You must be a Lord or Duke to access the Kingdom shop section.");
                Bukkit.getLogger().info("Player " + player.getUniqueId() + " was denied a purchase in Kingdom shop (insufficient rank)");
                event.setCancelled(true);
                return;
            }

            double cost = event.getAmount();

            // make sure player keeps their money but kingdom money deducted
            if (kingdom.getWealth() >= cost) {
                kingdom.giveWealth((int) -cost);
                Economy.add(player.getUniqueId(), new BigDecimal(cost));
                player.sendMessage(ChatColor.GREEN + "The cost of the item has been deducted from your kingdom's wealth.");
            } else {
                player.sendMessage(ChatColor.RED + "Your kingdom does not have enough wealth to purchase this item.");
                event.setCancelled(true);
            }
        }
    }
}