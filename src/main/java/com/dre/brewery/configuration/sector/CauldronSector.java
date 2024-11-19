package com.dre.brewery.configuration.sector;

import com.dre.brewery.configuration.sector.capsule.ConfigCauldronIngredient;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CauldronSector extends OkaeriConfig {

    // TODO: There might actually be no point in manually declaring default recipes/cauldron items/custom items programmatically,
    // Just generating the file from /resources/ and reading it using Okaeri should be fine?

    @Comment("Example Cauldron Ingredient with every possible entry first:")
    ConfigCauldronIngredient ex = ConfigCauldronIngredient.builder()
            .name("Example")
            .ingredients(List.of("Bedrock/2", "Diamond"))
            .color("BLACK")
            .cookParticles(List.of("RED/5", "WHITE/10", "800000/25"))
            .lore(List.of("An example for a Base Potion", "This is how it comes out of a Cauldron"))
            .customModelData(545)
            .build();

    @Comment("One Ingredient:")
    ConfigCauldronIngredient wheat = ConfigCauldronIngredient.builder()
            .name("Fermented wheat")
            .ingredients(List.of("Wheat"))
            .cookParticles(List.of("2d8686/8"))
            .build();
}
