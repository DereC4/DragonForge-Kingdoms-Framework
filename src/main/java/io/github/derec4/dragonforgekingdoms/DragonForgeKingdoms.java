package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.commands.Admin;
import io.github.derec4.dragonforgekingdoms.commands.KingdomCommandManager;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import io.github.derec4.dragonforgekingdoms.territory.PlayerEffects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class DragonForgeKingdoms extends JavaPlugin {
    private CreateDB databaseManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info(ChatColor.GREEN + "Enabled " + this.getName());
        this.getCommand("kingdom").setExecutor(new KingdomCommandManager());
        getServer().getPluginManager().registerEvents(new KingdomProtectionListener(), this);
        databaseManager = new CreateDB();
        if (databaseManager.connect()) {
            getLogger().info("Connected to database!");
            databaseManager.createLogTable();
            databaseManager.createKingdomTable();
            databaseManager.createChunkTable();
            databaseManager.createPlayerTable();
        } else {
            getLogger().severe("Failed to connect to the database!");
        }
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerEffects.applyEffects(player);
            }
        }, 0L, 2L);
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
}
