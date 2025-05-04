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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        System.out.println("onPreTransaction called");

        Player player = event.getPlayer();
        UUID playerID = player.getUniqueId();
        System.out.println("Player: " + player.getName());

        ShopItem shopItem = event.getShopItem();
//        System.out.println(shopItem.getShopItem().toString());
//        System.out.println(shopItem.getItemPath());
//        System.out.println(shopItem.itemLoc);

        String sectionTitle = shopItem.getItemPath();
        System.out.println("SectionTitle: " + sectionTitle);

        if (sectionTitle == null) {
            System.out.println("SectionTitle is null, returning");
            return;
        }

        Pattern pattern = Pattern.compile("kingdom", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sectionTitle);

        if (matcher.find()) {
            System.out.println("SectionTitle is 'kingdom'");

            Kingdom kingdom = kingdomManager.getPlayerKingdom(playerID);
            System.out.println("Kingdom: " + (kingdom != null ? kingdom.getName() : "null"));

            if (kingdom == null) {
                System.out.println("Player is not in a kingdom, cancelling event");
                player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                Bukkit.getLogger().info("Player " + playerID + " was denied a purchase in Kingdom shop (not in a kingdom)");
                event.setCancelled(true);
                return;
            }

            PermissionLevel rank = PlayerUtils.getPlayerRank(player);
            System.out.println("Player Rank: " + rank);

            if (rank != PermissionLevel.LORD && rank != PermissionLevel.DUKE) {
                System.out.println("Player rank insufficient, cancelling event");
                player.sendMessage(ChatColor.RED + "You must be a Lord or Duke to access the Kingdom shop section.");
                Bukkit.getLogger().info("Player " + playerID + " was denied a purchase in Kingdom shop (insufficient rank)");
                event.setCancelled(true);
                return;
            }

            double cost = event.getPrice();
            System.out.println("Item Cost: " + cost);

            if (kingdom.getWealth() >= cost) {
                System.out.println("Kingdom has enough wealth, deducting cost");
                kingdom.giveWealth((int) -cost);
                BigDecimal playerBalanceFixed = Economy.getMoneyExact(playerID).add(BigDecimal.valueOf(cost));
                // Get player current balance and add value to offset the spend
                Economy.setMoney(playerID, playerBalanceFixed);
                player.sendMessage(ChatColor.GREEN + "The cost of the item has been deducted from your kingdom's wealth.");
            } else {
                System.out.println("Kingdom does not have enough wealth, cancelling event");
                player.sendMessage(ChatColor.RED + "Your kingdom does not have enough wealth to purchase this item.");
                event.setCancelled(true);
            }
        }
    }
}