package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.configurer.BreweryXConfigurer;
import com.dre.brewery.configuration.sector.CauldronSector;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import lombok.Getter;
import lombok.Setter;

@Header({
        "-- Ingredients in the Cauldron --",
        "Which Ingredients are accepted by the Cauldron and the base potion resulting from them",
        "You only need to add something here if you want to specify a custom name or color for the base potion",
        "",
        "name: Name of the base potion coming out of the Cauldron (Formatting codes possible: such as &6)",
        "",
        "ingredients: List of 'material/amount'",
        "  With an item in your hand, use /brew ItemName to get its material for use in a recipe",
        "  (Item-ids instead of material are not supported by bukkit anymore and will not work)",
        "  A list of materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html",
        "",
        "color: Color of the base potion from a cauldron. Defaults to CYAN",
        "  Usable Colors: DARK_RED, RED, BRIGHT_RED, ORANGE, YELLOW, PINK, PURPLE, BLUE, CYAN, WATER, TEAL, OLIVE, GREEN, LIME, BLACK, GREY, BRIGHT_GREY, WHITE",
        "  Or RGB colors (hex: for example '99FF33') (with '') (search for \"HTML color\" on the internet)",
        "",
        "cookParticles:",
        "  Color of the Particles above the cauldron at different cooking-times",
        "  Color and minute during which each color should appear, i.e. one color at 8 minutes fading to another at 18 minutes.",
        "  As List, each Color as name or RGB, see above. Written as 'Color/Minute'",
        "  It will fade to the last color in the end, if there is only one color in the list, it will fade to grey",
        "",
        "lore: List of additional text on the base potion. (Formatting codes possible: such as &6 or hex as #&<hex>)",
        "",
        "customModelData: Custom Model Data Tag. This is a number that can be used to add custom textures to the item."
})
@Getter @Setter
public class CauldronFile extends AbstractOkaeriConfigFile {
    @Getter @Exclude
    private static final CauldronFile instance = createConfig(CauldronFile.class, "cauldron.yml", new BreweryXConfigurer());

    // TODO: See RecipesFile comment here.
    private CauldronSector cauldron = new CauldronSector();
}
