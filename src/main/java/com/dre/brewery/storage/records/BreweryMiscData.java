package com.dre.brewery.storage.records;

import java.util.List;

/**
 * Miscellaneous save data about brewery.
 * These were added by the original author(s) and their source/usage hasn't been completely
 * read through.
 * <p>
 * These params may not be correct:
 * @param installTime The time brewery was installed
 * @param mcBarrelTime The time it takes for a barrel to brew
 * @param prevSaveSeeds Previous save seeds
 * @param brewsCreated Brews created. Implementers need to store the hashcode of this list.
 * @param brewsCreatedHash Hashcode of brews created
 */
public record BreweryMiscData(long installTime, long mcBarrelTime, List<Long> prevSaveSeeds, List<Integer> brewsCreated, int brewsCreatedHash) implements SerializableThing {
    @Override
    public String getId() {
        return "misc";
    }
}
