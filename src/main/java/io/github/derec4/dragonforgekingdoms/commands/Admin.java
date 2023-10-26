package io.github.derec4.dragonforgekingdoms.commands;

import io.github.derec4.dragonforgekingdoms.Kingdom;
import io.github.derec4.dragonforgekingdoms.KingdomManager;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.UUID;

public class Admin implements CommandExecutor {

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
                    // Implement the logic
                    CreateDB databaseManager = new CreateDB();
                    String name = args[1];

                    // Send in connection and try to remove the kingdom row from table
                    try (Connection connection = databaseManager.getConnection()) {
                        km.removeKingdomAdmin(name, connection);
                    } catch (Exception e) {
                        Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
                    }
                }
                case "reset" -> {
                    // Implement the logic
                    CreateDB databaseManager = new CreateDB();

                    // Send in connection and try to remove the kingdom row from table
                    try (Connection connection = databaseManager.getConnection()) {

                    } catch (Exception e) {
                        Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
                    }
                }
            }
        }
        return true;
    }
}
