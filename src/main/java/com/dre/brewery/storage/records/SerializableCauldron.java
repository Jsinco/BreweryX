package com.dre.brewery.storage.records;

import java.util.UUID;

/**
 * Represents a cauldron that can be serialized.
 * @param id The UUID of the cauldron
 * @param serializedLocation The Block/Location of the cauldron
 * @param serializedIngredients Serialized BIngredients 'BIngredients.deserialize(String)'
 * @param state The state
 */
public record SerializableCauldron(UUID id, String serializedLocation, String serializedIngredients, int state) {
}
