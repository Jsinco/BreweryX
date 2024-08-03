package com.dre.brewery.storage.records;

import com.dre.brewery.storage.DataManagerType;

public record ConfiguredDataManager(DataManagerType type, String database, String address, String username, String password) {
}
