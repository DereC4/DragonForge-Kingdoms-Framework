package io.github.derec4.dragonforgekingdoms.commands;

import io.github.derec4.dragonforgekingdoms.CustomWitherSkeleton;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import static io.github.derec4.dragonforgekingdoms.util.DatabaseUtils.removeKingdomFromDatabase;

public class AdminCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is either the console or an OP player
        if ((sender instanceof Player && !sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

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
                            km.updatePlayerKingdom(connection, newOwnerUUID, kingdomUUID);
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
                case "spawnwither" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
                        return true;
                    }

                    Location location = player.getLocation();
                    ServerLevel world = ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle();
                    CustomWitherSkeleton customWitherSkeleton = new CustomWitherSkeleton(world);
                    customWitherSkeleton.setPos(location.getX(), location.getY(), location.getZ());
                    world.addFreshEntity(customWitherSkeleton);
                    sender.sendMessage(ChatColor.GREEN + "[ADMIN] Custom Wither Skeleton has been spawned.");
                }
            }
        }
        return true;
    }
}
