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
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {
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

    private void initialClaimLand(Player player) {
        int playerChunkX = player.getLocation().getChunk().getX();
        int playerChunkZ = player.getLocation().getChunk().getZ();
        UUID worldID = player.getWorld().getUID();
        KingdomManager km = KingdomManager.getInstance();
        UUID playerKingdom = km.getPlayerKingdom(player.getUniqueId()).getID();

        // Claim chunks in a 3x3 grid centered around the player's chunk
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                int chunkX = playerChunkX + xOffset;
                int chunkZ = playerChunkZ + zOffset;
                ChunkCoordinate chunk = new ChunkCoordinate(chunkX, chunkZ, worldID);
                if (km.claimChunk(playerKingdom, chunk)) {
                    player.sendMessage(ChatColor.GREEN + "You have successfully claimed a chunk for your kingdom at (" + chunkX + ", " + chunkZ + ").");
                } else {
                    player.sendMessage(ChatColor.RED + "Chunk claim failed for chunk at (" + chunkX + ", " + chunkZ + "). This chunk may already be claimed or there was an error.");
                }
            }
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

    private BaseComponent[] mapCommand(Player player, ChunkCoordinate centerChunk) {
        int mapLength = 16;
        int mapHeight = 8;
        KingdomManager km = KingdomManager.getInstance();
        ComponentBuilder message = new ComponentBuilder();
        Kingdom playerKingdom = km.getPlayerKingdom(player.getUniqueId());

        // Title
        String kingdomName = playerKingdom == null ? "Wilderness" : km.getPlayerKingdom(player.getUniqueId()).getName();
        message.append(String.format("o0o0o [ X: %d, Y: %d, %s ] o0o0o", centerChunk.getX(), centerChunk.getZ(),
                kingdomName)).color(ChatColor.AQUA.asBungee());
        message.append("\n");
        for (int dz = -mapHeight; dz <= mapHeight; dz++) {
            for (int dx = -mapLength; dx <= mapLength; dx++) {
                int x = centerChunk.getX() + dx;
                int z = centerChunk.getZ() + dz;
                ChunkCoordinate chunkCoord = new ChunkCoordinate(x, z, centerChunk.getWorldID());

                // Check if the chunk is claimed
                UUID kingdomUUID = km.getKingdomByChunk(chunkCoord);

                // Determine the character based on the claimed status
                char mapChar;
                if (kingdomUUID == null) {
                    // Wilderness
                    mapChar = '-';
                } else if (playerKingdom != null && kingdomUUID.equals(playerKingdom.getID())) {
                    // Ally Kingdom
                    mapChar = '$';
                } else {
                    // Enemy Kingdom
                    mapChar = '#';
                }
                if (chunkCoord.equals(new ChunkCoordinate(player.getLocation().getChunk().getX(),
                        player.getLocation().getChunk().getZ(),
                        player.getLocation().getWorld().getUID()))){
                    mapChar = '9'; // Use blue color for player's current chunk
                }
                message.append(String.valueOf(mapChar)).color(getColor(mapChar).asBungee());
            }
            message.append("\n");
        }
        message.append("-").color(ChatColor.GRAY.asBungee()).append(": Wilderness ")
                .append("#").color(ChatColor.RED.asBungee()).append(": Ally ")
                .append("$").color(ChatColor.GREEN.asBungee()).append(": Enemy")
                .append("9").color(ChatColor.BLUE.asBungee()).append(": Current Chunk");
        return message.create();
    }

    private ChatColor getColor(char mapChar) {
        return switch (mapChar) {
            case '-' -> ChatColor.GRAY;
            case '#' -> ChatColor.RED;
            case '$' -> ChatColor.GREEN;
            case '9' -> ChatColor.BLUE; // Blue color for the player's current chunk
            default -> ChatColor.WHITE;
        };
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
        KingdomManager kManager = KingdomManager.getInstance();
        if (!(source instanceof Player player)) {
            source.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return false;
        }
        UUID playerID = player.getUniqueId();
        if (args.length == 0) {
            // Handle the /kingdom command without sub-commands
            player.sendMessage("Usage: /kingdom <sub-command>");
            return false;
        }

        // Handle sub-commands
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "help" -> {
                HelpCommand helpCommand = new HelpCommand();
                return helpCommand.onCommand(source, command, label, args);
            }
            case "create" -> {
                player.sendMessage("Creating a new kingdom...");
                if (!player.hasPermission("kingdom.create")) {
                    player.sendMessage(permsError);
                    return false;
                }

                // Check if the player is standing on solid ground
                Location playerLocation = player.getLocation();
                Block blockBelow = playerLocation.subtract(0, 1, 0).getBlock();
                if (!blockBelow.getType().isSolid()) {
                    player.sendMessage(ChatColor.RED + "You must be standing on solid ground to create a kingdom.");
                    return false;
                }

                // Most annoying check first: If land is claimed
                ChunkCoordinate playerChunk = getPlayerCurrentChunk(player);
                if (kManager.getKingdomByChunk(playerChunk) != null) {
                    player.sendMessage(ChatColor.RED + "This chunk is claimed!");
                    return false;
                }

                // Checks: Player already in a kingdom, and if said kingdom name exists already
                if (inAKingdom(playerID)) {
                    player.sendMessage(ChatColor.RED + "You are already in a kingdom!");
                    return false;
                }
                String name = args[1];
                if (kManager.containsName(name)) {
                    player.sendMessage(ChatColor.RED + "That name is already used by another kingdom!");
                    return false;
                }
                Kingdom kingdom = new Kingdom(name, playerID, player.getLocation());

                // Save the new kingdom to the database
                kManager.createKingdom(kingdom, playerID);
                kManager.addKingdom(kingdom, playerID);
                kingdom = kManager.getPlayerKingdom(playerID);
                kingdom.setHome(playerLocation);
                initialClaimLand(player);
                player.sendMessage(ChatColor.GREEN + "The Kingdom of " + kingdom.getName() +
                        " has been created by " + player.getName());
                player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 1.0f, 1.0f);

                // Create Heartstone
                kManager.createHeartstone(kingdom, player);
            }
            case "remove" -> {
                if (!player.hasPermission("kingdom.remove")) {
                    player.sendMessage(permsError);
                    return false;
                }
                if(!inAKingdom(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                    return false;
                }

                // Implement the logic
                player.sendMessage("Removing your kingdom...");
                CreateDB databaseManager = new CreateDB();

                // Send in connection and try to remove the kingdom row from table
                try (Connection connection = databaseManager.getConnection()) {
                    kManager.removeKingdom(playerID, connection);
                } catch (Exception e) {
                    Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
                }
            }
            case "claim" -> {
                if (!player.hasPermission("kingdom.claim")) {
                    player.sendMessage(permsError);
                    return false;
                }
                if(!inAKingdom(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                    return false;
                }
                claimLand(player);
            }
            case "rename" -> {
                if (player.hasPermission("kingdom.rename")) {
                    player.sendMessage(permsError);
                    return false;
                }
                if(!inAKingdom(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                    return false;
                }
                String name = args[1];
                Kingdom k = kManager.getPlayerKingdom(playerID);
                player.sendMessage(ChatColor.GREEN + k.getName() + " has been renamed to " + name);
                k.setName(name);
            }
            case "leave" -> {
                if (!player.hasPermission("kingdom.leave")) {
                    player.sendMessage(permsError);
                    return false;
                }
                if (!inAKingdom(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                    return false;
                }
                Kingdom k = kManager.getPlayerKingdom(playerID);
                String name = k.getName();
                if (kManager.playerLeave(player)) {
                    player.sendMessage(ChatColor.GREEN + "Your freedom has been bought!\n" + ChatColor.RED +
                            "You are no longer a member of " + name);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0f, 1.0f);
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to leave kingdom!");
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
                    Kingdom k = kManager.getPlayerKingdom(playerID);
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
                    Kingdom k = kManager.getPlayerKingdom(playerID);
                    Location homeLocation = player.getLocation();
                    k.setHome(homeLocation);
                    player.sendMessage(ChatColor.GREEN + "Home location set to " + homeLocation);
                } else {
                    player.sendMessage(permsError);
                }
            }
            case "territory" -> {
                if (!player.hasPermission("kingdom.territory")) {
                    player.sendMessage(permsError);
                    return false;
                }

                if (!inAKingdom(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                    return false;
                }

                UUID uuid = kManager.getKingdomFromID(playerID).getID();
                player.sendMessage(ChatColor.GREEN + kManager.getKingdomTerritory(uuid));
            }
            //TODO pledging and adjusting level
            case "join" -> {
                if (!player.hasPermission("kingdom.join")) {
                    player.sendMessage(permsError);
                    return false;
                }

                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /kingdom join <kingdom name>");
                    return false;
                }

                if (inAKingdom(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You are already in a kingdom!");
                    return false;
                }

                String name = args[1];
                UUID id = kManager.getKingdomFromName(name);

                if (id == null) {
                    player.sendMessage(ChatColor.RED + "Couldn't find a kingdom with that name!");
                    return false;
                }

                Kingdom kingdom = kManager.getKingdoms().get(id);

                if (!kingdom.isOpen() && !kManager.getPendingInvites().containsKey(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "That kingdom is private and you do not have an invite.");
                    return false;
                }

                if (kManager.getPendingInvites().containsKey(player.getUniqueId()) &&
                        !kManager.getPendingInvites().get(player.getUniqueId()).equals(id)) {
                    player.sendMessage(ChatColor.RED + "You have an invite to a different kingdom.");
                    return false;
                }

                /*
                    To join a kingdom, update the player mappings
                    Also add the player to that kingdom's members
                    TODO Finally, update the player database
                */
                kManager.addPlayerToKingdom(player.getUniqueId(), id);
                kManager.getKingdoms().get(id).addPlayer(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You have joined the kingdom " + name + "!");
                return true;
            }
            case "home" -> {
                if(player.hasPermission("kingdom.home")) {
                    if (!inAKingdom(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                        return false;
                    }
                    Kingdom k = kManager.getPlayerKingdom(playerID);
                    Location home = k.getHome();
                    player.sendMessage(ChatColor.GREEN + "Teleporting you to your kingdom's home!");
                    player.teleport(home);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                } else {
                    player.sendMessage(permsError);
                }
            }
            case "promote" -> {
                if(!player.hasPermission("kingdom.promote")) {
                    player.sendMessage(permsError);
                    return false;
                }
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

                // Check if the player is online and in the same kingdom
                if (targetPlayer != null) {
                    Kingdom sourceKingdom = kManager.getPlayerKingdom(playerID);
                    Kingdom targetKingdom = kManager.getPlayerKingdom(targetPlayer.getUniqueId());
                    if(sourceKingdom.equals(targetKingdom)) {
                        promotePlayer(player, targetPlayer);
                    } else {
                        player.sendMessage(ChatColor.YELLOW + name + " is not in your kingdom.");
                    }
                    
                } else {
                    player.sendMessage(ChatColor.RED + "Player " + name + " is not online or the name is invalid.");
                }
            }
            case "stats" -> {
                if(player.hasPermission("kingdom.stats")) {
                    if (!inAKingdom(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You are not in a kingdom!");
                        return false;
                    }
                    Kingdom k = kManager.getPlayerKingdom(playerID);
                    player.spigot().sendMessage(k.getStats());
                } else {
                    player.sendMessage(permsError);
                }
            }
            case "map" -> {
                if(player.hasPermission("kingdom.map")) {
                    // Can execute without being in a kingdom
                    ChunkCoordinate playerChunk = getPlayerCurrentChunk(player);
                    player.spigot().sendMessage(mapCommand(player, playerChunk));
                    return true;
                } else {
                    player.sendMessage(permsError);
                }
            }
            default -> player.sendMessage(ChatColor.RED + "Unknown sub-command. Usage: /kingdom <sub-command>");
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // List of subcommands for the first argument
            List<String> subcommands = Arrays.asList("create", "remove", "claim", "rename", "leave", "description",
                    "sethome", "territory", "join", "home", "promote", "stats", "map", "help");
            List<String> completions = new ArrayList<>();

//            // If the sender is a player or has specific permissions, add the relevant subcommands
//            if (sender instanceof Player) {
//                Player player = (Player) sender;
//                KingdomManager km = KingdomManager.getInstance();
//
//                if (km.isPlayerMapped(player.getUniqueId())) {
//                    completions.addAll(Arrays.asList("leave", "description", "sethome", "territory", "join", "home", "promote", "stats", "map"));
//                } else {
//                    completions.addAll(subcommands);
//                }
//            }
            completions.addAll(subcommands);

            // Filter completions based on the entered text
            String partialCommand = args[0].toLowerCase();
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(partialCommand)) {
                    completions.add(subcommand);
                }
            }

            Collections.sort(completions);
            return completions;
        }

        return null;
    }
}
