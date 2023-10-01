package io.github.derec4.dragonforgekingdoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KingdomManager {
    private static KingdomManager instance;
    private Map<String, Kingdom> kingdoms;

    private KingdomManager() {
        kingdoms = new HashMap<>();
    }

    public static synchronized KingdomManager getInstance() {
        if (instance == null) {
            instance = new KingdomManager();
        }
        return instance;
    }

    // Add methods to manage factions (e.g., addFaction, getFactions, etc.)

    public void addKingdom(Kingdom kingdom) {
        kingdoms.put(kingdom.getName(), kingdom);
    }

    // Example method to get a kingdom by name
    public Kingdom getKingdomByName(String name) {
        return kingdoms.get(name);
    }

    // Example method to get all kingdoms
    public List<Kingdom> getAllKingdoms() {
        return new ArrayList<>(kingdoms.values());
    }
}