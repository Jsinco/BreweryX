package com.dre.brewery.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.exception.OkaeriException;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractOkaeriConfigFile extends OkaeriConfig {

    // Util methods

    public void saveAsync() throws OkaeriException {
        CompletableFuture.runAsync(this::save);
    }

    @SneakyThrows
    public void reload() {
        this.save();
        ConfigManager.newInstance(this);
    }
}
