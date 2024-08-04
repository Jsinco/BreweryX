package com.dre.brewery.storage.records;

import java.util.UUID;

/**
 * Represents a wakeup that can be serialized.
 * @param id The UUID of the wakeup
 * @param serializedLocation The Location of the wakeup
 */
public record SerializableWakeup(UUID id, String serializedLocation) {
}
