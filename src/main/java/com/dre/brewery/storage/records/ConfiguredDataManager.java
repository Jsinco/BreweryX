package com.dre.brewery.storage.records;

import com.dre.brewery.configuration.annotation.LocalizedComment;
import com.dre.brewery.storage.DataManagerType;
import eu.okaeri.configs.OkaeriConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class ConfiguredDataManager extends OkaeriConfig {

    @LocalizedComment("config.storage.type")
    private final DataManagerType type;
    @LocalizedComment("config.storage.database")
    private final String database;
    private final String tablePrefix;
    private final String address;
    private final String username;
    private final String password;
}
