package com.dre.brewery.configuration.sector.capsule;

import eu.okaeri.configs.OkaeriConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Material;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class ConfigCustomItem extends OkaeriConfig {

    private boolean matchAny;
    private List<Material> material;
    private List<String> name;
    private List<String> lore;
    private List<Integer> customModelData;
}
