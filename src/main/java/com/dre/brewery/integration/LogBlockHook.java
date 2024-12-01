package com.dre.brewery.integration;

import com.dre.brewery.integration.barrel.BlockLockerBarrel;
import com.dre.brewery.utility.Logging;

public class LogBlockHook extends Hook {

    public static final LogBlockHook LOGBLOCK = new LogBlockHook("LogBlock", config.isUseLogBlock());

    public LogBlockHook(String name, boolean enabled) {
        super(name, enabled);

        if (!isEnabled()) {
            return;
        }

        try {
            Class.forName("nl.rutgerkok.blocklocker.BlockLockerAPIv2");
            Class.forName("nl.rutgerkok.blocklocker.ProtectableBlocksSettings");
            BlockLockerBarrel.registerBarrelAsProtectable();
        } catch (ClassNotFoundException e) {
            this.enabled = false;
            Logging.log("Unsupported Version of 'BlockLocker', locking Brewery Barrels disabled");
        }
    }
}
