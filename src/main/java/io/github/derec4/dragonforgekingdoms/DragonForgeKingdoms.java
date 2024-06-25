package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.commands.CommandManager;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.territory.EggBossBar;
import io.github.derec4.dragonforgekingdoms.territory.KingdomProtectionListener;
import io.github.derec4.dragonforgekingdoms.territory.PlayerEffects;
import io.github.derec4.dragonforgekingdoms.territory.TerritoryEnterExit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public final class DragonForgeKingdoms extends JavaPlugin {
    private CreateDB databaseManager;
    private static DragonForgeKingdoms instance; // Static variable to store the instance of the plugin
    private KingdomManager kingdomManager;

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        Bukkit.getLogger().info(ChatColor.GREEN + "Enabled " + this.getName());
        this.getCommand("kingdom").setExecutor(new CommandManager());
        if (!getDataFolder().exists()) {
            getLogger().info("Directory Creation Status : " + getDataFolder().mkdirs());
        }
        databaseManager = new CreateDB();
        if (databaseManager.connect()) {
            getLogger().info("Connected to database!");
            databaseManager.createLogTable();
            databaseManager.createKingdomTable();
            databaseManager.createChunkTable();
            databaseManager.createPlayerTable();
            databaseManager.createInvitesTable();
        } else {
            getLogger().severe("Failed to connect to the database!");
        }
        this.kingdomManager = KingdomManager.getInstance();
        try {
            Connection connection = databaseManager.getConnection();
            kingdomManager.loadKingdomsFromDatabase(connection);
            kingdomManager.loadTerritoryMappingsFromDatabase(connection);
            kingdomManager.loadPlayersFromDatabase(connection);
            getLogger().info("Databases loaded!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        getServer().getPluginManager().registerEvents(new KingdomProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new TerritoryEnterExit(), this);
        getServer().getPluginManager().registerEvents(new EggListener(), this);
        getServer().getPluginManager().registerEvents(new EggBossBar(), this);
//        getServer().getPluginManager().registerEvents(new TerritoryEnterExit(kingdomManager), this);
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerEffects.applyEffects(player);
            }
        }, 0L, 2L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (UUID uuid : kingdomManager.getKingdoms().keySet()) {
                kingdomManager.getKingdoms().get(uuid).updateHealth(2);
            }
        }, 0L, 100L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info(ChatColor.RED + "Disabled " + this.getName());
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        Bukkit.getScheduler().cancelTasks(this);
    }

    // Static method to get the instance of the plugin
    public static DragonForgeKingdoms getInstance() {
        return instance;
    }
}