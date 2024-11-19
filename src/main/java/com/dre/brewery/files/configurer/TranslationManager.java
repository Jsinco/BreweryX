package com.dre.brewery.files.configurer;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.files.Config;
import com.dre.brewery.utility.BUtil;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

public class TranslationManager {

	private static final BreweryPlugin plugin = BreweryPlugin.getInstance();

    private final ConfigTranslations translations;
	private final ConfigTranslations fallbackTranslations;

	public TranslationManager() {
		Yaml yaml = new Yaml();

		// Ok so,
		// Our config depends on our TranslationManager in order to load properly.
		// The problem is, the TranslationManager needs to be provided a translation in order to load,
		// in order to preserve the configurability of the language using the 'config.yml' instead of a separate file,
		// we have to grab the language from the config.yml before Okaeri loads it, so that's what we're doing right here.
		// Annoying race condition, but so be it.
		Translation activeTranslation = Translation.EN;

		try (InputStream inputStream = Files.newInputStream(plugin.getDataFolder().toPath().resolve(Config.FILE_NAME))) {

            Map<String, String> data = yaml.load(inputStream);
			if (data != null) {
				Translation trans = BUtil.getEnumByName(Translation.class, data.get("language"));
				if (trans != null) {
					activeTranslation = trans;
				}
			}
		} catch (IOException ignored) {
			// file probably doesn't exist yet
			plugin.debugLog("No config file found, using default translation");
		}

		this.translations = new ConfigTranslations(activeTranslation, yaml);
		this.fallbackTranslations = new ConfigTranslations(Translation.EN, yaml);
	}

	/**
	 * Attempts to retrieve a translation for the given key with fallback logic.
	 */
	@Nullable
	public String getTranslationWithFallback(String key) {
		String activeTranslation = translations.getTranslation(key);
		if (activeTranslation != null) {
			return activeTranslation;
		}

		// Fallback to the english translation
		String fallbackTranslation = fallbackTranslations.getTranslation(key);
		if (fallbackTranslation == null) {
			BreweryPlugin.getInstance().warningLog("No translation found for key: " + key);
		}

		return fallbackTranslation;
	}
}
