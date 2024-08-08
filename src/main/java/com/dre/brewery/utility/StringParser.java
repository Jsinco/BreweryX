package com.dre.brewery.utility;

import com.dre.brewery.BreweryPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringParser {

	public static Tuple<Integer, String> parseQuality(String line, ParseType type) {
		line = BreweryPlugin.getInstance().color(line);
		int plus = 0;
		if (line.startsWith("+++")) {
			plus = 3;
			line = line.substring(3);
		} else if (line.startsWith("++")) {
			plus = 2;
			line = line.substring(2);
		} else if (line.startsWith("+")) {
			plus = 1;
			line = line.substring(1);
		}
		if (line.startsWith(" ")) {
			line = line.substring(1);
		}

		if (type == ParseType.CMD && line.startsWith("/")) {
			line = line.substring(1);
		}

		if (type == ParseType.LORE && !line.startsWith("§")) {
			line = "§9" + line;
		}
		return new Tuple<>(plus, line);
	}

	public static Map<Integer, String> parseQualityMap(List<String> load, ParseType type) {
		Map<Integer, String> map = new HashMap<>();

		for (String line : load) {
			line = BreweryPlugin.getInstance().color(line);

			int plus = 0;
			if (line.startsWith("+++")) {
				plus = 3;
				line = line.substring(3);
			} else if (line.startsWith("++")) {
				plus = 2;
				line = line.substring(2);
			} else if (line.startsWith("+")) {
				plus = 1;
				line = line.substring(1);
			}
			if (line.startsWith(" ")) {
				line = line.substring(1);
			}

			if (type == ParseType.CMD && line.startsWith("/")) {
				line = line.substring(1);
			}

			if (type == ParseType.LORE && !line.startsWith("§")) {
				line = "§9" + line;
			}
			map.put(plus, line);
		}
		return map;
	}

	public enum ParseType {
		LORE,
		CMD,
		OTHER
	}
}
