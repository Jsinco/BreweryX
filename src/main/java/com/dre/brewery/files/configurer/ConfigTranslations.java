package com.dre.brewery.files.configurer;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ConfigTranslations {
	private final Map<String, String> translations;

	public ConfigTranslations(Translation activeTranslation) {
		try (InputStream inputStream = this.getClass()
			.getClassLoader()
			.getResourceAsStream("config/lang/" + activeTranslation.getFilename())) {

			Yaml yaml = new Yaml();
			translations = yaml.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getTranslation(String key) {
		return translations.get(key);
	}
}
