package com.dre.brewery.configuration.configurer;

import com.dre.brewery.BreweryPlugin;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ConfigTranslations {

	private final Map<String, Object> translations;

	public ConfigTranslations(Translation activeTranslation) {
		this(activeTranslation, new Yaml());
	}

	public ConfigTranslations(Translation activeTranslation, Yaml yamlInstance) {
		try (InputStream inputStream = this.getClass()
			.getClassLoader()
			.getResourceAsStream("configlangs" + File.separator + activeTranslation.getFilename())) {

			translations = yamlInstance.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Attempts to retrieve a translation for the given key.
	 * @param key the key to retrieve the translation for
	 * @return the translation for the given key, or null if not found
	 */
	@Nullable
	public String getTranslation(String key) {
		try {
			if (!key.contains(".")) {
				return (String) translations.get(key);
			}


			String[] keys = key.split("\\.");
			Map<String, Object> current = translations;

			for (int i = 0; i < keys.length - 1; i++) {
				current = (Map<String, Object>) current.get(keys[i]);
				if (current == null) {
					return null;
				}
			}

			return (String) current.get(keys[keys.length - 1]);
		} catch (Exception e) {
			BreweryPlugin.getInstance().errorLog("Error while getting translation for key: " + key, e);
			return null;
		}
	}
}
