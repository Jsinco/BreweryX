package com.dre.brewery.lore;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.utility.LegacyUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayInputStream;

public class NBTLoadStream extends ByteArrayInputStream {
	private static final String TAG = "brewdata";
	private static final NamespacedKey KEY = new NamespacedKey(BreweryPlugin.getInstance(), TAG);
	private static final NamespacedKey LEGACY_KEY = new NamespacedKey("brewery", TAG.toLowerCase());

	public NBTLoadStream(ItemMeta meta) {
		super(getNBTBytes(meta));
	}

	private static byte[] getNBTBytes(ItemMeta meta) {
		byte[] bytes = LegacyUtil.readBytesItem(meta, KEY);
		if (bytes == null) {
			bytes = LegacyUtil.readBytesItem(meta, LEGACY_KEY);
		}
		if (bytes == null) {
			return new byte[0];
		}
		return bytes;
	}

	public boolean hasData() {
		return count > 0;
	}

	public static boolean hasDataInMeta(ItemMeta meta) {
		return LegacyUtil.hasBytesItem(meta, KEY) || LegacyUtil.hasBytesItem(meta, LEGACY_KEY);
	}
}
