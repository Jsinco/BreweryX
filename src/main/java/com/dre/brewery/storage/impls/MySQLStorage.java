package com.dre.brewery.storage.impls;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.Wakeup;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.storage.serialization.SQLDataSerializer;
import com.dre.brewery.storage.StorageInitException;
import com.dre.brewery.storage.records.BreweryMiscData;
import com.dre.brewery.storage.records.ConfiguredDataManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MySQLStorage extends DataManager {

    private static final String URL = "jdbc:mysql://";
    private static final String[] TABLES = {
            "misc (data TEXT, PRIMARY KEY (data))",
            "barrels (id VARCHAR(36), data TEXT, PRIMARY KEY (id))",
            "cauldrons (id VARCHAR(36), data TEXT, PRIMARY KEY (id))",
            "players (id VARCHAR(36), data TEXT, PRIMARY KEY (id))",
            "wakeups (id VARCHAR(36), data TEXT, PRIMARY KEY (id))"
    };

    private final SQLDataSerializer serializer = new SQLDataSerializer();
    private final Connection connection;
    private final String tablePrefix;

    public MySQLStorage(ConfiguredDataManager record) throws StorageInitException {
        try {
            this.connection = DriverManager.getConnection(URL + record.address(), record.username(), record.password());
            this.tablePrefix = record.tablePrefix();
        } catch (SQLException e) {
            throw new StorageInitException("Failed to connect to MySQL database! (Did you configure it correctly?)", e);
        }

        try {
            for (String table : TABLES) {
                executeSQL("CREATE TABLE IF NOT EXISTS " + tablePrefix + table);
            }
        } catch (SQLException e) {
            throw new StorageInitException("Failed to create tables!", e);
        }
    }

    @Override
    protected void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            plugin.errorLog("Failed to close MySQL connection!", e);
        }
    }

    private void executeSQL(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }


    @Override
    public Barrel getBarrel(UUID id) {
        return null;
    }

    @Override
    public Collection<Barrel> getAllBarrels() {
        return List.of();
    }

    @Override
    public void saveAllBarrels(Collection<Barrel> barrels, boolean overwrite) {

    }

    @Override
    public void saveBarrel(Barrel barrel) {

    }

    @Override
    public void deleteBarrel(UUID id) {

    }

    @Override
    public BCauldron getCauldron(UUID id) {
        return null;
    }

    @Override
    public Collection<BCauldron> getAllCauldrons() {
        return List.of();
    }

    @Override
    public void saveAllCauldrons(Collection<BCauldron> cauldrons, boolean overwrite) {

    }

    @Override
    public void saveCauldron(BCauldron cauldron) {

    }

    @Override
    public void deleteCauldron(UUID id) {

    }

    @Override
    public BPlayer getPlayer(UUID playerUUID) {
        return null;
    }

    @Override
    public Collection<BPlayer> getAllPlayers() {
        return List.of();
    }

    @Override
    public void saveAllPlayers(Collection<BPlayer> players, boolean overwrite) {

    }

    @Override
    public void savePlayer(BPlayer player) {

    }

    @Override
    public void deletePlayer(UUID playerUUID) {

    }

    @Override
    public Wakeup getWakeup(UUID id) {
        return null;
    }

    @Override
    public Collection<Wakeup> getAllWakeups() {
        return List.of();
    }

    @Override
    public void saveAllWakeups(Collection<Wakeup> wakeups, boolean overwrite) {

    }

    @Override
    public void saveWakeup(Wakeup wakeup) {

    }

    @Override
    public void deleteWakeup(UUID id) {

    }

    @Override
    public BreweryMiscData getBreweryMiscData() {
        return null;
    }

    @Override
    public void saveBreweryMiscData(BreweryMiscData data) {

    }
}
