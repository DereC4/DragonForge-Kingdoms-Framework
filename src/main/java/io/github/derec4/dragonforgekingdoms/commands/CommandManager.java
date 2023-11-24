package io.github.derec4.dragonforgekingdoms.commands;

import io.github.derec4.dragonforgekingdoms.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.DragonForgeKingdoms;
import io.github.derec4.dragonforgekingdoms.Kingdom;
import io.github.derec4.dragonforgekingdoms.KingdomManager;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.Arrays;
import java.util.UUID;

public class CommandManager implements CommandExecutor {
    final String permsError = ChatColor.RED + "You do not have permission to use this command! Check" +
            " with the server staff?";

    /**
     * Checks if a player is in a kingdom
     */
    public boolean inAKingdom(UUID playerID) {
        KingdomManager temp = KingdomManager.getInstance();
        return temp.isPlayerMapped(playerID);
    }

    public void claimLand(Player player) {
        int chunkX = player.getLocation().getChunk().getX();
        int chunkZ = player.getLocation().getChunk().getZ();
        UUID worldID = player.getWorld().getUID();
//        String name = "TEMP"; // Replace with the actual kingdom name retrieval logic
//        Kingdom kingdom = KingdomManager.getInstance().getPlayerKingdom(player.getUniqueId());
        KingdomManager temp = KingdomManager.getInstance();
        ChunkCoordinate chunk = new ChunkCoordinate(chunkX, chunkZ, worldID);
        UUID playerKingdom = temp.getPlayerKingdom(player.getUniqueId()).getID();

        if (temp.claimChunk(playerKingdom, chunk)) {
            player.sendMessage(ChatColor.GREEN + "You have successfully claimed this chunk for your kingdom.");
        } else {
            player.sendMessage(ChatColor.RED + "Chunk claim failed. This chunk may already be claimed " +
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
        KingdomManager km = KingdomManager.getInstance();
        if (!(source instanceof Player player)) {
            source.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return false;
        }
        UUID playerID = player.getUniqueId();
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
                        //Checks: Player already in a kingdom, and if said kingdom name exists
                        // already
                        if(inAKingdom(playerID)) {
                            player.sendMessage(ChatColor.RED + "You are already in a kingdom!");
                            return false;
                        }
                        String name = args[1];
                        if(km.containsName(name)) {
                            player.sendMessage(ChatColor.RED + "That name is already used by " +
                                    "another kingdom!");
                            return false;
                        }
                        Kingdom k = new Kingdom(name, playerID, player.getLocation());
                        // Save the new kingdom to the database

                        CreateDB databaseManager = new CreateDB();
                        try (Connection connection = databaseManager.getConnection()) {
                            k.saveToDatabase(connection);
                        } catch (Exception e) {
                            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
                        }
                        km.addKingdom(k, playerID);
                        k = km.getPlayerKingdom(playerID);
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
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }
                        // Implement the logic
                        player.sendMessage("Removing your kingdom...");
                        CreateDB databaseManager = new CreateDB();

                        // Send in connection and try to remove the kingdom row from table
                        try (Connection connection = databaseManager.getConnection()) {
                            km.removeKingdom(playerID, connection);
                        } catch (Exception e) {
                            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
                        }
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "claim" -> {
                    if (player.hasPermission("kingdom.claim")) {
                        if(!inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
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
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }
                        String name = args[1];
                        Kingdom k = km.getPlayerKingdom(playerID);
                        player.sendMessage(ChatColor.GREEN + k.getName() + " has been renamed to " + name);
                        k.setName(name);
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "leave" -> {
                    if (player.hasPermission("kingdom.leave")) {
                        if(!inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }
                        Kingdom k = km.getPlayerKingdom(playerID);
                        String name = k.getName();
                        if(km.removePlayer(playerID)) {
                            player.sendMessage(ChatColor.RED + "You are no longer a member of " + name);
                        } else {
                            player.sendMessage(ChatColor.RED + "Could not remove player");
                        }

                    }
                }
                case "description" -> {
                    if (player.hasPermission("kingdom.description")) {
                        if (!inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }
                        if (args.length < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /kingdom description <description>");
                            return false;
                        }

                        String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                        Kingdom k = km.getPlayerKingdom(playerID);
                        k.setDescription(description);
                        player.sendMessage(ChatColor.GREEN + "Kingdom description set to: " + description);
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "sethome" -> {
                    if (player.hasPermission("kingdom.sethome")) {
                        if (!inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }
                        Kingdom k = km.getPlayerKingdom(playerID);
                        Location homeLocation = player.getLocation();
                        k.setHome(homeLocation);
                        player.sendMessage(ChatColor.GREEN + "Home location set to " + homeLocation);
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "territory" -> {
                    if (player.hasPermission("kingdom.territory")) {
                        if (!inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }

                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "join" -> {
                    if(player.hasPermission("kingdom.join")) {
                        if(inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are already in a kingdom!");
                            return false;
                        }
                        String name = args[1];
                        UUID id = km.getKingdomFromName(name);
                        if(id == null) {
                            player.sendMessage(ChatColor.RED + "Couldn't find a kingdom with that name!");
                            return false;
                        }

                        /*
                        To join a kingdom, update the player mappings
                        Also add the player to that kingdom's members
                        TODO Finally, update the player database
                         */
                        km.addPlayerToKingdom(player.getUniqueId(), id);
                        km.getKingdoms().get(id).addPlayer(player.getUniqueId());
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "home" -> {
                    if(player.hasPermission("kingdom.home")) {
                        if (!inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }
                        Kingdom k = km.getPlayerKingdom(playerID);
                        Location home = k.getHome();
                        player.sendMessage(ChatColor.GREEN + "Teleporting you to your kingdom's home!");
                        player.teleport(home);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "promote" -> {
                    if(player.hasPermission("kingdom.home")) {
                        if (!inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }

                        // Check if there's a specified player to promote
                        if (args.length < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /kingdom promote <player>");
                            return false;
                        }
                        String name = args[1];
                        Player targetPlayer = Bukkit.getPlayerExact(name);

                        // Check if the player is online
                        if (targetPlayer != null) {
                            Kingdom playerKingdom = km.getPlayerKingdom(player.getUniqueId());
                            if (targetPlayer.hasPermission("kingdom.role.vassal")) {
                                // Promote vassal to duke
                                targetPlayer.addAttachment(DragonForgeKingdoms.getInstance(), "kingdom.role.duke",
                                        true, 1);
                                targetPlayer.sendMessage(ChatColor.GREEN + "You have been promoted to Duke. You can now add, remove, and banish players, as well as access the kingdom store.");

                                player.sendMessage(ChatColor.GREEN + "Player has been successfully promoted.");
                            } else if (targetPlayer.hasPermission("kingdom.role.duke")) {
                                player.sendMessage(ChatColor.YELLOW + name + " cannot be promoted any higher than Duke.");
                            } else {
                                player.sendMessage(ChatColor.YELLOW + name + " is not a vassal in your kingdom.");
                            }
                        } else {
                            // The player is not online or the name is not valid
                            // Handle accordingly, for example, send a message to the sender
                            player.sendMessage(ChatColor.RED + "Player " + name + " is not online or the name is invalid.");
                        }
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                default -> player.sendMessage(ChatColor.RED + "Unknown sub-command. Usage: /kingdom <sub-command>");
            }
        }
        return true;
    }

}
