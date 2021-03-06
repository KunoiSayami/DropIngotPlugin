package dev.leanhe.minecraft.dropingotplugin;

import dev.leanhe.minecraft.dropingotplugin.database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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
            this.dropIngotPlugin.clearJobs();
            sender.sendMessage("Stopped all jobs");
            return true;
        }

        try {
            Integer jobID = Integer.parseInt(args[0]);
            if (JobOptions.queryJobs(jobID)) {
                JobOptions.cancelJob(sender.getServer(), jobID);
                sender.sendMessage("Stopped job " + args[0]);
                this.dropIngotPlugin.removeJob(jobID);
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


public final class DropIngotPlugin extends JavaPlugin {

    private SQLite sqliteInstance;

    static final String[] materials = new String[]{"iron", "gold", "diamond", "netherite", "emerald", "notch", "exp", "netherstar", "ghasttear"};


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
        for (String material : materials) {
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
        sqliteInstance = new SQLite(this).load();
        ArrayList<JobOptions> jobs = sqliteInstance.getJobs();
        sqliteInstance.cleanJob();
        for (JobOptions job : jobs) {
            if (!job.isMaterialVaild()) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[DropIngot] Skipped not vaild job => " + job);
                continue;
            }
            BukkitTask task = this.getServer().getScheduler().runTaskTimer(this, () -> ItemCommandExecutor.staff(this.getServer(), job), 0, job.getInterval());
            JobOptions.insertJobs(task.getTaskId());
            sqliteInstance.insertJob(job, task.getTaskId());
            Bukkit.getConsoleSender().sendMessage("%s[DropIngot] Created job(%d): %s%s".formatted(ChatColor.GREEN, task.getTaskId(), job.prettyString(), ChatColor.WHITE));
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[DropIngot] Load " + jobs.size() + " job(s)");
    }

    @Override
    public void onDisable() {
        JobOptions.clearJobs(getServer(), getPlugin(getClass()));
        getLogger().info("Disabled");
    }

    void clearJobs() {
        sqliteInstance.cleanJob();
    }

    void insertJob(JobOptions job, int jobID) {
        sqliteInstance.insertJob(job, jobID);
    }

    void removeJob(Integer jobID) {
        sqliteInstance.removeJob(jobID);
    }
}

