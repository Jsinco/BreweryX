package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.configurer.BreweryXConfigurer;
import com.dre.brewery.configuration.sector.RecipesSector;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import lombok.Getter;
import lombok.Setter;

// I want to add support for an external recipes file along with BreweryX being able to read
// Recipes from the default config too. Gotta figure this out later
@Header({
        "-- Recipes for Potions --",
        "",
        "Proper guide for this section can be found in our wiki here - https://brewery.lumamc.net/guide/recipies/",
        "",
        "name: Different names for bad/normal/good (Formatting codes possible: such as &6 or hex as &#123123)",
        "  name: \"Worst drink/Good Drink/Best drink i had in my entire life!\"",
        "",
        "ingredients: List of 'material/amount'",
        "  With an item in your hand, use /brew ItemName to get its material for use in a recipe",
        "  A list of materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html",
        "  Plugin items with 'plugin:id' (Currently supporting Brewery, Oraxen, ItemsAdder)",
        "  Or a custom item defined above",
        "",
        "cookingtime: Time in real minutes ingredients have to boil",
        "",
        "distillruns: How often it has to be distilled for full alcohol (0=without distilling)",
        "",
        "distilltime: How long (in seconds) one distill-run takes (0=Default time of 40 sec) MC Default would be 20 sec",
        "",
        "wood: Wood of the barrel 0=any 1=Birch 2=Oak 3=Jungle 4=Spruce 5=Acacia 6=Dark Oak 7=Crimson 8=Warped 9=Mangrove 10=Cherry 11=Bamboo (12=Cut Copper)",
        "  The Minecraft barrel is made of oak",
        "",
        "age: Time in Minecraft-days, the potion has to age in a barrel 0=no aging",
        "",
        "color: Color of the potion after distilling/aging.",
        "  Usable Colors: DARK_RED, RED, BRIGHT_RED, ORANGE, YELLOW, PINK, PURPLE, BLUE, CYAN, WATER, TEAL, OLIVE, GREEN, LIME, BLACK, GREY, BRIGHT_GREY, WHITE",
        "  Or RGB colors (hex: for example '99FF33') (with '') (search for \"HTML color\" on the internet)",
        "",
        "difficulty: 1-10 accuracy needed to get good quality (1 = unaccurate/easy, 10 = very precise/hard)",
        "",
        "alcohol: Absolute amount of alcohol 0-100 in a perfect potion (will be added directly to the player, where 100 means fainting)",
        "",
        "lore: List of additional text on the finished brew. (Formatting codes possible: such as &6)",
        "  Specific lore for quality possible, using + bad, ++ normal, +++ good, added to the front of the line.",
        "  - +++ This is the best drink!",
        "  - ++ This is decent drink.",
        "  - + This is the worst drink",
        "",
        "servercommands: List of Commands executed by the -Server- when drinking the brew (Can use %player_name%  %quality%)",
        "  Specific Commands for quality possible, using + bad, ++ normal, +++ good, added to the front of the line.",
        "  - +++ op %player%",
        "  - ++ money give %player% 10",
        "  - + kill %player% ",
        " Command execution can be delayed by adding \"/ <amount>s\" to the end, like this:",
        " - op Jsinco / 3s",
        "",
        "playercommands: List of Commands executed by the -Player- when drinking the brew (Can use %player_name%  %quality%)",
        "  Specific Commands for quality possible, using + bad, ++ normal, +++ good, added to the front of the line.",
        "  - +++ spawn",
        "  - ++ home",
        "  - + suicide",
        " Command execution can be delayed by adding \"/ <amount>s\" to the end, like this:",
        " - op Jsinco / 3s",
        "",
        "drinkmessage: Chat-message to the Player when drinking the Brew",
        "",
        "glint: Boolean if the item should have a glint (enchant glint)",
        "customModelData: Custom Model Data Tag. This is a number that can be used to add custom textures to the item.",
        "  Can specify one for all, or one for each quality, separated by /",
        "  customModelData: 1",
        "  customModelData: 1/2/3",
        "",
        "effects: List of effect/level/duration  Special potion-effect when drinking, duration in seconds.",
        "  Possible Effects: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html",
        "  Level or Duration ranges may be specified with a \"-\", ex. 'SPEED/1-2/30-40' = lvl 1 and 30 sec at worst and lvl 2 and 40 sec at best",
        "  Ranges also work high-low, ex. 'POISON/3-1/20-5' for weaker effects at good quality.",
        "  Highest possible Duration: 1638 sec. Instant Effects dont need any duration specified."
})
@Getter @Setter
public class RecipesFile extends AbstractOkaeriConfigFile {
    @Getter @Exclude
    private static final RecipesFile instance = createConfig(RecipesFile.class, "recipes.yml", new BreweryXConfigurer());

    // TODO: Replace the header with a localized comment here so all of that text above can be translated
    private RecipesSector recipes = new RecipesSector();
}
