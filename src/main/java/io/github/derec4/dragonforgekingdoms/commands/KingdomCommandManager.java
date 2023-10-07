package io.github.derec4.dragonforgekingdoms.commands;

import io.github.derec4.dragonforgekingdoms.Kingdom;
import io.github.derec4.dragonforgekingdoms.KingdomManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KingdomCommandManager implements CommandExecutor {
    final String permsError = ChatColor.RED + "You do not have permission to use this command! Check" +
            " with the server staff?";

    public boolean inAKingdom(UUID playerID) {
        KingdomManager temp = KingdomManager.getInstance();
        return temp.isPlayerMapped(playerID);
    }
    public void claimLand(Player player) {
        int chunkX = player.getLocation().getChunk().getX();
        int chunkZ = player.getLocation().getChunk().getZ();
//        String name = "TEMP"; // Replace with the actual kingdom name retrieval logic
        Kingdom kingdom = KingdomManager.getInstance().getPlayerKingdom(player.getUniqueId());

        if (kingdom.claimChunk(chunkX, chunkZ)) {
            player.sendMessage("You have successfully claimed this chunk for your kingdom.");
        } else {
            player.sendMessage("Chunk claim failed. This chunk may already be claimed " +
                    "or there was an error.");
        }
    }
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
        if (!(source instanceof Player player)) {
            source.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return false;
        }

        if (args.length == 0) {
            // Handle the /kingdom command without sub-commands
            player.sendMessage("Usage: /kingdom <sub-command>");
        } else {
            // Handle sub-commands
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "create" -> {
                    player.sendMessage("Creating a new kingdom...");
                    if (player.hasPermission("kingdom.create")) {
                        UUID playerID = player.getUniqueId();
                        KingdomManager temp = KingdomManager.getInstance();

                        //Checks: Player already in a kingdom, and if said kingdom name exists
                        // already
                        if(inAKingdom(playerID)) {
                            player.sendMessage(ChatColor.RED + "You are already in a kingdom!");
                            return false;
                        }
                        String name = args[1];
                        if(temp.containsName(name)) {
                            player.sendMessage(ChatColor.RED + "That name is already used by " +
                                    "another kingdom!");
                            return false;
                        }
                        Kingdom k = new Kingdom(name, playerID);
                        temp.addKingdom(k, playerID);
                        k = temp.getPlayerKingdom(playerID);
                        player.sendMessage(ChatColor.GREEN + "The Kingdom of " + k.getName() +
                                " has been created by " + player.getName());
                        player.sendMessage(ChatColor.GREEN + k.printMembers());
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "remove" -> {
                    if (player.hasPermission("kingdom.remove")) {
                        if(!inAKingdom(player.getUniqueId())) {
                            player.sendMessage("You are not in a kingdom!");
                            return false;
                        }
                        // Implement the logic
                        player.sendMessage("Removing your kingdom...");
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "claim" -> {
                    if (player.hasPermission("kingdom.claim")) {
                        if(!inAKingdom(player.getUniqueId())) {
                            player.sendMessage("You are not in a kingdom!");
                            return false;
                        }
                        player.sendMessage("Claiming land for your kingdom...");
                        claimLand(player);
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "rename" -> {
                    if (player.hasPermission("kingdom.rename")) {
                        if(!inAKingdom(player.getUniqueId())) {
                            player.sendMessage("You are not in a kingdom!");
                            return false;
                        }
                        String name = args[1];
                        UUID playerID = player.getUniqueId();
                        KingdomManager temp = KingdomManager.getInstance();
                        Kingdom k = temp.getKingdomByName(playerID);
                        player.sendMessage(k.getName() + " has been renamed to " + name);
                        k.setName(name);
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "leave" -> {
                    if (player.hasPermission("kingdom.leave")) {
                        if(!inAKingdom(player.getUniqueId())) {
                            player.sendMessage("You are not in a kingdom!");
                            return false;
                        }
                        UUID playerID = player.getUniqueId();
                        KingdomManager temp = KingdomManager.getInstance();
                        Kingdom k = temp.getKingdomByName(playerID);
                        String name = k.getName();
                        if(temp.removePlayer(playerID)) {
                            player.sendMessage(ChatColor.RED + "You are no longer a member of " + name);
                        } else {
                            player.sendMessage(ChatColor.RED + "Could not remove player");
                        }

                    }
                }
                default -> player.sendMessage(ChatColor.RED + "Unknown sub-command. Usage: /kingdom <sub-command>");
            }
        }
        return true;
    }

}
