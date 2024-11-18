package com.dre.brewery.files.configurer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * List of the available translation<br />
 * Should we even use an enum here?<br />
 * <br />
 * The comments can be found under {@code config/lang/LANGUAGE.yml}
 */
@Getter
@AllArgsConstructor
public enum Translation {
	ENGLISH("english", "english.yml"),
	GERMAN("german", "german.yml");

	private final String key;
	private final String filename;
}
