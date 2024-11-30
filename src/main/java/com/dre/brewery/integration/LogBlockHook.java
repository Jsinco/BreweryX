package com.dre.brewery.integration;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.integration.barrel.BlocklockerBarrel;
import com.dre.brewery.utility.Logging;

public class LogBlockHook extends Hook {

    public static final LogBlockHook LOGBLOCK = new LogBlockHook("LogBlock", config.isUseLogBlock());

    public LogBlockHook(String name, boolean enabled) {
        super(name, enabled);
        try {
            Class.forName("nl.rutgerkok.blocklocker.BlockLockerAPIv2");
            Class.forName("nl.rutgerkok.blocklocker.ProtectableBlocksSettings");
            BlocklockerBarrel.registerBarrelAsProtectable();
        } catch (ClassNotFoundException e) {
            this.enabled = false;
            Logging.log("Unsupported Version of 'BlockLocker', locking Brewery Barrels disabled");
        }
    }
}
