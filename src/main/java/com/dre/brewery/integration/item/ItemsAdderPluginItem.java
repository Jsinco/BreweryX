package com.dre.brewery.integration.item;

import com.dre.brewery.integration.Hook;
import com.dre.brewery.recipe.PluginItem;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderPluginItem extends PluginItem {
    @Override
    public boolean matches(ItemStack itemStack) {
        if (!Hook.ITEMSADDER.isEnabled()) {
            return false;
        }

        CustomStack cs = CustomStack.byItemStack(itemStack);
        if (cs == null) {
            return false;
        }
        return cs.getId().equals(this.getItemId());
    }
}
