package com.dre.brewery.configuration.sector.capsule;

import eu.okaeri.configs.OkaeriConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Material;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCustomItem extends OkaeriConfig {

    private boolean matchAny;
    private Object material; // List<Material> or Material
    private Object name; // String or List<String>
    private Object lore; // List<String> or String
    private Object customModelData; // List<Integer> or Integer
}
