package com.dre.brewery.files.configurer;

import com.dre.brewery.files.Config;
import org.jetbrains.annotations.Nullable;

public class TranslationHelper {
	private final ConfigTranslations translations = new ConfigTranslations(Config.getInstance().getLanguage());
	private final ConfigTranslations fallbackTranslations = new ConfigTranslations(Translation.ENGLISH);

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
			System.out.println("No translation found for key: " + key);
		}

		return fallbackTranslation;
	}
}
