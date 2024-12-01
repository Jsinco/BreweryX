package com.dre.brewery.configuration;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.DistortChat;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.configurer.BreweryXConfigurer;
import com.dre.brewery.configuration.configurer.TranslationManager;
import com.dre.brewery.configuration.files.CauldronFile;
import com.dre.brewery.configuration.files.Config;
import com.dre.brewery.configuration.files.RecipesFile;
import com.dre.brewery.configuration.sector.capsule.ConfigDistortWord;
import com.dre.brewery.configuration.serdes.MaterialTransformer;
import com.dre.brewery.integration.item.BreweryPluginItem;
import com.dre.brewery.integration.item.ItemsAdderPluginItem;
import com.dre.brewery.integration.item.MMOItemsPluginItem;
import com.dre.brewery.integration.item.OraxenPluginItem;
import com.dre.brewery.integration.item.SlimefunPluginItem;
import com.dre.brewery.recipe.BCauldronRecipe;
import com.dre.brewery.recipe.BRecipe;
import com.dre.brewery.recipe.PluginItem;
import com.dre.brewery.utility.Logging;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigManager {

    public static final Map<Class<? extends AbstractOkaeriConfigFile>, AbstractOkaeriConfigFile> LOADED_CONFIGS = new HashMap<>();

    private static final Path DATA_FOLDER = BreweryPlugin.getInstance().getDataFolder().toPath();
    private static final Map<Class<? extends Configurer>, Supplier<Configurer>> CONFIGURERS = Map.of(
            BreweryXConfigurer.class, BreweryXConfigurer::new,
            YamlSnakeYamlConfigurer.class, YamlSnakeYamlConfigurer::new
    );
    private static final List<Supplier<BidirectionalTransformer<?, ?>>> TRANSFORMERS = List.of(
            MaterialTransformer::new
    );


    /**
     * Get a config instance from the LOADED_CONFIGS map, or create a new instance if it doesn't exist
     * @param configClass The class of the config to get
     * @return The config instance
     * @param <T> The type of the config
     */
    public static <T extends AbstractOkaeriConfigFile> T getConfig(Class<T> configClass) {
        try {
            for (var mapEntry : LOADED_CONFIGS.entrySet()) {
                if (mapEntry.getKey().equals(configClass)) {
                    return (T) mapEntry.getValue();
                }
            }
            return createConfig(configClass);
        } catch (Throwable e) {
            Logging.errorLog("Something went wrong trying to load a config file! &e(" + configClass.getSimpleName() + ".yml)", e);
            Logging.warningLog("Resolve the issue in the file and run &6/brewery reload");
            return createBlankConfigInstance(configClass);
        }
    }

    /**
     * Replaces a config instance in the LOADED_CONFIGS map with a new instance of the same class
     * @param configClass The class of the config to replace
     * @param <T> The type of the config
     */
    public static <T extends AbstractOkaeriConfigFile> void newInstance(Class<T> configClass, boolean overwrite) {
        if (!overwrite && LOADED_CONFIGS.containsKey(configClass)) {
            return;
        }
        LOADED_CONFIGS.put(configClass, createConfig(configClass));
    }


    /**
     * Get the file path of a config class
     * @param configClass The class of the config to get the file name of
     * @return The file name
     * @param <T> The type of the config
     */
    public static <T extends AbstractOkaeriConfigFile> Path getFilePath(Class<T> configClass) {
        OkaeriConfigFileOptions options = getOkaeriConfigFileOptions(configClass);

        if (!options.useLangFileName()) {
            return DATA_FOLDER.resolve(options.value());
        } else {
            return DATA_FOLDER.resolve("languages/" + TranslationManager.getInstance().getActiveTranslation().getFilename());
        }
    }




    /**
     * Create a new config instance with a custom file name, configurer, serdes pack, and puts it in the LOADED_CONFIGS map
     * @param configClass The class of the config to create
     * @param file The file to use
     * @param configurer The configurer to use
     * @param serdesPack The serdes pack to use
     * @return The new config instance
     * @param <T> The type of the config
     */
    private static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, Path file, Configurer configurer, OkaeriSerdesPack serdesPack, boolean update, boolean removeOrphans) {
        boolean firstCreation = !Files.exists(file);

        T instance = eu.okaeri.configs.ConfigManager.create(configClass, (it) -> {
            it.withConfigurer(configurer, serdesPack);
            it.withRemoveOrphans(removeOrphans);
            it.withBindFile(file);
            it.saveDefaults();


            for (Supplier<BidirectionalTransformer<?, ?>> packSupplier : TRANSFORMERS) {
                it.withSerdesPack(registry -> registry.register(packSupplier.get()));
            }
            it.load(update);
        });

        instance.setUpdate(update);
        instance.setFirstCreation(firstCreation);
        LOADED_CONFIGS.put(configClass, instance);
        return instance;
    }

    /**
     * Create a new config instance using a config class' annotation
     * @param configClass The class of the config to create
     * @return The new config instance
     * @param <T> The type of the config
     */
    private static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass) {
        OkaeriConfigFileOptions options = configClass.getAnnotation(OkaeriConfigFileOptions.class);

        return createConfig(configClass, getFilePath(configClass), CONFIGURERS.get(options.configurer()).get(), new StandardSerdes(), options.update(), options.removeOrphans());
    }

	@Nullable
	private static <T extends AbstractOkaeriConfigFile> T createBlankConfigInstance(Class<T> configClass) {
		try {
			T inst = configClass.getDeclaredConstructor().newInstance();
            inst.setBlankInstance(true);
			LOADED_CONFIGS.put(configClass, inst);
			return inst;
		} catch (Exception e) {
            Logging.errorLog("Failed to create a blank config instance for " + configClass.getSimpleName(), e);
			return null;
		}
	}


    // Util

    public static void createFileFromResources(String resourcesPath, Path destination) {
        Path targetDir = destination.getParent();

        try {
            // Ensure the directory exists, create it if necessary
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            if (Files.exists(destination)) {
                return;
            }

            try (InputStream inputStream = BreweryPlugin.class.getClassLoader().getResourceAsStream(resourcesPath)) {

                if (inputStream != null) {
                    // Copy the input stream content to the target file
                    Files.copy(inputStream, destination);
                } else {
                    Logging.warningLog("Could not find resource file for " + resourcesPath);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating or copying file", e);
        }
    }


    private static OkaeriConfigFileOptions getOkaeriConfigFileOptions(Class<? extends AbstractOkaeriConfigFile> configClass) {
        OkaeriConfigFileOptions options = configClass.getAnnotation(OkaeriConfigFileOptions.class);
        if (options == null) {
            options = new OkaeriConfigFileOptions() {
                @Override public Class<? extends Annotation> annotationType() { return OkaeriConfigFileOptions.class; }
                @Override public Class<? extends Configurer> configurer() { return BreweryXConfigurer.class; }
                @Override public boolean useLangFileName() { return false; }
                @Override public boolean update() { return false; }
                @Override public boolean removeOrphans() { return false; }
                @Override public String value() { return configClass.getSimpleName().toLowerCase() + ".yml"; }
            };
        }
        return options;
    }


    // Not really what I want to do, but I have to move these from BConfig right now

    public static void loadRecipes() {
        // loading recipes
        List<BRecipe> configRecipes = BRecipe.getConfigRecipes();
        configRecipes.clear();

        for (var recipeEntry : getConfig(RecipesFile.class).getRecipes().entrySet()) {
            BRecipe recipe = BRecipe.fromConfig(recipeEntry.getKey(), recipeEntry.getValue());
            if (recipe != null && recipe.isValid()) {
                configRecipes.add(recipe);
            } else {
                Logging.errorLog("Loading the Recipe with id: '" + recipeEntry.getKey() + "' failed!");
            }

            BRecipe.setNumConfigRecipes(configRecipes.size());
        }
    }


    public static void loadCauldronIngredients() {
        // Loading Cauldron Recipes

        List<BCauldronRecipe> configRecipes = BCauldronRecipe.getConfigRecipes();
        configRecipes.clear();

        for (var cauldronEntry : getConfig(CauldronFile.class).getCauldronIngredients().entrySet()) {
            BCauldronRecipe recipe = BCauldronRecipe.fromConfig(cauldronEntry.getKey(), cauldronEntry.getValue());
            if (recipe != null) {
                configRecipes.add(recipe);
            } else {
                Logging.errorLog("Loading the Cauldron-Recipe with id: '" + cauldronEntry.getKey() + "' failed!");
            }
        }
        BCauldronRecipe.setNumConfigRecipes(configRecipes.size());

        // Recalculating Cauldron-Accepted Items for non-config recipes
        for (BRecipe recipe : BRecipe.getAddedRecipes()) {
            recipe.updateAcceptedLists();
        }
        for (BCauldronRecipe recipe : BCauldronRecipe.getAddedRecipes()) {
            recipe.updateAcceptedLists();
        }
    }

    public static void loadDistortWords() {
        // Loading Words
        Config config = getConfig(Config.class);

        if (config.isEnableChatDistortion()) {
            for (ConfigDistortWord distortWord : config.getWords()) {
                new DistortChat(distortWord);
            }
            for (String bypass : config.getDistortBypass()) {
                DistortChat.getIgnoreText().add(bypass.split(","));
            }
            DistortChat.getCommands().addAll(config.getDistortCommands());
        }
    }

    public static void registerDefaultPluginItems() {
        PluginItem.registerForConfig("brewery", BreweryPluginItem::new);
        PluginItem.registerForConfig("mmoitems", MMOItemsPluginItem::new);
        PluginItem.registerForConfig("slimefun", SlimefunPluginItem::new);
        PluginItem.registerForConfig("exoticgarden", SlimefunPluginItem::new);
        PluginItem.registerForConfig("oraxen", OraxenPluginItem::new);
        PluginItem.registerForConfig("itemsadder", ItemsAdderPluginItem::new);
    }
}
