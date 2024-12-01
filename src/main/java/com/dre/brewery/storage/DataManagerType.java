package com.dre.brewery.storage;

public enum DataManagerType {
    // We can add whatever type of storage type we want! As long as it's implemented properly.
    // Maybe add: h2, mongodb, sqlite?
    FLATFILE("FlatFile"),
    MYSQL("MySQL"),
    SQLITE("SQLite"),
    MONGODB("MongoDB");

    private final String formattedName;

    DataManagerType(String formattedName) {
        this.formattedName = formattedName;
    }

    public String getFormattedName() {
        return formattedName;
    }
}
