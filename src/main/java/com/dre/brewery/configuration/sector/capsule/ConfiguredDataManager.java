package com.dre.brewery.configuration.sector.capsule;

import com.dre.brewery.configuration.annotation.CommentSpace;
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

    @CommentSpace(0)
    @LocalizedComment("config.storage.type")
    private DataManagerType type;
    @CommentSpace(0)
    @LocalizedComment("config.storage.database")
    private String database;
    private String tablePrefix;
    private String address;
    private String username;
    private String password;
}
