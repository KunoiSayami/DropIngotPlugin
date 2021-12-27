package dev.leanhe.minecraft.dropingotplugin;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class DropIngotPlugin extends JavaPlugin {

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        getLogger().info("Enabled");
        PluginCommand command = this.getCommand("test_plugin");
        if (command != null) {
            command.setExecutor(new ItemCommandExecutor(this));
        }
        //getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }
}

