package com.dre.brewery.configuration;

import com.dre.brewery.BreweryPlugin;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.exception.OkaeriException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;

@Getter @Setter
public abstract class AbstractOkaeriConfigFile extends OkaeriConfig {

    @Exclude
    protected transient boolean update = false;
    @Exclude
    protected transient boolean firstCreation = false;

    // Util methods

    public void saveAsync() throws OkaeriException {
        CompletableFuture.runAsync(this::save);
    }

    @SneakyThrows
    public void reload() {
        this.load(update);
    }

    public void onFirstCreation() {
        BreweryPlugin.getInstance().log("Created a new configurable file: &6" + this.getBindFile().getFileName());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "file=" + this.getBindFile().getFileName() +
                ", update=" + update +
                ", firstCreation=" + firstCreation +
                '}';
    }
}
