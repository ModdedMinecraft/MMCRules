package net.moddedminecraft.mmcrules.Database;

import com.zaxxer.hikari.HikariDataSource;
import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class H2DataStore implements IDataStore {

    private final Main plugin;
    private final Optional<HikariDataSource> dataSource;

    public H2DataStore(Main plugin) {
        this.plugin = plugin;
        this.dataSource = getDataSource();
    }

    @Override
    public String getDatabaseName() {
        return "H2";
    }

    @Override
    public boolean load() {
        if (!dataSource.isPresent()) {
            plugin.getLogger().error("Selected datastore: 'H2' is not avaiable please select another datastore.");
            return false;
        }
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + Config.h2Prefix + "players ("
                    + " playeruuid VARCHAR(36) NOT NULL PRIMARY KEY"
                    + ");");

            getConnection().commit();
        } catch (SQLException ex) {
            plugin.getLogger().error("Unable to create tables", ex);
            return false;
        }
        return true;
    }

    @Override
    public List<String> getAccepted() {
        List<String> uuidList = new ArrayList<>();

        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + Config.h2Prefix + "players");
            while (rs.next()) {
                uuidList.add(rs.getString("playeruuid"));
            }
            return uuidList;
        } catch (SQLException ex) {
            plugin.getLogger().info("H2: Couldn't read ticketdata from H2 database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addPlayer(String uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + Config.h2Prefix + "players VALUES (?);");
            statement.setString(1, uuid);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error adding playerdata", ex);
        }
        return false;
    }

    @Override
    public boolean removePlayer(String uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + Config.h2Prefix + "players WHERE playeruuid = '" + uuid + "';");
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error removing playerdata", ex);
        }
        return false;
    }

    @Override
    public boolean clearList() {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE " + Config.h2Prefix + "players;");
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error removing all playerdata", ex);
        }
        return false;
    }

    public boolean hasColumn(String tableName, String columnName) {
        try (Connection connection = getConnection()) {
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, columnName);
            return rs.next();
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Error checking if column exists.", ex);
        }
        return false;
    }

    public Optional<HikariDataSource> getDataSource() {
        try {
            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setJdbcUrl("jdbc:h2:" + new File(plugin.configDir.toFile(), Config.databaseFile).getAbsolutePath());
            ds.setConnectionTimeout(1000);
            ds.setLoginTimeout(5);
            ds.setAutoCommit(true);
            return Optional.ofNullable(ds);
        } catch (SQLException ex) {
            plugin.getLogger().error("H2: Failed to get datastore.", ex);
            return Optional.empty();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.get().getConnection();
    }

}
