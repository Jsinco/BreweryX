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

package com.dre.brewery;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum BarrelWoodType {

    ANY("Any", 0),
    BIRCH("Birch", 1),
    OAK("Oak", 2),
    JUNGLE("Jungle", 3),
    SPRUCE("Spruce", 4),
    ACACIA("Acacia", 5),
    DARK_OAK("Dark Oak", 6),
    CRIMSON("Crimson", 7),
    WARPED("Warped", 8),
    MANGROVE("Mangrove", 9),
    CHERRY("Cherry", 10),
    BAMBOO("Bamboo", 11),
    CUT_COPPER("Cut Copper", 12),
    PALE_OAK("Pale Oak", 13),
    // If you're adding more wood types, add them above 'NONE'
    NONE("None", -1);


    private final String formattedName;
    private final int index;

    BarrelWoodType(String formattedName, int index) {
        this.formattedName = formattedName;
        this.index = index;
    }

    public static BarrelWoodType fromName(String name) {
        for (BarrelWoodType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.formattedName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return ANY;
    }

    public static BarrelWoodType fromIndex(int index) {
        for (BarrelWoodType type : values()) {
            if (type.index == index) {
                return type;
            }
        }
        return ANY;
    }

    public static BarrelWoodType fromMaterial(Material material) {
        for (BarrelWoodType type : values()) {
            if (material.name().toUpperCase().startsWith(type.name().toUpperCase())) {
                return type;
            }
        }
        return ANY;
    }


    public static BarrelWoodType fromAny(Object intOrString) {
        if (intOrString instanceof Integer integer) {
            return fromIndex(integer);
        } else if (intOrString instanceof String s) {
            return fromName(s);
        } else if (intOrString instanceof Float || intOrString instanceof Double) {
            return fromIndex((int) (float) intOrString);
        } else if (intOrString instanceof Material m) {
            return fromMaterial(m);
        }
        return ANY;
    }
}
