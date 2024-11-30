package com.dre.brewery.integration.item;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.integration.Hook;
import com.dre.brewery.recipe.PluginItem;
import com.dre.brewery.utility.Logging;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.inventory.ItemStack;

public class SlimefunPluginItem extends PluginItem {

// When implementing this, put Brewery as softdepend in your plugin.yml!
// We're calling this as server start:
// PluginItem.registerForConfig("slimefun", SlimefunPluginItem::new);
// PluginItem.registerForConfig("exoticgarden", SlimefunPluginItem::new);

	@Override
	public boolean matches(ItemStack item) {
		if (!Hook.SLIMEFUN.isEnabled()) return false;

		try {
			SlimefunItem sfItem = SlimefunItem.getByItem(item);
			if (sfItem != null) {
				return sfItem.getId().equalsIgnoreCase(getItemId());
			}
		} catch (Exception | LinkageError e) {
			Logging.errorLog("Could not check Slimefun for Item ID", e);
			Hook.SLIMEFUN.setEnabled(false);
		}
		return false;
	}
}
