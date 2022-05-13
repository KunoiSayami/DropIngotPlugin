package dev.leanhe.minecraft.dropingotplugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


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
                sender.sendMessage("Stopped job " + args[0]);
            } else {
                sender.sendMessage("JobID incorrect, please check your input");
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

    private static final String[] materials = new String[]{"iron", "gold", "diamond", "netherite", "emerald", "notch", "exp", "netherstar", "ghasttear"};


    public static Location getSenderLocation(CommandSender sender) {
        Location location = null;
        if (sender instanceof Player) {
            location = ((Player) sender).getLocation();
        } else if (sender instanceof BlockCommandSender) {
            location = ((BlockCommandSender) sender).getBlock().getLocation();
        }
        return location;
    }

    public static World getSenderWorld(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getWorld();
        } else if (sender instanceof BlockCommandSender) {
            return ((BlockCommandSender) sender).getBlock().getWorld();
        } else {
            return sender.getServer().getWorlds().get(0);
        }
    }

    private static ArrayList<String> getLevel3Complete(CommandSender sender) {
        ArrayList<String> list = new ArrayList<>(List.of("~", "~ ~", "~ ~ ~"));
        Location location = getSenderLocation(sender);
        if (location != null) {
            list.addAll(List.of(
                    String.valueOf(location.getX()),
                    location.getX() + " " + location.getY(),
                    location.getX() + " " + location.getY() + " " + location.getZ()
            ));
        }
        return list;
    }

    private static ArrayList<String> getLevel2Complete(CommandSender sender) {
        ArrayList<String> list = new ArrayList<>(List.of("~", "~ ~"));
        Location location = getSenderLocation(sender);
        if (location != null) {
            list.addAll(List.of(
                    String.valueOf(location.getY()),
                    location.getY() + " " + location.getZ()
            ));
        }
        return list;
    }

    private static ArrayList<String> getLevel1Complete(CommandSender sender) {
        ArrayList<String> list = new ArrayList<>(List.of("~"));
        Location location = getSenderLocation(sender);
        if (location != null) {
            list.add(String.valueOf(location.getZ()));
        }
        return list;
    }

    private static List<String> getMatchedMaterial(String arg) {
        if (arg.isEmpty()) {
            return List.of(materials);
        }
        ArrayList<String> list = new ArrayList<>();
        for (String material: materials) {
            if (material.startsWith(arg)) {
                list.add(material);
            }
        }
        if (list.size() == 1) {
            return List.of(list.get(0) + " ");
        }
        return list;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String str = command.getName();
        switch (str) {
            case "airdrop":
                switch (args.length) {
                    case 1:
                        return getMatchedMaterial(args[0]);
                    case 2:
                    case 3:
                        return new ArrayList<>();
                    case 4:
                        return getLevel3Complete(sender);
                    case 5:
                        return getLevel2Complete(sender);
                    case 6:
                        return getLevel1Complete(sender);
                }
                break;
            case "cancelairdrop":
                if (args.length == 1) {
                    ArrayList<String> list = new ArrayList<>();
                    for (Integer job : JobOptions.getJobs()) {
                        list.add(job.toString());
                    }
                    return list;
                }
            default:
        }
        return super.onTabComplete(sender, command, alias, args);
    }

    @Override
    public void onEnable() {
        getLogger().info("Enabled");
        PluginCommand command = this.getCommand("airdrop");
        if (command != null) {
            command.setExecutor(new ItemCommandExecutor(this));
            command.setTabCompleter(this);
        }
        command = this.getCommand("cancelairdrop");
        if (command != null) {
            command.setExecutor(new StopCommandExecutor(this));
            command.setTabCompleter(this);
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

