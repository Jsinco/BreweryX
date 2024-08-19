package com.dre.brewery.storage.records;

import java.util.List;

/**
 * Miscellaneous save data about brewery.
 * These were added by the original author(s) and their source/usage hasn't been completely
 * read through by me.
 */
public class BreweryMiscData implements SerializableThing {

    private long installTime;
    private long mcBarrelTime;
    private List<Long> prevSaveSeeds;
    private List<Integer> brewsCreated;
    private int brewsCreatedHash;


    public BreweryMiscData(long installTime, long mcBarrelTime, List<Long> prevSaveSeeds, List<Integer> brewsCreated, int brewsCreatedHash) {
        this.installTime = installTime;
        this.mcBarrelTime = mcBarrelTime;
        this.prevSaveSeeds = prevSaveSeeds;
        this.brewsCreated = brewsCreated;
        this.brewsCreatedHash = brewsCreatedHash;
    }

    @Override
    public String getId() {
        return "misc";
    }


    public long installTime() {
        return installTime;
    }

    public long mcBarrelTime() {
        return mcBarrelTime;
    }

    public List<Long> prevSaveSeeds() {
        return prevSaveSeeds;
    }

    public List<Integer> brewsCreated() {
        return brewsCreated;
    }

    public int brewsCreatedHash() {
        return brewsCreatedHash;
    }
}
