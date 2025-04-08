package io.github.derec4.dragonforgekingdoms.commands;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    @Getter
    private static final Map<String, String> commandDescriptions = new HashMap<>();

    static {
        commandDescriptions.put("create", "Create a new kingdom");
        commandDescriptions.put("remove", "Remove your kingdom");
        commandDescriptions.put("claim", "Claim land for your kingdom");
        commandDescriptions.put("rename", "Rename your kingdom");
        commandDescriptions.put("leave", "Leave your kingdom");
        commandDescriptions.put("description", "Set your kingdom's description");
        commandDescriptions.put("sethome", "Set your kingdom's home location");
        commandDescriptions.put("territory", "Show your kingdom's territory");
        commandDescriptions.put("join", "Join a kingdom");
        commandDescriptions.put("home", "Teleport to your kingdom's home");
        commandDescriptions.put("promote", "Promote a kingdom member");
        commandDescriptions.put("stats", "Show your kingdom's stats");
        commandDescriptions.put("map", "Show a map of nearby allies and enemies");
        commandDescriptions.put("help", "Show help information");
        commandDescriptions.put("invite", "Invite a player to your kingdom");
        commandDescriptions.put("transfer", "Transfer money to your kingdom's wealth");
        commandDescriptions.put("save-all", "Save all data to the database");
        commandDescriptions.put("banish", "Banish a player in your kingdom");
    }

}