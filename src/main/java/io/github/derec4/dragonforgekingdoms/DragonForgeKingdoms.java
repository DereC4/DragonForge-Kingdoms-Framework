package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.commands.Admin;
import io.github.derec4.dragonforgekingdoms.commands.KingdomCommandManager;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class DragonForgeKingdoms extends JavaPlugin {
    private CreateDB databaseManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info(ChatColor.GREEN + "Enabled " + this.getName());
        this.getCommand("kingdom").setExecutor(new KingdomCommandManager());
        this.getCommand("kingdom").setExecutor(new Admin());
        databaseManager = new CreateDB();
        if (databaseManager.connect()) {
            getLogger().info("Connected to database!");
            databaseManager.createLogTable();
            databaseManager.createKingdomTable();
        } else {
            getLogger().severe("Failed to connect to the database!");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info(ChatColor.RED + "Disabled " + this.getName());
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }
}
