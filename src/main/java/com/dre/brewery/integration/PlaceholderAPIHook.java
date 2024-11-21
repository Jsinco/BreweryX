package com.dre.brewery.integration;

import com.dre.brewery.integration.papi.PlaceholderAPI;

public class PlaceholderAPIHook extends Hook {

    public static final PlaceholderAPIHook PLACEHOLDERAPI = new PlaceholderAPIHook("PlaceholderAPI");

    private PlaceholderAPI instance;

    public PlaceholderAPIHook(String name) {
        super(name);
    }

    public PlaceholderAPI getInstance() {
        if (instance == null && isEnabled()) {
            instance = new PlaceholderAPI();
        }
        return instance;
    }

}
