package io.github.derec4.dragonforgekingdoms;

import java.util.ArrayList;
import java.util.List;

public class KingdomManager {
    private static KingdomManager instance;
    private List<Kingdom> Kingdoms;

    private KingdomManager() {
        Kingdoms = new ArrayList<>();
    }

    public static synchronized KingdomManager getInstance() {
        if (instance == null) {
            instance = new KingdomManager();
        }
        return instance;
    }

    // Add methods to manage factions (e.g., addFaction, getFactions, etc.)

    // Example method to add a faction
    public void addKingdom(Kingdom k) {
        Kingdoms.add(k);
    }

    // Example method to get all factions
    public List<Kingdom> getKingdoms() {
        return Kingdoms;
    }
}