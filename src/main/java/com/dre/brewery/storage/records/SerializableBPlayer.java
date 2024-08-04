package com.dre.brewery.storage.records;

import com.dre.brewery.BPlayer;

/**
 * Represents a player that can be serialized.
 * @param id The UUID of the player
 * @param quality The quality of the player
 * @param drunkenness The drunkenness of the player
 * @param offlineDrunkenness The offline drunkenness of the player
 */
public record SerializableBPlayer(String id, int quality, int drunkenness, int offlineDrunkenness) implements SerializableThing {
    public SerializableBPlayer(BPlayer player) {
        this(player.getUuid(), player.getQuality(), player.getDrunkeness(), player.getOfflineDrunkeness());
    }

    public BPlayer toBPlayer() {
        return new BPlayer(id, quality, drunkenness, offlineDrunkenness);
    }

    @Override
    public String getId() {
        return id;
    }
}
