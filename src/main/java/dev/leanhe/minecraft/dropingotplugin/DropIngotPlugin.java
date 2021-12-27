package dev.leanhe.minecraft.dropingotplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;


class StopCommandExecutor implements CommandExecutor {

    private final DropIngotPlugin dropIngotPlugin;

    StopCommandExecutor(DropIngotPlugin plugin) {
        dropIngotPlugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp() || sender.hasPermission("dropingot.control"))) {
            sender.sendMessage("You don't have dropingot.control permission");
            return false;
        }

        if (args.length == 0) {
            sender.getServer().getScheduler().cancelTasks(this.dropIngotPlugin);
            sender.sendMessage("Stopped all jobs");
            return true;
        }

        try {
            Integer jobID = Integer.parseInt(args[0]);
            if (JobOptions.queryJobs(jobID)) {
                JobOptions.cancelJob(sender.getServer(), jobID);
                sender.sendMessage("Stopped " + args[0]);
            } else {
                sender.sendMessage("JobID uncorrect, please check your input");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("Please check your input");
            return false;
        }
        return true;
    }
}

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
        PluginCommand command = this.getCommand("airdrop");
        if (command != null) {
            command.setExecutor(new ItemCommandExecutor(this));
        }
        command = this.getCommand("stop_task");
        if (command != null) {
            command.setExecutor(new StopCommandExecutor(this));
        }
        JobOptions.getJobs();

        //getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        JobOptions.clearJobs(getServer(), getPlugin(getClass()));
        getLogger().info("Disabled");
    }
}

