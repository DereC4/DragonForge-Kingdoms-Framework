package io.github.derec4.dragonforgekingdoms.shop;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.util.PermissionLevel;
import io.github.derec4.dragonforgekingdoms.util.PlayerUtils;
import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PreventKingdomShopEntryListener implements Listener {
    private final KingdomManager kingdomManager;

    public PreventKingdomShopEntryListener(KingdomManager kingdomManager) {
        this.kingdomManager = kingdomManager;
    }

    /**
     * Use PreTransactionEvent, check if the player is allowed to buy kingdom eggs (and any future items added
     * under the section titled "kingdom" (case insensitive). If not a duke or lord in LuckPerms, deny the transaction
     * @param event
     */
    @EventHandler
    public void onPreTransaction (PreTransactionEvent event) {
        Player player = event.getPlayer();
        String sectionTitle = event.getShopItem().getSubSection();

        if (sectionTitle.equalsIgnoreCase("Kingdom")) {
            PermissionLevel rank = PlayerUtils.getPlayerRank(player);

            if (rank != PermissionLevel.LORD && rank != PermissionLevel.DUKE) {
                player.sendMessage(ChatColor.RED + "You must be a Lord or Duke to access the Kingdom shop section.");
                Bukkit.getLogger().info("Player " + player.getUniqueId() + " was denied a purchase in Kingdom shop");
                event.setCancelled(true);
            }
        }
    }
}
