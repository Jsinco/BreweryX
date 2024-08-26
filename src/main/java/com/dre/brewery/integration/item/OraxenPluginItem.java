package com.dre.brewery.integration.item;

import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.recipe.PluginItem;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.inventory.ItemStack;

public class OraxenPluginItem extends PluginItem {
    @Override
    public boolean matches(ItemStack itemStack) {
        if (!BConfig.hasOraxen) {
            return false;
        }

        String itemId = OraxenItems.getIdByItem(itemStack);
        if (itemId == null) {
            return false;
        }
        return itemId.equals(this.getItemId());
    }
}