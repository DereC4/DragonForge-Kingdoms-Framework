package io.github.derec4.dragonforgekingdoms.commands;

import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
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
            }
        }
        return true;
    }
}
