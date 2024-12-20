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

package com.dre.brewery.storage.interfaces;

import com.dre.brewery.storage.DataManager;

/**
 * Allows an external class (a class outside or inside) of this Plugin to be auto-saved by Brewery.
 * Auto saving will occur every X number of minutes and will also occur when this Plugin is disabled.
 * <p>
 * A class
 * implementing this interface must be registered with BreweryX auto-savable system. {@link DataManager#registerAutoSavable(ExternallyAutoSavable)}
 *
 * @see DataManager#registerAutoSavable(ExternallyAutoSavable)
 * @see DataManager#unregisterAutoSavable(ExternallyAutoSavable) 
 */
public interface ExternallyAutoSavable {

    /**
     * @return The identifier of the table. Should be unique.
     */
    String table();

    /**
     * The max length of the identifier for the PRIMARY KEY of the table.
     * This only matters if the user is using an SQL-relational database.
     * This specifies what {@code VARCHAR(size)} should be.
     * @return The max length of the table specified by the child class. Max length is 255.
     */
    default int tableMaxIdLength() {
        return 36; // Standard UUID length is 36
    }

    /**
     * Fired when Brewery is handling its auto-save task.
     * @param dataManager Instance of the DataManager
     */
    void onAutoSave(DataManager dataManager);
}
