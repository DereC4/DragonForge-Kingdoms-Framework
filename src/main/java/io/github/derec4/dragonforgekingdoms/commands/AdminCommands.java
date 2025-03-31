package io.github.derec4.dragonforgekingdoms.commands;

import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import io.github.derec4.dragonforgekingdoms.entity.CustomGuard;
import io.github.derec4.dragonforgekingdoms.entity.CustomSoldier;
import io.github.derec4.dragonforgekingdoms.entity.CustomSpawnEgg;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import static io.github.derec4.dragonforgekingdoms.util.DatabaseUtils.removeKingdomFromDatabase;
import static io.github.derec4.dragonforgekingdoms.util.DatabaseUtils.updatePlayerKingdom;

public class AdminCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is either the console or an OP player
        if ((sender instanceof Player && !sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        KingdomManager manager = KingdomManager.getInstance();

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /admin <arguments>");
            return true;
        } else {
            // Handle sub-commands
            String subCommand = args[0].toLowerCase();
            KingdomManager km = KingdomManager.getInstance();
            switch (subCommand) {
                case "delete" -> {
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Usage: /admin delete <kingdom name>");
                        return true;
                    }

                    String kingdomName = args[1];
                    CreateDB databaseManager = new CreateDB();

                    try (Connection connection = databaseManager.getConnection()) {
                        UUID kingdomUUID = km.getKingdomFromName(kingdomName);
                        if (kingdomUUID != null) {
                            km.removeKingdom(kingdomUUID);
                            removeKingdomFromDatabase(connection, kingdomUUID);
                            sender.sendMessage(ChatColor.GREEN + "[ADMIN] Kingdom " + kingdomName + " has been deleted.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "[ADMIN] Kingdom " + kingdomName + " not found.");
                        }
                    } catch (Exception e) {
                        Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
                        sender.sendMessage(ChatColor.RED + "[ADMIN] An error occurred while deleting the kingdom.");
                    }
                }
                case "changewner" -> {
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /admin changeowner <kingdom name> <new owner>");
                        return true;
                    }
                    String kingdomName = args[1];
                    String newOwnerName = args[2];
                    Player newOwner = Bukkit.getPlayer(newOwnerName);
                    if (newOwner == null) {
                        sender.sendMessage(ChatColor.RED + "Player " + newOwnerName + " not found or is offline.");
                        return false;
                    }
                    UUID newOwnerUUID = newOwner.getUniqueId();
                    CreateDB databaseManager = new CreateDB();
                    try (Connection connection = databaseManager.getConnection()) {
                        UUID kingdomUUID = km.getKingdomFromName(kingdomName);
                        if (kingdomUUID != null) {
                            Kingdom kingdom = km.getKingdomFromID(kingdomUUID);
                            UUID oldOwnerUUID = kingdom.getLeader();
                            kingdom.setLeader(newOwnerUUID);
                            updatePlayerKingdom(connection, newOwnerUUID, kingdomUUID);
                            km.removePlayerAsync(oldOwnerUUID);
                            km.addPlayerToKingdom(newOwnerUUID, kingdomUUID);
                            sender.sendMessage(ChatColor.GREEN + "[ADMIN] Kingdom " + kingdomName + " ownership has been transferred to " + newOwnerName + ".");
                        } else {
                            sender.sendMessage(ChatColor.RED + "[ADMIN] Kingdom " + kingdomName + " not found.");
                        }
                    } catch (SQLException e) {
                        Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
                        sender.sendMessage(ChatColor.RED + "[ADMIN] An error occurred while changing the kingdom owner.");
                    }
                }
//                case "spawnguard" -> {
//                    if (!(sender instanceof Player player)) {
//                        sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
//                        return true;
//                    }
//
//                    Location location = player.getLocation();
//                    ServerLevel world = ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle();
//                    CustomGuard customGuard = new CustomGuard(world,
//                            manager.getPlayerKingdom(player.getUniqueId()).getID());
//                    customGuard.setPos(location.getX(), location.getY(), location.getZ());
//                    world.addFreshEntity(customGuard);
//                    sender.sendMessage(ChatColor.GREEN + "[ADMIN] Custom Guard has been spawned.");
//                }
//                case "spawnsoldier" -> {
//                    if (!(sender instanceof Player player)) {
//                        sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
//                        return true;
//                    }
//
//                    Location location = player.getLocation();
//                    ServerLevel world = ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle();
//                    CustomSoldier customSoldier = new CustomSoldier(world,
//                            manager.getPlayerKingdom(player.getUniqueId()).getID());
//                    customSoldier.setPos(location.getX(), location.getY(), location.getZ());
//                    world.addFreshEntity(customSoldier);
//                    sender.sendMessage(ChatColor.GREEN + "[ADMIN] Custom Soldier has been spawned.");
//                }
                case "guardegg" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
                        return true;
                    }

                    ItemStack guardEgg = CustomSpawnEgg.createCustomSpawnEgg(1);
                    player.getInventory().addItem(guardEgg);
                    sender.sendMessage(ChatColor.GREEN + "[ADMIN] Custom Guard Spawn Egg has been given.");
                }
                case "soldieregg" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
                        return true;
                    }

                    ItemStack soldierEgg = CustomSpawnEgg.createCustomSpawnEgg(2);
                    player.getInventory().addItem(soldierEgg);
                    sender.sendMessage(ChatColor.GREEN + "[ADMIN] Custom Soldier Spawn Egg has been given.");
                }
            }
        }
        return true;
    }
}
