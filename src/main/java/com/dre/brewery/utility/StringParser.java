package com.dre.brewery.utility;

public class StringParser {

	public static Tuple<Integer, String> parseQuality(String line, ParseType type) {
		line = BUtil.color(line);
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

	public enum ParseType {
		LORE,
		CMD,
		OTHER
	}
}
