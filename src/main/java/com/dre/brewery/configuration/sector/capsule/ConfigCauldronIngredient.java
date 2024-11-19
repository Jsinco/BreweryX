package com.dre.brewery.configuration.sector.capsule;

import eu.okaeri.configs.OkaeriConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ConfigCauldronIngredient extends OkaeriConfig {

    private String name;
    private List<String> ingredients;

    private String color;
    private List<String> cookParticles;
    private List<String> lore;
    private int customModelData;
}
