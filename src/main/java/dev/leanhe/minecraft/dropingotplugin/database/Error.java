package dev.leanhe.minecraft.dropingotplugin.database;


import dev.leanhe.minecraft.dropingotplugin.DropIngotPlugin;

import java.util.logging.Level;

public class Error {
    public static void execute(DropIngotPlugin plugin, Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }

    public static void close(DropIngotPlugin plugin, Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}