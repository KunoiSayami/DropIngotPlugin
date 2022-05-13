package dev.leanhe.minecraft.dropingotplugin.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import dev.leanhe.minecraft.dropingotplugin.DropIngotPlugin;
import org.bukkit.entity.Player;

public abstract class Database {
    DropIngotPlugin plugin;
    Connection connection;
    // The name of the table we created back in SQLite class.

    public Database(DropIngotPlugin instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}