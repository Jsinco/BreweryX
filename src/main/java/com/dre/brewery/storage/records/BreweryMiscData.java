package com.dre.brewery.storage.records;

import java.util.List;

/**
 * Miscellaneous save data about brewery.
 * These were added by the original author(s) and their source/usage hasn't been completely
 * read through by me.
 */
public record BreweryMiscData(long installTime, long mcBarrelTime, List<Long> prevSaveSeeds, List<Integer> brewsCreated,
                              int brewsCreatedHash) implements SerializableThing {

    @Override
    public String getId() {
        return "misc";
    }
}
