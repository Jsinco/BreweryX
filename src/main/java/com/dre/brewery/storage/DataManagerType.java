package com.dre.brewery.storage;

public enum DataManagerType {
    // We can add whatever type of storage type we want! As long as it's implemented properly.
    // Maybe add: h2, mongodb, sqlite?
    FLATFILE,
    MYSQL,
    SQLITE
}
