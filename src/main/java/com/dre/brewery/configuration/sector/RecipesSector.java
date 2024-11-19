package com.dre.brewery.configuration.sector;

import com.dre.brewery.configuration.sector.capsule.ConfigRecipe;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// TODO: Configuration sections for recipes
@Getter
@Setter
public class RecipesSector extends OkaeriConfig {

    // TODO: There might actually be no point in manually declaring default recipes/cauldron items/custom items programmatically,
    // Just generating the file from /resources/ and reading it using Okaeri should be fine?

    @Comment("Example Recipe with every possible entry first:")
    ConfigRecipe ex = ConfigRecipe.builder()
            .name("Diamond/1/Example/Good Example")
            .ingredients(List.of("Bedrock/2", "Spruce_Planks/8", "Bedrock/1", "Brewery:Wheatbeer/2", "ExoticGarden:Grape/3", "ex-item/4"))
            .cookingTime(3)
            .distillRuns(2)
            .distillTime(60)
            .wood(4)
            .age(11)
            .color("DARK_RED")
            .difficulty(3)
            .alcohol(14)
            .lore(List.of("This is an example brew", "++Just a normal Example", "This text would be on the brew", "+ Smells disgusting", "++ Smells alright", "+++ Smells really good"))
            .serverCommands(List.of("+++ weather clear", "+ weather rain"))
            .playerCommands(List.of("homes"))
            .drinkMessage("Tastes good")
            .drinkTitle("Warms you from inside")
            .glint(true)
            .customModelData("556/557/557")
            .effects(List.of("FIRE_RESISTANCE/20", "HEAL/1", "WEAKNESS/2-3/50-60", "POISON/1-0/20-0"))
            .build();

    ConfigRecipe wheatbeer = ConfigRecipe.builder()
            .name("Skunky Wheatbeer/Wheatbeer/Fine Wheatbeer")
            .ingredients(List.of("Wheat/3"))
            .cookingTime(8)
            .distillRuns(0)
            .wood(1)
            .age(2)
            .color("ffb84d")
            .difficulty(1)
            .alcohol(5)
            .lore(List.of("+++ &8Refreshing"))
            .build();
}
