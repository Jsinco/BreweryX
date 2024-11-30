package com.dre.brewery.configuration;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.utility.Logging;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.exception.OkaeriException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;

@Getter @Setter
public abstract class AbstractOkaeriConfigFile extends OkaeriConfig {

    protected @Exclude transient boolean update = false;
    protected @Exclude transient boolean firstCreation = false;
    private @Exclude transient boolean blankInstance = false;

    // Util methods

    public void saveAsync() throws OkaeriException {
        CompletableFuture.runAsync(this::save);
    }

    @SneakyThrows
    public void reload() {
        if (this.blankInstance) {
            ConfigManager.newInstance(this.getClass(), true);
            this.blankInstance = false;
        } else {
            this.load(update);
        }
    }

    public void onFirstCreation() {
        Logging.log("Created a new configurable file: &6" + this.getBindFile().getFileName());
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
