package dev.leanhe.minecraft.dropingotplugin.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dev.leanhe.minecraft.dropingotplugin.DropIngotPlugin;

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