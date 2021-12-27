package dev.leanhe.minecraft.dropingotplugin;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;


// TODO: Use configure file to store jobs
class ConfigWrapper {

    public final int VERSION = 1;

    FileConfiguration config;

    ConfigWrapper(FileConfiguration config) {
        this.config = config;
        this.config.addDefault("VERSION", VERSION);
        this.config.addDefault("jobs", new ArrayList<JobOptions>());
        config.options().copyDefaults(true);
    }

    ConfigWrapper write_jobs() {
        ArrayList<JobOptions> jobs = (ArrayList<JobOptions>) this.config.getList("jobs");
        return this;
    }

}

public final class DropIngotPlugin extends JavaPlugin {

    ConfigWrapper config = new ConfigWrapper(getConfig());

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

