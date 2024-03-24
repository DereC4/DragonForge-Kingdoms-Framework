package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;

public class EggListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();

        if (clickedBlock == null) {
            return;
        }

        if(clickedBlock.getType() != Material.DRAGON_EGG) {
            return;
        }
        System.out.println("Break 1");
        EggData eggData = getKingdomEgg(clickedBlock);
        if (eggData == null) {
            return;
        }

        event.setCancelled(true);
        System.out.println("DEBUG LOL BERSAM BASAG");
        Set<AttributeModifier> damageModifiers = getDamageModifiers(event.getPlayer().getInventory().getItemInMainHand());
        int dmg = (int) player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        player.sendMessage("Damage to egg: " + dmg);
//        if (damageModifiers != null) {
////          KingdomManager kingdomManager = KingdomManager.getInstance();
////          byte[] encodedData = loadEncodedData(chunk); // HOW????
//            for (AttributeModifier modifier : damageModifiers) {
//                double amount = modifier.getAmount();
//                // You can use the amount and other properties of the modifier
//                player.sendMessage("Damage to egg: " + amount);
//            }
//        } else {
//            player.sendMessage("No damage to egg found.");
//        }
        player.playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 1.0f, 1.0f);
    }

    private Set<AttributeModifier> getDamageModifiers(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null && itemMeta.getPersistentDataContainer().has(Attribute.GENERIC_ATTACK_DAMAGE.getKey(), PersistentDataType.STRING)) {
            // Retrieve the attribute modifiers for damage
            return Set.copyOf(itemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE));
        }

        return null; // No damage modifiers found
    }

    private EggData getKingdomEgg(Block block) {
        Chunk chunk = block.getChunk();
        EggData data = EggData.getChunkEggData(chunk);

        if(data == null) {
            return null;
        }
        System.out.println("Break 2");
        System.out.println(data.getX());
        System.out.println(data.getY());
        System.out.println(data.getZ());
        return (data.getX() == block.getX() && data.getY() == block.getY() && data.getZ() == block.getZ())
                ? data : null;
    }
}
