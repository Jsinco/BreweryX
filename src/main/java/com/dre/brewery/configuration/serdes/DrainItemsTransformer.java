package com.dre.brewery.configuration.serdes;

import com.Acrobot.ChestShop.Libs.ORMlite.stmt.query.In;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.utility.BUtil;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import lombok.NonNull;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrainItemsTransformer extends BidirectionalTransformer<List<String>, Map<Material, Integer>> {

    private static final BreweryPlugin breweryPlugin = BreweryPlugin.getInstance();

    @Override
    public GenericsPair<List<String>, Map<Material, Integer>> getPair() {
        return this.genericsPair((Class<List<String>>) (Class<?>) List.class, (Class<Map<Material, Integer>>) (Class<?>) Map.class);
    }

    @Override
    public Map<Material, Integer> leftToRight(@NonNull List<String> data, @NonNull SerdesContext serdesContext) {
        Map<Material, Integer> drainItems = new HashMap<>();
        for (String drainString : data) {
            String[] drainSplit = drainString.split("/");
            if (drainSplit.length > 1) {
                Material mat = BUtil.getMaterialSafely(drainSplit[0]);
                int strength = breweryPlugin.parseInt(drainSplit[1]);
//                if (mat == null && hasVault && strength > 0) {
//                    try {
//                        net.milkbowl.vault.item.ItemInfo vaultItem = net.milkbowl.vault.item.Items.itemByString(drainSplit[0]);
//                        if (vaultItem != null) {
//                            mat = vaultItem.getType();
//                        }
//                    } catch (Exception e) {
//                        BreweryPlugin.getInstance().errorLog("Could not check vault for Item Name");
//                        e.printStackTrace();
//                    }
//                }
                if (mat != null && strength > 0) {
                    drainItems.put(mat, strength);
                }
            }
        }
        return drainItems;
    }

    @Override
    public List<String> rightToLeft(@NonNull Map<Material, Integer> data, @NonNull SerdesContext serdesContext) {
        List<String> drainItemsStr = new ArrayList<>();
        for (Map.Entry<Material, Integer> entry : data.entrySet()) {
            drainItemsStr.add(entry.getKey().toString() + "/" + entry.getValue());
        }
        return drainItemsStr;
    }


}
