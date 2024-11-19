package com.dre.brewery.configuration.serdes;

import com.dre.brewery.utility.BUtil;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import lombok.NonNull;
import org.bukkit.Material;

public class MaterialTransformer extends BidirectionalTransformer<String, Material> {
	@Override
	public GenericsPair<String, Material> getPair() {
		return this.genericsPair(String.class, Material.class);
	}

	@Override
	public Material leftToRight(@NonNull String data, @NonNull SerdesContext serdesContext) {
		return BUtil.getMaterialSafely(data); // handles grass -> short_grass
	}

	@Override
	public String rightToLeft(@NonNull Material data, @NonNull SerdesContext serdesContext) {
		return data.toString();
	}
}
