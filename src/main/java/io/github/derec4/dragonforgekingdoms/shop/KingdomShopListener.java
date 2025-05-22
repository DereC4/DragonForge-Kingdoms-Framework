package io.github.derec4.dragonforgekingdoms.shop;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.util.PermissionLevel;
import io.github.derec4.dragonforgekingdoms.util.PlayerUtils;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        Player player = event.getPlayer();
        UUID playerID = player.getUniqueId();
        Kingdom kingdom = kingdomManager.getPlayerKingdom(playerID);
        Bukkit.getLogger().info("onPreTransaction called from Player: " + player.getName());
        Bukkit.getLogger().info("Kingdom: " + (kingdom != null ? kingdom.getName() : "null"));

        if (kingdom == null) {
            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
            Bukkit.getLogger().info("Player " + playerID + " was denied a purchase in Kingdom shop (not in a kingdom)");
            event.setCancelled(true);
            return;
        }

        ShopItem shopItem = event.getShopItem();

//        Personal note - Right now only getItemPath seems to work but we can't access parents. For now we will hard
//        code categories
        String sectionTitle = shopItem.getItemPath();
        String shopTitle = shopItem.getSubSection();
        Bukkit.getLogger().info("Purchase being made:");
        Bukkit.getLogger().info("SectionTitle: " + sectionTitle);
        Bukkit.getLogger().info("subSection: " + shopTitle);

        if (sectionTitle == null) {
            Bukkit.getLogger().warning("SectionTitle is null, returning");
            return;
        }

        Pattern pattern = Pattern.compile("kingdom|wood|stone|mobs|redstone|mining|hunting|fishing|farming|armor|tools|potions|extras|offense|mobility", Pattern.CASE_INSENSITIVE);
        Pattern kingdomItems = Pattern.compile("kingdom", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sectionTitle);

        if (matcher.find()) {
            PermissionLevel rank = PlayerUtils.getPlayerRank(player);
            Bukkit.getLogger().info("Player Rank: " + rank);
            boolean isKingdomPurchase = false;

            if (rank == PermissionLevel.ADVENTURE) {
                player.sendMessage(ChatColor.RED + "You have to be in a kingdom!");
                Bukkit.getLogger().info("Player " + playerID + " was denied a purchase in Kingdom shop (not in a " +
                        "kingdom)");
                event.setCancelled(true);
                return;
            }

            if (shopItem.getShopItem().getType().equals(Material.SKELETON_SPAWN_EGG) && rank != PermissionLevel.LORD && rank != PermissionLevel.DUKE) {
                player.sendMessage(ChatColor.RED + "You must be a Lord or Duke to access the Kingdom shop section.");
                Bukkit.getLogger().info("Player " + playerID + " was denied a purchase in Kingdom shop (insufficient rank)");
                event.setCancelled(true);
                return;
            }

            if (kingdomItems.matcher(sectionTitle).find()) {
                isKingdomPurchase = true;
                Bukkit.getLogger().info("Purchase is using Kingdom funds");
            }

            double cost = event.getPrice();
            Bukkit.getLogger().info("Item Cost: " + cost);

            if (isKingdomPurchase && kingdom.getWealth() >= cost) {
                Bukkit.getLogger().info("Kingdom has enough wealth, deducting cost");
                kingdom.giveWealth((int) -cost);
                BigDecimal playerBalanceFixed = Economy.getMoneyExact(playerID).add(BigDecimal.valueOf(cost));

                // Get player current balance and add value to offset the spend
                Economy.setMoney(playerID, playerBalanceFixed);
                player.sendMessage(ChatColor.GREEN + "The cost of the item has been deducted from your kingdom's wealth.");
            } else if (!isKingdomPurchase) {
                Bukkit.getLogger().info("Kingdom does not have enough wealth, cancelling event");
                player.sendMessage(ChatColor.RED + "Your kingdom does not have enough wealth to purchase this item.");
                event.setCancelled(true);
            }
        }
    }
}