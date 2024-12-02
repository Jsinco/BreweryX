package com.dre.brewery;

import lombok.Getter;
import org.bukkit.Material;

import java.util.*;

public enum BarrelAsset {
	PLANKS, // Base block
	STAIRS, // Alt 1 Block
	SIGN, // Alt 2 Block
	FENCE; // Optional: Alt 3 Block


	public static final Map<BarrelAsset, List<Material>> BARREL_ASSET_LIST_MAP = new HashMap<>();
	static {
		for (BarrelAsset asset : values()) {
			BARREL_ASSET_LIST_MAP.put(asset, new ArrayList<>());
		}
	}

	public static void addBarrelAsset(BarrelAsset asset, Material... materials) {
		if (materials == null || materials.length == 0) {
			return;
		}
		Collections.addAll(BARREL_ASSET_LIST_MAP.get(asset), Arrays.stream(materials).filter(Objects::nonNull).toArray(Material[]::new));
	}

	public static boolean isBarrelAsset(BarrelAsset assetType, Material material) {
		return BARREL_ASSET_LIST_MAP.get(assetType).contains(material);
	}
}
