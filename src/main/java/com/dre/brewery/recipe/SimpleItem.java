package com.dre.brewery.recipe;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Simple Minecraft Item with just Material
 */
public class SimpleItem extends RecipeItem implements Ingredient {


	@Serial
	private static final long serialVersionUID = -1984224739164679875L;

	private static final MinecraftVersion VERSION = BreweryPlugin.getMCVersion();

	private Material material;
	private short duration; // Old Mc


	public SimpleItem(Material material) {
		this(material, (short) 0);
	}

	public SimpleItem(Material material, short duration) {
		this.material = material;
		this.duration = duration;
	}

	@Override
	public boolean hasMaterials() {
		return material != null;
	}

	public Material getMaterial() {
		return material;
	}

	@Override
	public List<Material> getMaterials() {
		List<Material> l = new ArrayList<>(1);
		l.add(material);
		return l;
	}

	@NotNull
	@Override
	public Ingredient toIngredient(ItemStack forItem) {
		return ((SimpleItem) getMutableCopy());
	}

	@NotNull
	@Override
	public Ingredient toIngredientGeneric() {
		return ((SimpleItem) getMutableCopy());
	}

	@Override
	public boolean matches(ItemStack item) {
		if (!material.equals(item.getType())) {
			return false;
		}
		//noinspection deprecation
		return VERSION.isOrLater(MinecraftVersion.V1_13) || duration == item.getDurability();
	}

	@Override
	public boolean matches(Ingredient ingredient) {
		if (isSimilar(ingredient)) {
			return true;
		}
		if (ingredient instanceof RecipeItem) {
			if (!((RecipeItem) ingredient).hasMaterials()) {
				return false;
			}
			if (ingredient instanceof CustomItem) {
				// Only match if the Custom Item also only defines material
				// If the custom item has more info like name and lore, it is not supposed to match a simple item
				CustomItem ci = (CustomItem) ingredient;
				return !ci.hasLore() && !ci.hasName() && material == ci.getMaterial();
			}
		}
		return false;
	}

	@Override
	public boolean isSimilar(Ingredient item) {
		if (this == item) {
			return true;
		}
		if (item instanceof SimpleItem) {
			SimpleItem si = ((SimpleItem) item);
			return si.material == material && si.duration == duration;
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		SimpleItem item = (SimpleItem) o;
		return duration == item.duration &&
			material == item.material;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), material, duration);
	}

	@Override
	public String toString() {
		return "SimpleItem{" +
			"mat=" + material.name().toLowerCase() +
			" amount=" + getAmount() +
			'}';
	}

	@Override
	public void saveTo(DataOutputStream out) throws IOException {
		out.writeUTF("SI");
		out.writeUTF(material.name());
		out.writeShort(duration);
	}

	public static SimpleItem loadFrom(ItemLoader loader) {
		try {
			DataInputStream in = loader.getInputStream();
			Material mat = Material.getMaterial(in.readUTF());
			short dur = in.readShort();
			if (mat != null) {
				SimpleItem item = new SimpleItem(mat, dur);
				return item;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Needs to be called at Server start
	public static void registerItemLoader(BreweryPlugin breweryPlugin) {
		breweryPlugin.registerForItemLoader("SI", SimpleItem::loadFrom);
	}


	@Serial
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(material.name());
		out.writeShort(duration);
	}

	@Serial
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		material = Material.valueOf((String) in.readObject());
		duration = in.readShort();
	}

}

