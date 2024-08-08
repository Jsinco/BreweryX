package com.dre.brewery.storage.records;

import com.dre.brewery.BPlayer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Represents a player that can be serialized.
 * @param id The UUID of the player
 * @param quality The quality of the player
 * @param drunkenness The drunkenness of the player
 * @param offlineDrunkenness The offline drunkenness of the player
 */
public record SerializableBPlayer(String id, int quality, int drunkenness, int offlineDrunkenness) implements SerializableThing {
    public SerializableBPlayer(BPlayer player) {
        this(player.getUuid().toString(), player.getQuality(), player.getDrunkeness(), player.getOfflineDrunkeness());
    }

    public BPlayer toBPlayer() {
        return new BPlayer(UUID.fromString(id), quality, drunkenness, offlineDrunkenness);
    }

    @Override
    public String getId() {
        return id;
    }

    public static List<SerializableBPlayer> fromBPlayers(Collection<BPlayer> players) {
        if (players == null) {
            return List.of();
        }
        return players.stream().map(SerializableBPlayer::new).toList();
    }

    public static List<BPlayer> toBPlayers(List<SerializableBPlayer> players) {
        if (players == null || players.isEmpty()) {
            return List.of();
        }
        return players.stream().map(SerializableBPlayer::toBPlayer).toList();
    }

    @Override
    public String toString() {
        return "SerializableBPlayer{" +
                "id='" + id + '\'' +
                ", quality=" + quality +
                ", drunkenness=" + drunkenness +
                ", offlineDrunkenness=" + offlineDrunkenness +
                '}';
    }
}
