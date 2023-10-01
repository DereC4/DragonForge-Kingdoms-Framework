package io.github.derec4.dragonforgekingdoms;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KingdomCommandManager implements CommandExecutor {
    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param source  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender source, Command command, String label, String[] args) {
        if (source instanceof Player player) {
            if (args.length == 1) {
                String kingdomName = args[0];
                // Implement your logic for creating a kingdom here
                // You can access the player who executed the command with 'player'
                player.sendMessage("Creating a kingdom with the name: " + kingdomName);
                return true;
            } else {
                player.sendMessage("Usage: /createkingdom <name>");
            }
        } else {
            source.sendMessage("Only players can use this command.");
        }
        return false;
    }

}
