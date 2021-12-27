package dev.leanhe.minecraft.dropingotplugin;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class JobOptions {

    static ArrayList<Integer> jobs;

    final Location location;
    final double x, y, z;

    public int getInterval() {
        return interval * 20;
    }

    final int interval, amount;
    String type;

    @Deprecated
    public JobOptions(String type) {
        this(type, 1);
    }

    @Deprecated
    public JobOptions(String type, int amount) {
        this(type, amount, 3);
    }

    @Deprecated
    public JobOptions(String type, int amount, int interval) {
        this(type, amount, interval, -1 ,-1 ,-1, null);
    }

    public JobOptions(String type, Location location) {
        this(type, 1, location);
    }

    public JobOptions(String type, int amount, Location location) {
        this(type, amount, 3, location);
    }

    public JobOptions(String type, int amount, int interval, Location location) {
        this(type, amount, interval, -1 ,-1 ,-1, location);
    }

    public JobOptions(String type, int amount, int interval, double x, double y, double z, Location location) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.interval = interval;
        this.type = type;
        this.amount = amount;
        this.location = location;
    }

    static JobOptions fromVec(String[] args, CommandSender sender) throws CommandFormatErrorException {
        Location location = null;
        if (sender instanceof Player) {
            location = ((Player) sender).getLocation();
        } else if (sender instanceof BlockCommandSender) {
            location = ((BlockCommandSender) sender).getBlock().getLocation();
        }
        switch (args.length) {
            case 1:
                return new JobOptions(args[0], location);
            case 2:
                return new JobOptions(args[0], Integer.parseInt(args[1]), location);
            case 3:
                return new JobOptions(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), location);
            case 6:
                return new JobOptions(args[0], Integer.parseInt(args[4]), Integer.parseInt(args[5]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), null);
            default:
                throw new CommandFormatErrorException();
        }
    }

    Location getLocation(World world) {
        if (location == null) {
            return new Location(world, this.x, this.y, this.z);
        } else {
            return location;
        }
    }

    @Deprecated
    Item spawn(World world, CommandSender sender) {
        if (sender instanceof Player) {
            return this.spawn(world, ((Player) sender).getLocation());
        } else if (sender instanceof BlockCommandSender) {
            return this.spawn(world, ((BlockCommandSender) sender).getBlock().getLocation());
        }
        throw new RuntimeException("Unreachable!");
    }

    Item spawn(World world, Location location) {
        return world.dropItem(location, this.get_item());
    }

    Item spawn(World world) {
        if (this.x == this.y && this.y == this.z && this.z == -1 && location == null) {
            throw new RuntimeException("Job is initialized without location, use spawn(World, Location) instead");
        }
        Location location = this.location;
        if (location == null) {
            location = this.getLocation(world);
        }
        return this.spawn(world, location);
    }

    Material getMaterial() {
        switch (this.type) {
            case "1":
            case "iron":
                return Material.IRON_INGOT;
            case "2":
            case "gold":
                return Material.GOLD_INGOT;
            case "3":
            case "diamond":
                return Material.DIAMOND;
            case "4":
            case "nether":
            case "netherite":
                return Material.NETHERITE_INGOT;
            case "5":
            case "emerald":
                return Material.EMERALD;
            case "6":
            case "notch":
                return Material.ENCHANTED_GOLDEN_APPLE;
            default:
                return Material.DEAD_BRAIN_CORAL_FAN;
        }
    }

    ItemStack get_item() {
        return new ItemStack(this.getMaterial(), this.amount);
    }

    static ArrayList<Integer> getJobs() {
        if (JobOptions.jobs == null) {
            JobOptions.jobs = new ArrayList<>();
        }
        return JobOptions.jobs;
    }

    static void insertJobs(Integer jobID) {
        if (JobOptions.jobs == null) {
            JobOptions.jobs = new ArrayList<>();
        }
        JobOptions.jobs.add(jobID);
    }

    static void clearJobs(Server server, Plugin plugin) {
        server.getScheduler().cancelTasks(plugin);
    }

    static void clearJobs(Server server) {
        for (Integer i: JobOptions.jobs) {
            server.getScheduler().cancelTask(i);
        }
        JobOptions.jobs.clear();
    }

    static void cancelJob(Server server, Integer jobID) {
        server.getScheduler().cancelTask(jobID);
        JobOptions.jobs.remove(jobID);
    }

    static boolean queryJobs(Integer jobID) {
        return JobOptions.jobs.contains(jobID);
    }
}
