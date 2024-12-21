/*
 * BreweryX Bukkit-Plugin for an alternate brewing process
 * Copyright (C) 2024 The Brewery Team
 *
 * This file is part of BreweryX.
 *
 * BreweryX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BreweryX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BreweryX. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package com.dre.brewery.configuration;

import com.dre.brewery.utility.Logging;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.exception.OkaeriException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Getter @Setter
public abstract class AbstractOkaeriConfigFile extends OkaeriConfig {

    @Exclude
    protected transient boolean update = false;
    @Exclude
    protected transient boolean firstCreation = false;
    @Exclude
    protected transient boolean blankInstance = false;



    @SneakyThrows
    public void reload() {
        if (this.blankInstance) {
            ConfigManager.newInstance(this.getClass(), true);
            return;
        }

        this.bindFileExists(true);
        this.load(update);
    }

    public boolean bindFileExists(boolean createIfNotExist) throws IOException {
        if (this.blankInstance) { // Don't create if this is a placeholder instance
            return false;
        }
        boolean b = this.getBindFile().toFile().exists();
        if (!b && createIfNotExist){
            return this.getBindFile().toFile().createNewFile();
        }
        return b;
    }

    public void saveAsync() throws OkaeriException {
        CompletableFuture.runAsync(this::save); // swap with normal scheduler?
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
