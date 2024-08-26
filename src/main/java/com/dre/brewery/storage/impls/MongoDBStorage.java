package com.dre.brewery.storage.impls;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.Wakeup;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.storage.StorageInitException;
import com.dre.brewery.storage.records.BreweryMiscData;
import com.dre.brewery.storage.records.ConfiguredDataManager;
import com.dre.brewery.storage.records.SerializableBPlayer;
import com.dre.brewery.storage.records.SerializableBarrel;
import com.dre.brewery.storage.records.SerializableCauldron;
import com.dre.brewery.storage.records.SerializableThing;
import com.dre.brewery.storage.records.SerializableWakeup;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MongoDBStorage extends DataManager {

    private static final String URL = "mongodb+srv://%s:%s@%s/?retryWrites=true&w=majority&appName=BreweryX#%d";
    private static final String[] COLLECTIONS = {
            "misc", "barrels", "cauldrons", "players", "wakeups"
    };

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final String collectionPrefix;

    public MongoDBStorage(ConfiguredDataManager record) throws StorageInitException {
        String fullurl = String.format(URL, record.username(), record.password(), record.address(), this.getClass().hashCode());
        System.out.println(fullurl);
        this.mongoClient = MongoClients.create(fullurl);
        this.mongoClient.startSession();
        this.mongoDatabase = mongoClient.getDatabase(record.database());
        this.collectionPrefix = record.tablePrefix();

        for (String collection : COLLECTIONS) {
            mongoDatabase.createCollection(collectionPrefix + collection); // Create the collection if it doesn't exist
        }
        try {
            System.out.println("ttttt");
        } catch (Exception e) {
            throw new StorageInitException("Failed to start MongoDB client or get database!", e);
        }
    }

    @Override
    protected void closeConnection() {
        mongoClient.close();
    }

    private <T extends SerializableThing> T getGeneric(UUID id, String collection, Class<T> type) {
        MongoCollection<T> mongoCollection = mongoDatabase.getCollection(collectionPrefix + collection, type);
        return mongoCollection.find(Filters.eq("_id", id)).first();
    }

    private <T extends SerializableThing> T getGeneric(String id, String collection, Class<T> type) {
        MongoCollection<T> mongoCollection = mongoDatabase.getCollection(collectionPrefix + collection, type);
        return mongoCollection.find(Filters.eq("_id", id)).first();
    }

    private <T extends SerializableThing> List<T> getAllGeneric(String collection, Class<T> type) {
        MongoCollection<T> mongoCollection = mongoDatabase.getCollection(collectionPrefix + collection, type);
        return mongoCollection.find().into(new ArrayList<>());
    }


    private <T extends SerializableThing> void saveGeneric(T thing, String collection) {
        MongoCollection<T> mongoCollection = (MongoCollection<T>) mongoDatabase.getCollection(collectionPrefix + collection, thing.getClass());
        mongoCollection.replaceOne(Filters.eq("_id", thing.getId()), thing, new ReplaceOptions().upsert(true));
    }

    private <T extends SerializableThing> void saveAllGeneric(List<T> things, String collection) {
        MongoCollection<T> mongoCollection = (MongoCollection<T>) mongoDatabase.getCollection(collectionPrefix + collection, things.iterator().next().getClass());
        for (T thing : things) {
            mongoCollection.replaceOne(Filters.eq("_id", thing.getId()), thing, new ReplaceOptions().upsert(true));
        }
    }

    private void deleteGeneric(UUID id, String collection) {
        MongoCollection<SerializableThing> mongoCollection = mongoDatabase.getCollection(collectionPrefix + collection, SerializableThing.class);
        mongoCollection.deleteOne(Filters.eq("_id", id));
    }


    @Override
    public Barrel getBarrel(UUID id) {
        SerializableBarrel serializableBarrel = getGeneric(id, "barrels", SerializableBarrel.class);
        if (serializableBarrel != null) {
            return serializableBarrel.toBarrel();
        }
        return null;
    }

    @Override
    public Collection<Barrel> getAllBarrels() {
        return getAllGeneric("barrels", SerializableBarrel.class).stream()
                .map(SerializableBarrel::toBarrel)
                .toList();
    }

    @Override
    public void saveAllBarrels(Collection<Barrel> barrels, boolean overwrite) {
        List<SerializableBarrel> serializableBarrels = barrels.stream()
                .map(SerializableBarrel::new)
                .toList();
        saveAllGeneric(serializableBarrels, "barrels");
    }

    @Override
    public void saveBarrel(Barrel barrel) {
        saveGeneric(new SerializableBarrel(barrel), "barrels");
    }

    @Override
    public void deleteBarrel(UUID id) {
        deleteGeneric(id, "barrels");
    }

    @Override
    public BCauldron getCauldron(UUID id) {
        SerializableCauldron serializableCauldron = getGeneric(id, "cauldrons", SerializableCauldron.class);
        if (serializableCauldron != null) {
            return serializableCauldron.toCauldron();
        }
        return null;
    }

    @Override
    public Collection<BCauldron> getAllCauldrons() {
        return getAllGeneric("cauldrons", SerializableCauldron.class).stream()
                .map(SerializableCauldron::toCauldron)
                .toList();
    }

    @Override
    public void saveAllCauldrons(Collection<BCauldron> cauldrons, boolean overwrite) {
        List<SerializableCauldron> serializableCauldrons = cauldrons.stream()
                .map(SerializableCauldron::new)
                .toList();
        saveAllGeneric(serializableCauldrons, "cauldrons");
    }

    @Override
    public void saveCauldron(BCauldron cauldron) {
        saveGeneric(new SerializableCauldron(cauldron), "cauldrons");
    }

    @Override
    public void deleteCauldron(UUID id) {
        deleteGeneric(id, "cauldrons");
    }

    @Override
    public BPlayer getPlayer(UUID playerUUID) {
        SerializableBPlayer serializableBPlayer = getGeneric(playerUUID, "players", SerializableBPlayer.class);
        if (serializableBPlayer != null) {
            return serializableBPlayer.toBPlayer();
        }
        return null;
    }

    @Override
    public Collection<BPlayer> getAllPlayers() {
        return getAllGeneric("players", SerializableBPlayer.class).stream()
                .map(SerializableBPlayer::toBPlayer)
                .toList();
    }

    @Override
    public void saveAllPlayers(Collection<BPlayer> players, boolean overwrite) {
        List<SerializableBPlayer> serializableBPlayers = players.stream()
                .map(SerializableBPlayer::new)
                .toList();
        saveAllGeneric(serializableBPlayers, "players");
    }

    @Override
    public void savePlayer(BPlayer player) {
        saveGeneric(new SerializableBPlayer(player), "players");
    }

    @Override
    public void deletePlayer(UUID playerUUID) {
        deleteGeneric(playerUUID, "players");
    }

    @Override
    public Wakeup getWakeup(UUID id) {
        SerializableWakeup serializableWakeup = getGeneric(id, "wakeups", SerializableWakeup.class);
        if (serializableWakeup != null) {
            return serializableWakeup.toWakeup();
        }
        return null;
    }

    @Override
    public Collection<Wakeup> getAllWakeups() {
        return getAllGeneric("wakeups", SerializableWakeup.class).stream()
                .map(SerializableWakeup::toWakeup)
                .toList();
    }

    @Override
    public void saveAllWakeups(Collection<Wakeup> wakeups, boolean overwrite) {
        List<SerializableWakeup> serializableWakeups = wakeups.stream()
                .map(SerializableWakeup::new)
                .toList();
        saveAllGeneric(serializableWakeups, "wakeups");
    }

    @Override
    public void saveWakeup(Wakeup wakeup) {
        saveGeneric(new SerializableWakeup(wakeup), "wakeups");
    }

    @Override
    public void deleteWakeup(UUID id) {
        deleteGeneric(id, "wakeups");
    }

    @Override
    public BreweryMiscData getBreweryMiscData() {
        BreweryMiscData data = getGeneric("misc", "misc", BreweryMiscData.class);
        if (data != null) {
            return data;
        }
        return new BreweryMiscData(System.currentTimeMillis(), 0, new ArrayList<>(), new ArrayList<>(), 0);
    }

    @Override
    public void saveBreweryMiscData(BreweryMiscData data) {
        saveGeneric(data, "misc");
    }
}
