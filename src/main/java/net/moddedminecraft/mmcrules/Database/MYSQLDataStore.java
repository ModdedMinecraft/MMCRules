package net.moddedminecraft.mmcrules.Database;

import com.zaxxer.hikari.HikariDataSource;
import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MYSQLDataStore implements IDataStore  {

    private final Main plugin;
    private final Optional<HikariDataSource> dataSource;

    public MYSQLDataStore(Main plugin) {
        this.plugin = plugin;
        this.dataSource = getDataSource();
    }

    @Override
    public String getDatabaseName() {
        return "MySQL";
    }

    @Override
    public boolean load() {
        if (!dataSource.isPresent()) {
            plugin.getLogger().error("Selected datastore: 'MySQL' is not avaiable please select another datastore.");
            return false;
        }
        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + Config.mysqlPrefix + "players ("
                    + " playeruuid VARCHAR(60) NOT NULL PRIMARY KEY"
                    + ");");

            getConnection().commit();
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Unable to create tables", ex);
            return false;
        }
        return true;
    }

    @Override
    public List<String> getAccepted() {
        List<String> uuidList = new ArrayList<>();

        try (Connection connection = getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + Config.mysqlPrefix + "players");
            while (rs.next()) {
                uuidList.add(rs.getString("playeruuid"));
            }
            return uuidList;
        } catch (SQLException ex) {
            plugin.getLogger().info("MySQL: Couldn't read ticketdata from MySQL database.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addPlayer(String uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + Config.mysqlPrefix + "players VALUES (?);");
            statement.setString(1, uuid);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Error adding playerdata", ex);
        }
        return false;
    }

    @Override
    public boolean removePlayer(String uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + Config.mysqlPrefix + "players WHERE playeruuid = " + uuid + ";");
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Error removing playerdata", ex);
        }
        return false;
    }

    @Override
    public boolean clearList() {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE " + Config.mysqlPrefix + "players;");
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Error removing playerdata", ex);
        }
        return false;
    }

    public boolean hasColumn(String tableName, String columnName) {
        try (Connection connection = getConnection()) {
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, columnName);
            return rs.next();
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Error checking if column exists.", ex);
        }
        return false;
    }

    public Optional<HikariDataSource> getDataSource() {
        try {
            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName("org.mariadb.jdbc.Driver");
            ds.setJdbcUrl("jdbc:mariadb://"
                    + Config.mysqlHost
                    + ":" + Config.mysqlPort
                    + "/" +  Config.mysqlDatabase);
            ds.addDataSourceProperty("user", Config.mysqlUser);
            ds.addDataSourceProperty("password", Config.mysqlPass);
            ds.setConnectionTimeout(1000);
            ds.setLoginTimeout(5);
            ds.setAutoCommit(true);
            return Optional.ofNullable(ds);
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Failed to get datastore.", ex);
            return Optional.empty();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.get().getConnection();
    }

}

