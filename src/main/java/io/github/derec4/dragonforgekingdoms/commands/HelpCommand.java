package io.github.derec4.dragonforgekingdoms.commands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.TreeMap;

public class HelpCommand implements CommandExecutor {
    private static final int COMMANDS_PER_PAGE = 10;
    private final TreeMap<String, String> commandHelpMap;

    public HelpCommand(Map<String, String> commandDescriptions) {
        commandHelpMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        commandHelpMap.putAll(commandDescriptions);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("k") && args.length > 0 && args[0].equalsIgnoreCase("help")) {
            int page = 1;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid page number.");
                    return true;
                }
            }
            sendHelpMessage(sender, page);
            return true;
        }
        return false;
    }

    private void sendHelpMessage(CommandSender sender, int page) {
        int startIndex = (page - 1) * COMMANDS_PER_PAGE;
        int endIndex = Math.min(startIndex + COMMANDS_PER_PAGE, commandHelpMap.size());
        sender.sendMessage(ChatColor.GREEN + "=== Kingdoms Help Page (" + page + ") ===");
        int count = 0;
        for (String command : commandHelpMap.keySet()) {
            if (count >= startIndex && count < endIndex) {
                sender.sendMessage(ChatColor.YELLOW + "/k " + command + " - " + commandHelpMap.get(command));
            }
            count++;
        }
        if (startIndex > 0) {
            sender.sendMessage(ChatColor.GRAY + "<<< Previous Page");
        }
        if (endIndex < commandHelpMap.size()) {
            sender.sendMessage(ChatColor.GRAY + "Next Page >>>");
        }
    }
}