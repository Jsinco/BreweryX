package com.dre.brewery.configuration.sector;

import eu.okaeri.configs.OkaeriConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomItemsSector extends OkaeriConfig {

    // TODO: There might actually be no point in manually declaring default recipes/cauldron items/custom items programmatically,
    // Just generating the file from /resources/ and reading it using Okaeri should be fine?
}
