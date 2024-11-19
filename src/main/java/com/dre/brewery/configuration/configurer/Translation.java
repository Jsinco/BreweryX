package com.dre.brewery.configuration.configurer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * List of the available translation<br />
 * Should we even use an enum here?<br />
 * <br />
 * The comments can be found under {@code langs/LANGUAGE.yml}
 */
@Getter
@AllArgsConstructor
public enum Translation {

	// Languages added should have a config and a lang translation (resources/configlangs/, resources/languages/)

	EN("en.yml"),
	DE("de.yml"),
	ES("es.yml"),
	FR("fr.yml"),
	IT("it.yml"),
	RU("ru.yml"),
	ZH("zh.yml");

	private final String filename;
}
