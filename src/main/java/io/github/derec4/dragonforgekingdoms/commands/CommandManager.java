package io.github.derec4.dragonforgekingdoms.commands;

import io.github.derec4.dragonforgekingdoms.ChunkCoordinate;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
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
    private boolean inAKingdom(UUID playerID) {
        KingdomManager temp = KingdomManager.getInstance();
        return temp.isPlayerMapped(playerID);
    }

    private void claimLand(Player player) {
        int chunkX = player.getLocation().getChunk().getX();
        int chunkZ = player.getLocation().getChunk().getZ();
        UUID worldID = player.getWorld().getUID();
        KingdomManager km = KingdomManager.getInstance();
        ChunkCoordinate chunk = new ChunkCoordinate(chunkX, chunkZ, worldID);
        UUID playerKingdom = km.getPlayerKingdom(player.getUniqueId()).getID();

        if (km.claimChunk(playerKingdom, chunk)) {
            player.sendMessage(ChatColor.GREEN + "You have successfully claimed this chunk for your kingdom.");
        } else {
            player.sendMessage(ChatColor.RED + "Chunk claim failed. This chunk may already be claimed " +
                    "or there was an error.");
        }
    }

    private void promotePlayer(Player player, Player targetPlayer) {
        LuckPerms api = LuckPermsProvider.get();
//        User user = api.getPlayerAdapter(Player.class).getUser(targetPlayer);
        if (targetPlayer.hasPermission("group.vassal") && !targetPlayer.hasPermission("group.duke")) {
            // Promote vassal to duke
//            targetPlayer.addAttachment(DragonForgeKingdoms.getInstance(), "kingdom.role.duke", true, 1);
            Group group = api.getGroupManager().getGroup("duke");

            // Group doesn't exist?
            if (group == null) {
                player.sendMessage(ChatColor.RED +  " group does not exist!");
                return;
            }

            api.getUserManager().modifyUser(player.getUniqueId(), (User user) -> {
                // Create a node to add to the player.
                Node node = InheritanceNode.builder(group).build();

                // Add the node to the user.
                user.data().add(node);
                targetPlayer.sendMessage(ChatColor.GREEN + "You have been promoted to Duke. " +
                        "You can now add, remove, and banish players, as well as access the kingdom store.");
                player.sendMessage(ChatColor.GREEN + "Player has been successfully promoted.");
            });
        } else if (targetPlayer.hasPermission("group.duke")) {
            player.sendMessage(ChatColor.YELLOW + targetPlayer.getName() + " cannot be promoted any higher than Duke.");
        } else {
            player.sendMessage(ChatColor.RED + targetPlayer.getName() + " could not be promoted.");
        }
    }

    private boolean mapCommand(Player player, ChunkCoordinate centerChunk) {
        int mapRadius = 8;
        KingdomManager km = KingdomManager.getInstance();
        for (int dz = -mapRadius; dz <= mapRadius; dz++) {
            for (int dx = -mapRadius; dx <= mapRadius; dx++) {
                int x = centerChunk.getX() + dx;
                int z = centerChunk.getZ() + dz;
                ChunkCoordinate chunkCoord = new ChunkCoordinate(x, z, centerChunk.getWorldID());

                // Check if the chunk is claimed
                UUID kingdomUUID = km.getKingdomByChunk(chunkCoord);

                if (kingdomUUID == null) {
                    // Unclaimed chunk
                    player.sendMessage("-");
                } else {
                    // Chunk claimed by a kingdom
                    player.sendMessage("+");
                }
            }
        }
        return false;
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
                        km.createKingdom(k, playerID);
                        km.addKingdom(k, playerID);
                        k = km.getPlayerKingdom(playerID);
                        claimLand(player);
                        player.sendMessage(ChatColor.GREEN + "The Kingdom of " + k.getName() +
                                " has been created by " + player.getName());
//                        player.sendMessage(ChatColor.GREEN + k.printMembers());
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
//                        player.sendMessage("Claiming land for your kingdom...");
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
                            player.sendMessage(ChatColor.RED + "Failed to leave kingdom!");
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
                //TODO pledging and adjusting level
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
                    if(player.hasPermission("kingdom.promote")) {
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
                        Player targetPlayer = Bukkit.getPlayer(name);
                        LuckPerms api = LuckPermsProvider.get();

                        // Check if the player is online and in the same kingdom
                        if (targetPlayer != null) {
                            Kingdom sourceKingdom = km.getPlayerKingdom(playerID);
                            Kingdom targetKingdom = km.getPlayerKingdom(targetPlayer.getUniqueId());
                            if(sourceKingdom.equals(targetKingdom)) {
                                promotePlayer(player, targetPlayer);
                            } else {
                                player.sendMessage(ChatColor.YELLOW + name + " is not in your kingdom.");
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
                case "stats" -> {
                    if(player.hasPermission("kingdom.stats")) {
                        if (!inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }
                        Kingdom k = km.getPlayerKingdom(playerID);
                        player.spigot().sendMessage(k.getStats());
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                case "map" -> {
                    if(player.hasPermission("kingdom.map")) {
                        if (!inAKingdom(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                            return false;
                        }
                        ChunkCoordinate playerChunk = getPlayerCurrentChunk(player);

                        return mapCommand(player, playerChunk);
                    } else {
                        player.sendMessage(permsError);
                    }
                }
                default -> player.sendMessage(ChatColor.RED + "Unknown sub-command. Usage: /kingdom <sub-command>");
            }
        }
        return true;
    }

    private static ChunkCoordinate getPlayerCurrentChunk(Player player) {
        Location playerLocation = player.getLocation();
        int x = playerLocation.getBlockX() >> 4;
        int z = playerLocation.getBlockZ() >> 4;
        UUID worldID = playerLocation.getWorld().getUID();
        return new ChunkCoordinate(x, z, worldID);
    }
}
