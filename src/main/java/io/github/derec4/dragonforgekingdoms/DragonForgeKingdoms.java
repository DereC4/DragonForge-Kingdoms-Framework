package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.commands.KingdomCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class DragonForgeKingdoms extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info(ChatColor.GREEN + "Enabled " + this.getName());
        this.getCommand("kit").setExecutor(new KingdomCommandManager());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info(ChatColor.RED + "Disabled " + this.getName());
    }
}
