package com.dre.brewery.files.serdes;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import lombok.NonNull;
import org.bukkit.Material;

// TODO: Handle old versions, e.g. grass -> short_grass
public class MaterialTransformer extends BidirectionalTransformer<String, Material> {
	@Override
	public GenericsPair<String, Material> getPair() {
		return this.genericsPair(String.class, Material.class);
	}

	@Override
	public Material leftToRight(@NonNull String data, @NonNull SerdesContext serdesContext) {
		return Material.matchMaterial(data);
	}

	@Override
	public String rightToLeft(@NonNull Material data, @NonNull SerdesContext serdesContext) {
		return data.toString();
	}
}
