package com.dre.brewery;

import com.dre.brewery.api.events.brew.BrewModifyEvent;
import com.dre.brewery.lore.Base91DecoderStream;
import com.dre.brewery.lore.Base91EncoderStream;
import com.dre.brewery.lore.BrewLore;
import com.dre.brewery.recipe.BCauldronRecipe;
import com.dre.brewery.recipe.BRecipe;
import com.dre.brewery.recipe.Ingredient;
import com.dre.brewery.recipe.ItemLoader;
import com.dre.brewery.recipe.RecipeItem;
import com.dre.brewery.recipe.PotionColor;
import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents ingredients in Cauldron, Brew
 */
public class BIngredients implements Serializable {

	@Serial
	private static final long serialVersionUID = 8805707247678974367L;

	private static final MinecraftVersion VERSION = BreweryPlugin.getMCVersion();
	private static int lastId = 0; // Legacy

	private int id; // Legacy
	private List<Ingredient> ingredients = new ArrayList<>();
	private int cookedTime;

	/**
	 * Init a new BIngredients
 	 */
	public BIngredients() {
		//this.id = lastId;
		//lastId++;
	}

	/**
	 * Load from File
	 */
	public BIngredients(List<Ingredient> ingredients, int cookedTime) {
		this.ingredients = ingredients;
		this.cookedTime = cookedTime;
		//this.id = lastId;
		//lastId++;
	}

	/**
	 * Load from legacy Brew section
 	 */
	public BIngredients(List<Ingredient> ingredients, int cookedTime, boolean legacy) {
		this(ingredients, cookedTime);
		if (legacy) {
			this.id = lastId;
			lastId++;
		}
	}

	/**
	 * Force add an ingredient to this.
	 * <p>Will not check if item is acceptable
	 *
	 * @param ingredient the item to add
	 */
	public void add(ItemStack ingredient) {
		for (Ingredient existing : ingredients) {
			if (existing.matches(ingredient)) {
				existing.setAmount(existing.getAmount() + 1);
				return;
			}
		}

		Ingredient ing = RecipeItem.getMatchingRecipeItem(ingredient, true).toIngredient(ingredient);
		ing.setAmount(1);
		ingredients.add(ing);
	}

	/**
	 * Add an ingredient to this with corresponding RecipeItem
	 *
	 * @param ingredient the item to add
	 * @param rItem the RecipeItem that matches the ingredient
 	 */
	public void add(ItemStack ingredient, RecipeItem rItem) {
		Ingredient ingredientItem = rItem.toIngredient(ingredient);
		for (Ingredient existing : ingredients) {
			if (existing.isSimilar(ingredientItem)) {
				existing.setAmount(existing.getAmount() + 1);
				return;
			}
		}
		ingredientItem.setAmount(1);
		ingredients.add(ingredientItem);
	}

	/**
	 * returns a Potion item with cooked ingredients
	 */
	public ItemStack cook(int state, String brewer) {

		ItemStack potion = new ItemStack(Material.POTION);
		PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
		assert potionMeta != null;

		// cookedTime is always time in minutes, state may differ with number of ticks
		cookedTime = state;
		String cookedName = null;
		BRecipe cookRecipe = getCookRecipe();
		Brew brew;

		//int uid = Brew.generateUID();
		if (cookRecipe != null) {
			// Potion is best with cooking only
			int quality = (int) Math.round((getIngredientQuality(cookRecipe) + getCookingQuality(cookRecipe, false)) / 2.0);
			int alc = Math.round(cookRecipe.getAlcohol() * ((float) quality / 10.0f));
			BreweryPlugin.getInstance().debugLog("cooked potion has Quality: " + quality + ", Alc: " + alc);
			brew = new Brew(quality, alc, cookRecipe, this);
			BrewLore lore = new BrewLore(brew, potionMeta);
			lore.updateQualityStars(false);
			lore.updateCustomLore();
			lore.updateAlc(false);
			lore.updateBrewer(brewer);
			lore.addOrReplaceEffects(brew.getEffects(), brew.getQuality());
			lore.write();

			cookedName = cookRecipe.getName(quality);
			cookRecipe.getColor().colorBrew(potionMeta, potion, false);
			brew.updateCustomModelData(potionMeta);

			if (cookRecipe.hasGlint()) {
				potionMeta.addEnchant(Enchantment.MENDING, 1, true);
				potionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
		} else {
			// new base potion
			brew = new Brew(this);

			if (state <= 0) {
				cookedName = BreweryPlugin.getInstance().languageReader.get("Brew_ThickBrew");
				PotionColor.BLUE.colorBrew(potionMeta, potion, false);
			} else {
				BCauldronRecipe cauldronRecipe = getCauldronRecipe();
				if (cauldronRecipe != null) {
					BreweryPlugin.getInstance().debugLog("Found Cauldron Recipe: " + cauldronRecipe.getName());
					cookedName = cauldronRecipe.getName();
					if (cauldronRecipe.getLore() != null) {
						BrewLore lore = new BrewLore(brew, potionMeta);
						lore.addCauldronLore(cauldronRecipe.getLore());
						lore.write();
					}
					cauldronRecipe.getColor().colorBrew(potionMeta, potion, true);
					if (VERSION.isOrLater(MinecraftVersion.V1_14) && cauldronRecipe.getCmData() != 0) {
						potionMeta.setCustomModelData(cauldronRecipe.getCmData());
					}
				}
			}
		}
		if (cookedName == null) {
			// if no name could be found
			cookedName = BreweryPlugin.getInstance().languageReader.get("Brew_Undefined");
			PotionColor.CYAN.colorBrew(potionMeta, potion, true);
		}

		potionMeta.setDisplayName(BreweryPlugin.getInstance().color("&f" + cookedName));
		//if (!P.use1_14) {
			// Before 1.14 the effects duration would strangely be only a quarter of what we tell it to be
			// This is due to the Duration Modifier, that is removed in 1.14
		//	uid *= 4;
		//}
		// This effect stores the UID in its Duration
		//potionMeta.addCustomEffect((PotionEffectType.REGENERATION).createEffect((uid * 4), 0), true);

		brew.touch();
		BrewModifyEvent modifyEvent = new BrewModifyEvent(brew, potionMeta, BrewModifyEvent.Type.FILL);
		BreweryPlugin.getInstance().getServer().getPluginManager().callEvent(modifyEvent);
		if (modifyEvent.isCancelled()) {
			return null;
		}
		brew.save(potionMeta);
		potion.setItemMeta(potionMeta);
		BreweryPlugin.getInstance().stats.metricsForCreate(false);

		return potion;
	}

	/**
	 * returns amount of ingredients
	 */
	public int getIngredientsCount() {
		int count = 0;
		for (Ingredient ing : ingredients) {
			count += ing.getAmount();
		}
		return count;
	}

	public List<Ingredient> getIngredientList() {
		return ingredients;
	}

	public int getCookedTime() {
		return cookedTime;
	}

	/**
	 * best recipe for current state of potion, STILL not always returns the correct one...
	 */
	public BRecipe getBestRecipe(float wood, float time, boolean distilled) {
		float quality = 0;
		int ingredientQuality;
		int cookingQuality;
		int woodQuality;
		int ageQuality;
		BRecipe bestRecipe = null;
		for (BRecipe recipe : BRecipe.getAllRecipes()) {
			ingredientQuality = getIngredientQuality(recipe);
			cookingQuality = getCookingQuality(recipe, distilled);

			if (ingredientQuality > -1 && cookingQuality > -1) {
				if (recipe.needsToAge() || time > 0.5) {
					// needs riping in barrel
					ageQuality = getAgeQuality(recipe, time);
					woodQuality = getWoodQuality(recipe, wood);
					BreweryPlugin.getInstance().debugLog("Ingredient Quality: " + ingredientQuality + " Cooking Quality: " + cookingQuality +
						" Wood Quality: " + woodQuality + " age Quality: " + ageQuality + " for " + recipe.getName(5));

					// is this recipe better than the previous best?
					if ((((float) ingredientQuality + cookingQuality + woodQuality + ageQuality) / 4) > quality) {
						quality = ((float) ingredientQuality + cookingQuality + woodQuality + ageQuality) / 4;
						bestRecipe = recipe;
					}
				} else {
					BreweryPlugin.getInstance().debugLog("Ingredient Quality: " + ingredientQuality + " Cooking Quality: " + cookingQuality + " for " + recipe.getName(5));
					// calculate quality without age and barrel
					if ((((float) ingredientQuality + cookingQuality) / 2) > quality) {
						quality = ((float) ingredientQuality + cookingQuality) / 2;
						bestRecipe = recipe;
					}
				}
			}
		}
		if (bestRecipe != null) {
			BreweryPlugin.getInstance().debugLog("best recipe: " + bestRecipe.getName(5) + " has Quality= " + quality);
		}
		return bestRecipe;
	}

	/**
	 * returns recipe that is cooking only and matches the ingredients and cooking time
	 */
	public BRecipe getCookRecipe() {
		BRecipe bestRecipe = getBestRecipe(0, 0, false);

		// Check if best recipe is cooking only
		if (bestRecipe != null) {
			if (bestRecipe.isCookingOnly()) {
				return bestRecipe;
			}
		}
		return null;
	}

	/**
	 * Get Cauldron Recipe that matches the contents of the cauldron
	 */
	@Nullable
	public BCauldronRecipe getCauldronRecipe() {
		BCauldronRecipe best = null;
		float bestMatch = 0;
		float match;
		for (BCauldronRecipe recipe : BCauldronRecipe.getAllRecipes()) {
			match = recipe.getIngredientMatch(ingredients);
			if (match >= 10) {
				return recipe;
			}
			if (match > bestMatch) {
				best = recipe;
				bestMatch = match;
			}
		}
		return best;
	}

	/**
	 * returns the currently best matching recipe for distilling for the ingredients and cooking time
	 */
	public BRecipe getDistillRecipe(float wood, float time) {
		BRecipe bestRecipe = getBestRecipe(wood, time, true);

		// Check if best recipe needs to be destilled
		if (bestRecipe != null) {
			if (bestRecipe.needsDistilling()) {
				return bestRecipe;
			}
		}
		return null;
	}

	/**
	 * returns currently best matching recipe for ingredients, cooking- and ageingtime
	 */
	public BRecipe getAgeRecipe(float wood, float time, boolean distilled) {
		BRecipe bestRecipe = getBestRecipe(wood, time, distilled);

		if (bestRecipe != null) {
			if (bestRecipe.needsToAge()) {
				return bestRecipe;
			}
		}
		return null;
	}

	/**
	 * returns the quality of the ingredients conditioning given recipe, -1 if no recipe is near them
	 */
	public int getIngredientQuality(BRecipe recipe) {
		float quality = 10;
		int count;
		int badStuff = 0;
		if (recipe.isMissingIngredients(ingredients)) {
			// when ingredients are not complete
			return -1;
		}
		for (Ingredient ingredient : ingredients) {
			int amountInRecipe = recipe.amountOf(ingredient);
			count = ingredient.getAmount();
			if (amountInRecipe == 0) {
				// this ingredient doesnt belong into the recipe
				if (count > (getIngredientsCount() / 2)) {
					// when more than half of the ingredients dont fit into the
					// recipe
					return -1;
				}
				badStuff++;
				if (badStuff < ingredients.size()) {
					// when there are other ingredients
					quality -= count * (recipe.getDifficulty() / 2.0);
					continue;
				} else {
					// ingredients dont fit at all
					return -1;
				}
			}
			// calculate the quality
			quality -= ((float) Math.abs(count - amountInRecipe) / recipe.allowedCountDiff(amountInRecipe)) * 10.0;
		}
		if (quality >= 0) {
			return Math.round(quality);
		}
		return -1;
	}

	/**
	 * returns the quality regarding the cooking-time conditioning given Recipe
	 */
	public int getCookingQuality(BRecipe recipe, boolean distilled) {
		if (!recipe.needsDistilling() == distilled) {
			return -1;
		}
		int quality = 10 - (int) Math.round(((float) Math.abs(cookedTime - recipe.getCookingTime()) / recipe.allowedTimeDiff(recipe.getCookingTime())) * 10.0);

		if (quality >= 0) {
			if (cookedTime < 1) {
				return 0;
			}
			return quality;
		}
		return -1;
	}

	/**
	 * returns pseudo quality of distilling. 0 if doesnt match the need of the recipes distilling
	 */
	public int getDistillQuality(BRecipe recipe, byte distillRuns) {
		if (recipe.needsDistilling() != distillRuns > 0) {
			return 0;
		}
		return 10 - Math.abs(recipe.getDistillRuns() - distillRuns);
	}

	/**
	 * returns the quality regarding the barrel wood conditioning given Recipe
	 */
	public int getWoodQuality(BRecipe recipe, float wood) {
		if (recipe.getWood() == 0) {
			// type of wood doesnt matter
			return 10;
		}
		int quality = 10 - Math.round(recipe.getWoodDiff(wood) * recipe.getDifficulty());

		return Math.max(quality, 0);
	}

	/**
	 * returns the quality regarding the ageing time conditioning given Recipe
	 */
	public int getAgeQuality(BRecipe recipe, float time) {
		int quality = 10 - Math.round(Math.abs(time - recipe.getAge()) * ((float) recipe.getDifficulty() / 2));

		return Math.max(quality, 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BIngredients other)) return false;
        return cookedTime == other.cookedTime &&
				ingredients.equals(other.ingredients);
	}

	// Creates a copy ingredients
	public BIngredients copy() {
		BIngredients copy = new BIngredients();
		copy.ingredients.addAll(ingredients);
		copy.cookedTime = cookedTime;
		return copy;
	}

	@Override
	public String toString() {
		return "BIngredients{" +
				"cookedTime=" + cookedTime +
				", total ingredients: " + getIngredientsCount() + '}';
	}

	public void save(DataOutputStream out) throws IOException {
		out.writeInt(cookedTime);
		out.writeByte(ingredients.size());
		for (Ingredient ing : ingredients) {
			ing.saveTo(out);
			out.writeShort(Math.min(ing.getAmount(), Short.MAX_VALUE));
		}
	}

	public static BIngredients load(DataInputStream in, short dataVersion) throws IOException {
		int cookedTime = in.readInt();
		byte size = in.readByte();
		List<Ingredient> ing = new ArrayList<>(size);
		for (; size > 0; size--) {
			ItemLoader itemLoader = new ItemLoader(dataVersion, in, in.readUTF());
			if (!BreweryPlugin.getInstance().ingredientLoaders.containsKey(itemLoader.getSaveID())) {
				BreweryPlugin.getInstance().errorLog("Ingredient Loader not found: " + itemLoader.getSaveID());
				break;
			}
			Ingredient loaded = BreweryPlugin.getInstance().ingredientLoaders.get(itemLoader.getSaveID()).apply(itemLoader);
			int amount = in.readShort();
			if (loaded != null) {
				loaded.setAmount(amount);
				ing.add(loaded);
			}
		}
		return new BIngredients(ing, cookedTime);
	}

	// saves data into main Ingredient section. Returns the save id
	// Only needed for legacy potions
	public int saveLegacy(ConfigurationSection config) {
		String path = "Ingredients." + id;
		if (cookedTime != 0) {
			config.set(path + ".cookedTime", cookedTime);
		}
		config.set(path + ".mats", serializeIngredients());
		return id;
	}

	// Serialize Ingredients to String for storing in yml, ie for Cauldrons
	public String serializeIngredients() {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(new Base91EncoderStream(byteStream))) {
			out.writeByte(Brew.SAVE_VER);
			save(out);
		} catch (IOException e) {
			BreweryPlugin.getInstance().errorLog("Failed to serialize Ingredients", e);
			return "";
		}
		return byteStream.toString();
	}


	public static BIngredients deserializeIngredients(String mat) {
		try (DataInputStream in = new DataInputStream(new Base91DecoderStream(new ByteArrayInputStream(mat.getBytes())))) {
			byte ver = in.readByte();
			return BIngredients.load(in, ver);
		} catch (IOException e) {
			BreweryPlugin.getInstance().errorLog("Failed to deserialize Ingredients", e);
			return new BIngredients();
		}
	}

}
