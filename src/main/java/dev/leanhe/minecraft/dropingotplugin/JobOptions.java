package dev.leanhe.minecraft.dropingotplugin;


import dev.leanhe.minecraft.dropingotplugin.exceptions.CommandFormatErrorException;
import dev.leanhe.minecraft.dropingotplugin.exceptions.ConsoleUsePlaceHolderException;
import dev.leanhe.minecraft.dropingotplugin.exceptions.DropIngotPluginException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class JobOptions {

    static ArrayList<Integer> jobs;

    final World world;
    final Location location;
    final double x, y, z;

    public int getInterval() {
        return interval * 20;
    }

    final int interval, amount;
    String type;

    public JobOptions(String type, Location location, World world) {
        this(type, 1, location, world);
    }

    public JobOptions(String type, int amount, Location location, World world) {
        this(type, amount, 3, location, world);
    }

    public JobOptions(String type, int amount, int interval, Location location, World world) {
        this(type, amount, interval, -1, -1, -1, location, world);
    }

    public JobOptions(ResultSet resultSet, World world) throws SQLException {
        this(resultSet.getString(6), resultSet.getInt(7), resultSet.getInt(8), new Location(world, resultSet.getDouble(3), resultSet.getDouble(4), resultSet.getDouble(5)), world);
    }

    public PreparedStatement fillStatement(PreparedStatement statement) throws SQLException {
        statement.setString(2, Objects.requireNonNull(this.location.getWorld()).getName());
        statement.setDouble(3, this.location.getX());
        statement.setDouble(4, this.location.getY());
        statement.setDouble(5, this.location.getZ());
        statement.setString(6, this.type);
        statement.setInt(7, this.amount);
        statement.setInt(8, this.interval);
        return statement;
    }


    public Location getLocation() {
        return this.location;
    }

    public JobOptions(String type, int amount, int interval, double x, double y, double z, Location location, World world) {
        this.x = x;
        this.y = y;
        this.z = z;
        if (interval <= 0) {
            interval = 1;
        }
        this.interval = interval;
        this.type = type;
        if (amount < 1) {
            amount = 1;
        }
        this.amount = amount;
        this.location = location;
        this.world = world;
    }

    private static double getRealLocation(Location location, int seq) {
        switch (seq) {
            case 0:
                return location.getX();
            case 1:
                return location.getY();
            case 2:
                return location.getZ();
        }
        throw new RuntimeException("Unreachable!");
    }

    private static String replaceLocationPlaceHolder(String str, Location location, int seq) {
        if (str.startsWith("~")) {
            String offset = str.split("~", 1)[1];
            if (offset.isEmpty()) {
                return String.valueOf(getRealLocation(location, seq));
            }
            return String.valueOf(getRealLocation(location, seq) + Double.parseDouble(offset));
        }
        return str;
    }

    static JobOptions fromVec(String[] args, CommandSender sender) throws DropIngotPluginException {
        Location location = DropIngotPlugin.getSenderLocation(sender);
        World world = DropIngotPlugin.getSenderWorld(sender);
        switch (args.length) {
            case 1:
                return new JobOptions(args[0], location, world);
            case 2:
                return new JobOptions(args[0], Integer.parseInt(args[1]), location, world);
            case 3:
                return new JobOptions(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), location, world);
            case 6:
                if (location == null) {
                    for (int i = 3; i < 6; i++) {
                        if (args[i].startsWith("~")) {
                            throw new ConsoleUsePlaceHolderException();
                        }
                    }
                } else {
                    for (int i = 3; i < 6; i++) {
                        args[i] = replaceLocationPlaceHolder(args[i], location, i - 3);
                    }
                }
                return new JobOptions(args[0], Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]),
                        Double.parseDouble(args[5]), null, world);
            default:
                throw new CommandFormatErrorException();
        }
    }

    Location getLocation(World world) {
        return Objects.requireNonNullElseGet(location, () -> new Location(world, this.x, this.y, this.z));
    }

    void spawn(Location location) {
        if (this.type.equals("7") || this.type.equals("exp")) {
            this.world.spawn(location, ExperienceOrb.class).setExperience(amount);
            return;
        }
        this.world.dropItem(location, new ItemStack(this.getMaterial(), this.amount));
    }

    void spawn() {
        if (this.x == this.y && this.y == this.z && this.z == -1 && location == null) {
            throw new RuntimeException("Job is initialized without location, use spawn(World, Location) instead");
        }
        Location location = this.location;
        if (location == null) {
            location = this.getLocation(this.world);
        }
        this.spawn(location);
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
            // 7 SKIPPED (EXP)
            case "8":
            case "netherstar":
                return Material.NETHER_STAR;
            case "9":
            case "ghasttear":
                return Material.GHAST_TEAR;
            default:
                return Material.DEAD_BRAIN_CORAL_FAN;
        }
    }

    boolean isMaterialVaild() {
        if ((this.type.equals("7") || this.type.equals("exp"))) {
            return true;
        }
        return this.getMaterial() != Material.DEAD_TUBE_CORAL_FAN;
    }

    @Override
    public String toString() {
        return "JobOptions{" +
                "location=" + location +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", interval=" + interval +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }

    public String prettyString() {
        String type = this.type;
        try {
            int i = Integer.parseInt(type);
            type = DropIngotPlugin.materials[i - 1];
        } catch (NumberFormatException ignore) {
        }
        return "DropJob: %s x %d %s(%d, %d, %d) %ds".formatted(
                type, this.amount, Objects.requireNonNull(this.location.getWorld()).getName(), (int) this.location.getX(), (int) this.location.getY(), (int) this.location.getZ(), this.interval
        );
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
        for (Integer i : JobOptions.jobs) {
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

    public World getWorld() {
        return this.world;
    }

}
