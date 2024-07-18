package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class EggListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();

        if (clickedBlock == null || clickedBlock.getType() != Material.DRAGON_EGG) {
            return;
        }

        EggData eggData = getKingdomEgg(clickedBlock);
        if (eggData == null) {
            return;
        }

        KingdomManager kingdomManager = KingdomManager.getInstance();
        UUID uuid = UUID.fromString(eggData.getKingdomUuid());

        if (!kingdomManager.getKingdoms().containsKey(uuid)) {
            return;
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        Kingdom kingdom = kingdomManager.getKingdomFromID(uuid);

        if (kingdomManager.getPlayerKingdom(player.getUniqueId()).equals(kingdom)) {
            player.sendMessage(ChatColor.RED + "Friendly fire will not be tolerated!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0f, 1.0f);
            return;
        }

        System.out.println("DEBUG LOL BERSAM BASAG");
        getDamageModifiers(event.getPlayer().getInventory().getItemInMainHand());
        int dmg = (int) player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
//        player.sendMessage("Damage to egg: " + dmg);

        if(!kingdom.updateHealth(dmg * -1)) {
            System.out.println("Attempting to remove kingdom egg health is < 0");
            clickedBlock.getWorld().spawnParticle(Particle.DRAGON_BREATH, clickedBlock.getLocation().add(0.5, 0.5, 0.5), 30);
            clickedBlock.setType(Material.AIR);
            Block bedrockBase = clickedBlock.getRelative(BlockFace.DOWN);
            bedrockBase.setType(Material.GRASS_BLOCK);

            // Iterate through all players and send a destruction notification to those that are online
            for(UUID uuid1: kingdom.getMembers()) {
                Player player1 = Bukkit.getPlayer(uuid1);
                if (player1 != null && player1.isOnline()) {
                    player1.sendTitle(ChatColor.RED + "EGG DESTROYED", "Your kingdom has fallen!", 10, 70, 20); //
                    // (title,
                    // subtitle,
                    // fadeIn, stay, fadeOut)
                    player1.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f);
                }
            }
            kingdomManager.removeKingdom(uuid);
        }
        eggData.updateHealth(eggData.getHealth() - dmg, true);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 1.0f);
    }



    private void getDamageModifiers(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null && itemMeta.getPersistentDataContainer().has(Attribute.GENERIC_ATTACK_DAMAGE.getKey(), PersistentDataType.STRING)) {
            // Retrieve the attribute modifiers for damage
            itemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE);
        }

    }

    private EggData getKingdomEgg(Block block) {
        Chunk chunk = block.getChunk();
        EggData data = EggData.getChunkEggData(chunk);

        if(data == null) {
            return null;
        }
//        System.out.println(data.getX());
//        System.out.println(data.getY());
//        System.out.println(data.getZ());
        return (data.getX() == block.getX() && data.getY() == block.getY() && data.getZ() == block.getZ())
                ? data : null;
    }
}
