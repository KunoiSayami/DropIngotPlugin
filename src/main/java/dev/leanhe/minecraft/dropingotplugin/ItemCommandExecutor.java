package dev.leanhe.minecraft.dropingotplugin;

import dev.leanhe.minecraft.dropingotplugin.exceptions.*;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public class ItemCommandExecutor implements CommandExecutor {

    private final DropIngotPlugin dropIngotPlugin;

    public ItemCommandExecutor(DropIngotPlugin dropIngotPlugin) {
        this.dropIngotPlugin = dropIngotPlugin;
    }

    public static double calcDistance(Location a, Location b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2) + Math.pow(a.getZ() - b.getZ(), 2));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp() || sender.hasPermission("dropingot.control"))) {
            sender.sendMessage("You don't have dropingot.control permission");
            return false;
        }

        if (args.length < 4 && sender instanceof ConsoleCommandSender) {
            sender.sendMessage("You should specify location that from console");
            return false;
        }

        if ((args.length > 3 && args.length < 6) || args.length > 6) {
            sender.sendMessage("Please check your input, should be /<command> <type> <amount> [interval] [x] [y] [z]");
            return false;
        }

        try {
            JobOptions job = JobOptions.fromVec(args, sender);

            if (!job.isMaterialVaild()) {
                sender.sendMessage("Input type is unacceptable, please check your input.");
                return false;
            }
            sender.getServer().getLogger().info(job.toString());

            BukkitTask task = sender.getServer().getScheduler().runTaskTimer(this.dropIngotPlugin, () -> {
                if (sender.getServer().getOnlinePlayers().stream().anyMatch(player -> {

                    Location loc = player.getLocation();
                    if (!Objects.equals(loc.getWorld(), job.getWorld())) {
                        return false;
                    }

                    double distance = ItemCommandExecutor.calcDistance(loc, job.getLocation());

                    //Bukkit.getLogger().info("Distance => " + distance);

                    return distance <= 64;
                }))
                {
                    job.spawn();
                }
            }, 0, job.getInterval());

            sender.sendMessage("Job created, your job id is " + task.getTaskId());
            JobOptions.insertJobs(task.getTaskId());

        } catch (CommandFormatErrorException ignored) {
            return false;
        } catch (ConsoleUsePlaceHolderException e) {
            sender.sendMessage("Please dont use ~ in console to input location");
            return false;
        } catch (NumberFormatException e) {
            sender.sendMessage("Parse error, check your input");
            return false;
        } catch (DropIngotPluginException ignored) {

        }


        return true;
    }
}
