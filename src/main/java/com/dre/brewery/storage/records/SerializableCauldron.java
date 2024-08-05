package com.dre.brewery.storage.records;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BIngredients;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.utility.BUtil;

import java.util.Collection;
import java.util.List;

/**
 * Represents a cauldron that can be serialized.
 * @param id The UUID of the cauldron
 * @param serializedLocation The Block/Location of the cauldron
 * @param serializedIngredients Serialized BIngredients 'BIngredients.deserialize(String)'
 * @param state The state
 */
public record SerializableCauldron(String id, String serializedLocation, String serializedIngredients, int state) implements SerializableThing {
    public SerializableCauldron(BCauldron cauldron) {
        this(cauldron.getId().toString(), DataManager.serializeLocation(cauldron.getBlock().getLocation()), cauldron.getIngredients().serializeIngredients(), cauldron.getState());
    }

    public BCauldron toCauldron() {
        return new BCauldron(DataManager.deserializeLocation(serializedLocation).getBlock(), BIngredients.deserializeIngredients(serializedIngredients), state, BUtil.uuidFromString(id));
    }

    @Override
    public String getId() {
        return id;
    }


    public static List<SerializableCauldron> fromCauldrons(Collection<BCauldron> cauldrons) {
        if (cauldrons == null) {
            return List.of();
        }
        return cauldrons.stream().map(SerializableCauldron::new).toList();
    }

    public static List<BCauldron> toCauldrons(List<SerializableCauldron> cauldrons) {
        if (cauldrons == null) {
            return List.of();
        }
        return cauldrons.stream().map(SerializableCauldron::toCauldron).toList();
    }
}
