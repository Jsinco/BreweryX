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
	EN("en.yml"),
	DE("de.yml");

	private final String filename;
}
