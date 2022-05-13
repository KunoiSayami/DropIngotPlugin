package dev.leanhe.minecraft.dropingotplugin.database;

import dev.leanhe.minecraft.dropingotplugin.DropIngotPlugin;
import dev.leanhe.minecraft.dropingotplugin.JobOptions;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;

// Copied from: https://www.spigotmc.org/threads/how-to-sqlite.56847/
public class SQLite extends Database {
    String dbname;

    public SQLite(DropIngotPlugin instance) {
        super(instance);
        dbname = "dropingot";
    }

    public String SQLiteCreateTokensTable = """
            CREATE TABLE IF NOT EXISTS "job_list" (
            	"id" INTEGER NOT NULL,
            	"world" TEXT NOT NULL,
            	"x"	REAL NOT NULL,
            	"y"	REAL NOT NULL,
            	"z"	REAL NOT NULL,
            	"type"	TEXT NOT NULL,
            	"amount"	INTEGER NOT NULL,
            	"interval"	INTEGER NOT NULL,
            	PRIMARY KEY("id")
            );
            """;


    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        File database = new File(plugin.getDataFolder(), dbname + ".db");
        if (!database.exists()) {
            try {
                database.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + database);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public SQLite load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
        return this;
    }


    public void initialize() {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM job_list LIMIT 1");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }

    public final static String TRUNCATE_STATEMENT = "DELETE FROM job_list";
    /*public final static String INSERT_STATEMENT = """
        INSERT INTO "job_list" 
        ("world", "x", "y", "z", "type", "amount", "interval", "job_id") VALUES (?, ?, ?, ?, ?, ?, ?)
        """;*/
    public final static String INSERT_STATEMENT = """
        INSERT INTO "job_list" VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
    public final static String SELECT_STATEMENT = "SELECT * FROM job_list";
    public final static String DELETE_STATEMENT = "DELETE FROM job_list WHERE id = ?";


    /*public World getWorldByUUID(String worldUUID) {
        return plugin.getServer().getWorld(worldUUID);
    }*/

    public World getWorldByName(String worldName) {
        return plugin.getServer().getWorld(worldName);
    }

    public ArrayList<JobOptions> getJobs() {
        ArrayList<JobOptions> list = new ArrayList<>();

        try (
                PreparedStatement statement = connection.prepareStatement(SELECT_STATEMENT);
                ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                World world = this.getWorldByName(rs.getString(2));
                if (world == null) {
                    world = plugin.getServer().getWorlds().get(0);
                }
                list.add(new JobOptions(rs, world));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Got exception while query jobs", e);
        }
        return list;
    }

    /*public JobOptions updateJob()*/

    public void insertJob(JobOptions job, int jobID) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_STATEMENT)) {
            statement.setInt(1, jobID);
            job.fillStatement(statement).execute();
            //connection.commit();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Got exception while insert jobs", e);
        }
    }

    public void removeJob(int jobID) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_STATEMENT)) {
            statement.setInt(1, jobID);
            statement.execute();
            //connection.commit();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Got exception while insert jobs", e);
        }
    }

    public void cleanJob() {
        try (PreparedStatement statement = connection.prepareStatement(TRUNCATE_STATEMENT)) {
            statement.execute();
            //connection.commit();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Got exception while clean jobs", e);
        }
    }

}
