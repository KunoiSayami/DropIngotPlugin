package dev.leanhe.minecraft.dropingotplugin;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitTask;

public class ItemCommandExecutor implements CommandExecutor {

    private final DropIngotPlugin dropIngotPlugin;

    public ItemCommandExecutor(DropIngotPlugin dropIngotPlugin) {
        this.dropIngotPlugin = dropIngotPlugin;
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

            if (job.getMaterial() == Material.DEAD_BRAIN_CORAL_FAN) {
                sender.sendMessage("Input type is unacceptable, please check your input.");
            }


            BukkitTask task = sender.getServer().getScheduler().runTaskTimer(this.dropIngotPlugin, () -> {
                job.spawn(sender.getServer().getWorlds().get(0));
            }, 0, job.getInterval());

            sender.sendMessage("Job created, your job id is " + task.getTaskId());
            JobOptions.insertJobs(task.getTaskId());

        } catch (CommandFormatErrorException ignored) {
            return false;
        } catch (NumberFormatException e) {
            sender.sendMessage("Parse error, check your input");
            return false;
        }


        return true;
    }
}
