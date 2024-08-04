package com.dre.brewery.storage.records;

import java.util.UUID;

/**
 * Represents a player that can be serialized.
 * @param id The UUID of the player
 * @param quality The quality of the player
 * @param drunkenness The drunkenness of the player
 * @param offlineDrunkenness The offline drunkenness of the player
 */
public record SerializableBPlayer(UUID id, int quality, int drunkenness, int offlineDrunkenness) {
}
