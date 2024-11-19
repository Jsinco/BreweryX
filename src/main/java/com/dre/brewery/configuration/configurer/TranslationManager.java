package com.dre.brewery.configuration.configurer;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.files.Config;
import com.dre.brewery.utility.BUtil;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Getter
public class TranslationManager {

	@Getter
	private static final Translation fallbackTranslation = Translation.EN;
	private static final BreweryPlugin plugin = BreweryPlugin.getInstance();
	private static TranslationManager singleton;


	private Translation activeTranslation;
    private final ConfigTranslations translations;
	private final ConfigTranslations fallbackTranslations;

	private TranslationManager() {
		Yaml yaml = new Yaml();

		// Ok so,
		// Our config depends on our TranslationManager in order to load properly.
		// The problem is, the TranslationManager needs to be provided a translation in order to load,
		// in order to preserve the configurability of the language using the 'config.yml' instead of a separate file,
		// we have to grab the language from the config.yml before Okaeri loads it, so that's what we're doing right here.
		// Annoying race condition, but so be it.
		this.activeTranslation = Translation.EN;

		try (InputStream inputStream =
					 Files.newInputStream(plugin.getDataFolder().toPath().resolve(ConfigManager.getFileName(Config.class)))) {

            Map<String, String> data = yaml.load(inputStream);
			if (data != null) {
				Translation trans = BUtil.getEnumByName(Translation.class, data.get("language"));
				if (trans != null) {
					this.activeTranslation = trans;
				}
			}
		} catch (IOException ignored) {
			// file probably doesn't exist yet
			plugin.debugLog("No config file found, using default translation");
		}

		this.translations = new ConfigTranslations(activeTranslation, yaml);
		this.fallbackTranslations = new ConfigTranslations(fallbackTranslation, yaml);

		// Create lang files from /resources/languages
		for (Translation translation : Translation.values()) {
			createLanguageFile(translation);
		}
	}

	/**
	 * Attempts to retrieve a translation for the given key with fallback logic.
	 */
	@Nullable
	public String getTranslationWithFallback(String key) {
		String activeTranslationString = translations.getTranslation(key);
		if (activeTranslationString != null) {
			return activeTranslationString;
		}

		// Fallback to the english translation
		String fallbackTranslationString = fallbackTranslations.getTranslation(key);
		if (fallbackTranslationString == null) {
			BreweryPlugin.getInstance().warningLog("No translation found for key: " + key);
		}

		return fallbackTranslationString;
	}

	public void createLanguageFile(Translation translation) {
		Path targetPath = plugin.getDataFolder().toPath().resolve("langs").resolve(translation.getFilename());
		Path targetDir = targetPath.getParent();

		try {
			// Ensure the directory exists, create it if necessary
			if (!Files.exists(targetDir)) {
				Files.createDirectories(targetDir);  // Creates the directory and any missing parent directories
			}

			// Check if the file exists
			if (!Files.exists(targetPath)) {
				try (InputStream inputStream = this.getClass()
						.getClassLoader()
						.getResourceAsStream("languages" + File.separator + translation.getFilename())) {

					if (inputStream != null) {
						// Copy the input stream content to the target file
						Files.copy(inputStream, targetPath);
					} else {
						plugin.warningLog("Could not find language file for " + translation.getFilename());
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error creating or copying language file", e);
		}
	}

	public static TranslationManager getInstance() {
		if (singleton == null) {
			singleton = new TranslationManager();
		}
		return singleton;
	}
}
