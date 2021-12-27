package dev.leanhe.minecraft.dropingotplugin;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class JobOptions {
    final double x, y, z;
    final int interval, amout;
    String type;

    public JobOptions(double x, double y, double z, int interval, String type, int amout) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.interval = interval;
        this.type = type;
        this.amout = amout;
    }

    public static JobOptions from_vec(String[] args) throws CommandFormatErrorException {
        if (args.length < 5 || args.length > 6) {
            throw new CommandFormatErrorException();
        }
        return JobOptions.from_vec_unsafe(args);
    }

    static JobOptions from_vec_unsafe(String[] args) {
        return new JobOptions(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Integer.parseInt(args[4]), args[0], Integer.parseInt(args[5]));
    }

    Location get_location(World world) {
        return new Location(world, this.x, this.y, this.z);
    }

    Entity spawn(World world) {
        return world.dropItem(this.get_location(world), new ItemStack(Material.GOLD_INGOT, this.amout));
    }
}
