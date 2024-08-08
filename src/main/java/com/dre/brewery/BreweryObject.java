package com.dre.brewery;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BreweryObject {

    @Expose protected UUID owner; // Which Brewery plugin owns this Barrel
    @Expose protected final String qualifier;
    @Expose protected final UUID id;

    public BreweryObject(UUID id, String qualifier) {
        this.id = id;
        this.qualifier = qualifier;
    }


    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public UUID getId() {
        return id;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void onRequest() {

    }
}
