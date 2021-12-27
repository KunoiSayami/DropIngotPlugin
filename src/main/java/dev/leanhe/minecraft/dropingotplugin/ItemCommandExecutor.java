package dev.leanhe.minecraft.dropingotplugin;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
public class ItemCommandExecutor implements CommandExecutor {

    private final DropIngotPlugin dropIngotPlugin;

    public ItemCommandExecutor(DropIngotPlugin dropIngotPlugin) {
        this.dropIngotPlugin = dropIngotPlugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            return false;
        }
        if (args.length < 5 || args.length > 6 ) {
            return false;
        }

        JobOptions job = JobOptions.from_vec_unsafe(args);

        sender.getServer().getScheduler().runTaskLater(this.dropIngotPlugin, () -> {
            job.spawn(sender.getServer().getWorlds().get(0));
        }, 60L);

        return true;
    }
}
