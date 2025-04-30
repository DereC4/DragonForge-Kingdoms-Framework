package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.commands.AdminCommands;
import io.github.derec4.dragonforgekingdoms.commands.CommandManager;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import io.github.derec4.dragonforgekingdoms.entity.DenyArrowsListener;
import io.github.derec4.dragonforgekingdoms.entity.CustomSpawnEggListener;
import io.github.derec4.dragonforgekingdoms.entity.EntityDeathListener;
import io.github.derec4.dragonforgekingdoms.kingdom.KingdomManager;
import io.github.derec4.dragonforgekingdoms.kingdom.egg.EggBossBar;
import io.github.derec4.dragonforgekingdoms.kingdom.egg.EggExplosionListener;
import io.github.derec4.dragonforgekingdoms.kingdom.egg.EggListener;
import io.github.derec4.dragonforgekingdoms.shop.KingdomShopListener;
import io.github.derec4.dragonforgekingdoms.territory.*;
import io.github.derec4.dragonforgekingdoms.util.DatabaseUtils;
import io.github.derec4.dragonforgekingdoms.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Enabled " + this.getName());

        this.getCommand("kingdom").setExecutor(new CommandManager());
        this.getCommand("admin").setExecutor(new AdminCommands());

        if (!getDataFolder().exists()) {
            getLogger().info("Directory Creation Status : " + getDataFolder().mkdirs());
        }

        this.databaseManager = new CreateDB();

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

        Bukkit.getLogger().info("");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  |_______|                             " +
                "  ");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  | Derex |     DragonForgeKingdoms v1.7.0");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "  |_______|     Running on " + Bukkit.getName() + " - " + Bukkit.getVersion());
        Bukkit.getLogger().info("");

        getServer().getPluginManager().registerEvents(new KingdomProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new TerritoryEnterExit(), this);
        getServer().getPluginManager().registerEvents(new TerritorySidebar(), this);
        getServer().getPluginManager().registerEvents(new EggListener(), this);
        getServer().getPluginManager().registerEvents(new EggBossBar(), this);
        getServer().getPluginManager().registerEvents(new EggExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new CustomSpawnEggListener(), this);
        getServer().getPluginManager().registerEvents(new KingdomShopListener(),this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
        getServer().getPluginManager().registerEvents(new DenyArrowsListener(), this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerEffects.applyEffects(player);
            }
        }, 0L, 2L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (UUID uuid : kingdomManager.getKingdoms().keySet()) {
                kingdomManager.getKingdoms().get(uuid).updateHealth(40);
            }
        }, 0L, 100L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, PlayerUtils::updateAllSidebars, 0L, 60L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("Disabled " + this.getName());
        DatabaseUtils.saveAll();
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