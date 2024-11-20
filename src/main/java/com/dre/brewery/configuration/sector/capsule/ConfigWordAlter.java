package com.dre.brewery.configuration.sector.capsule;

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
public class ConfigWordAlter extends OkaeriConfig {

    private String replace;
    private String to;
    private String pre;
    private Boolean match;
    private Integer alcohol;
    private Integer percentage;
}
