package io.github.derec4.dragonforgekingdoms;

import java.util.ArrayList;
import java.util.List;

public class KingdomManager {
    private static List<Kingdom> kingdoms;

    public KingdomManager() {
        this.kingdoms = new ArrayList<>();
    }

    public void addKingdom(Kingdom k) {
        kingdoms.add(k);
    }

    // Constructor and methods for managing factions
}