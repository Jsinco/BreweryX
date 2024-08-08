package com.dre.brewery;

import com.dre.brewery.api.events.barrel.BarrelAccessEvent;
import com.dre.brewery.api.events.barrel.BarrelCreateEvent;
import com.dre.brewery.api.events.barrel.BarrelDestroyEvent;
import com.dre.brewery.api.events.barrel.BarrelRemoveEvent;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.hazelcast.HazelcastCacheManager;
import com.dre.brewery.integration.barrel.LogBlockBarrel;
import com.dre.brewery.lore.BrewLore;
import com.dre.brewery.storage.serialization.BukkitSerialization;
import com.dre.brewery.utility.BoundingBox;
import com.dre.brewery.utility.LegacyUtil;
import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A Multi Block Barrel with Inventory
 */
public class Barrel extends BarrelBody implements InventoryHolder, Serializable, Ownable {

	@Serial
	private static final long serialVersionUID = -2785658458165598294L;

    private static final BreweryPlugin plugin = BreweryPlugin.getInstance();
	private static final HazelcastInstance hazelcast = BreweryPlugin.getHazelcast();
	private static int check = 0; // Which Barrel was last checked

	private UUID owner;

	private UUID id;
	private Inventory inventory;
	private boolean checked; // Checked by the random BarrelCheck routine
	private float time;


	/**
	 * Create a new Barrel
	 */
	public Barrel(Block spigot, byte signOffset) {
		super(spigot, signOffset);
		this.owner = BreweryPlugin.ownerID;
		this.id = UUID.randomUUID();
		this.inventory = Bukkit.createInventory(this, getIntendedInvSize(), plugin.languageReader.get("Etc_Barrel"));
	}

	/**
	 * Load from File
	 * <p>If async: true, The Barrel Bounds will not be recreated when missing/corrupt, getBody().getBounds() will be null if it needs recreating
	 * Note from Jsinco, async is now checked using Bukkit.isPrimaryThread().^
	 */
	public Barrel(Block spigot, byte sign, BoundingBox bounds, @Nullable Map<String, Object> items, float time, UUID id) {
		super(spigot, sign, bounds);
		this.owner = HazelcastCacheManager.getClusterId();
		this.time = time;
		this.id = id;
		this.inventory = Bukkit.createInventory(this, getIntendedInvSize(), plugin.languageReader.get("Etc_Barrel"));
		if (items != null) {
			for (String slot : items.keySet()) {
				if (items.get(slot) instanceof ItemStack) {
					this.inventory.setItem(BreweryPlugin.getInstance().parseInt(slot), (ItemStack) items.get(slot));
				}
			}
		}

	}

	public Barrel(Block spigot, byte sign, BoundingBox bounds, ItemStack[] items, float time, UUID id) {
		super(spigot, sign, bounds);
		this.owner = HazelcastCacheManager.getClusterId();
		this.time = time;
		this.id = id;
		this.inventory = Bukkit.createInventory(this, getIntendedInvSize(), plugin.languageReader.get("Etc_Barrel"));
		if (items != null) {
			for (int slot = 0; slot < items.length; slot++) {
				if (items[slot] != null) {
					this.inventory.setItem(slot, items[slot]);
				}
			}
		}
	}

	public Barrel(Block spigot, byte sign, BoundingBox bounds, ItemStack[] items, boolean checked, float time, UUID id, UUID owner) {
		super(spigot, sign, bounds);
		this.owner = owner;
		this.checked = checked;
		this.time = time;
		this.id = id;
		this.inventory = Bukkit.createInventory(this, getIntendedInvSize(), plugin.languageReader.get("Etc_Barrel"));
		if (items != null) {
			for (int slot = 0; slot < items.length; slot++) {
				if (items[slot] != null) {
					this.inventory.setItem(slot, items[slot]);
				}
			}
		}
	}

	@Override
	public void regenerateBounds() {
		plugin.log("Regenerating Barrel BoundingBox: " + (bounds == null ? "was null" : "area=" + bounds.area()));
		Block broken = getBrokenBlock(true);
		if (broken != null) {
			this.remove(broken, null, true);
		}
	}


	public boolean hasOpenPerms(Player player, PlayerInteractEvent event) {
		if (isLarge()) {
			if (!player.hasPermission("brewery.openbarrel.big")) {
				plugin.msg(player, plugin.languageReader.get("Error_NoBarrelAccess"));
				return false;
			}
		} else {
			if (!player.hasPermission("brewery.openbarrel.small")) {
				plugin.msg(player, plugin.languageReader.get("Error_NoBarrelAccess"));
				return false;
			}
		}

		BarrelAccessEvent accessEvent = new BarrelAccessEvent(this, player, event.getClickedBlock(), event.getBlockFace());
		// Listened to by IntegrationListener
		Bukkit.getPluginManager().callEvent(accessEvent);
		return !accessEvent.isCancelled();
	}

	/**
	 * Ask for permission to destroy barrel
	 */
	public boolean hasPermsDestroy(Player player, Block block, BarrelDestroyEvent.Reason reason) {
		// Listened to by LWCBarrel (IntegrationListener)
		BarrelDestroyEvent destroyEvent = new BarrelDestroyEvent(this, block, reason, player);
		Bukkit.getPluginManager().callEvent(destroyEvent);
		return !destroyEvent.isCancelled();
	}

	/**
	 * Opens this barrels inventory for a player
	 */
	public void open(Player player) {
		if (inventory == null) {
			inventory = Bukkit.createInventory(this, getIntendedInvSize(), plugin.languageReader.get("Etc_Barrel"));
		} else if (time > 0) {
			if (inventory.getViewers().isEmpty() && inventory.contains(Material.POTION)) { // if nobody has the inventory opened and it contains potions
				byte wood = getWood();
				long loadTime = System.currentTimeMillis();
				for (ItemStack item : inventory.getContents()) {
					Brew brew = Brew.get(item);
					if (brew != null) {
						brew.age(item, time, wood);
					}
				}
				plugin.debugLog("opening Barrel with potions (" + (System.currentTimeMillis() - loadTime) + "ms)");
			}
		}

		time = 0; // reset barrel time, potions have new age
		if (BConfig.useLB) {
			LogBlockBarrel.openBarrel(player, inventory, spigot.getLocation());
		}
		player.openInventory(inventory);
	}


	/**
	 * Removes a barrel, throwing included potions/items to the ground
	 * @param broken The Block that was broken
	 * @param breaker The Player that broke it, or null if not known
	 * @param dropItems If the items in the barrels inventory should drop to the ground
	 */
	public void remove(@Nullable Block broken, @Nullable Player breaker, boolean dropItems) {
		BarrelRemoveEvent event = new BarrelRemoveEvent(this, dropItems);
		Bukkit.getPluginManager().callEvent(event); // Listened to by LWCBarrel (IntegrationListener)
		if (inventory != null) {
			for (HumanEntity viewer : inventory.getViewers()) {
				viewer.closeInventory();
			}
			ItemStack[] items = inventory.getContents(); inventory.clear();

			if (BConfig.useLB && breaker != null) {
				LogBlockBarrel.breakBarrel(breaker, items, spigot.getLocation());
			}
			if (event.willDropItems()) {
				byte wood = getWood();
				for (ItemStack item : items) {
					if (item == null) continue;

					Brew brew = Brew.get(item);
					if (brew != null) {
						// Brew before throwing
						brew.age(item, time, wood);
						PotionMeta meta = (PotionMeta) item.getItemMeta();
                        if (BrewLore.hasColorLore(meta)) {
							BrewLore lore = new BrewLore(brew, meta);
							lore.convertLore(false);
							lore.write();
							item.setItemMeta(meta);
						}
					}
					// "broken" is the block that destroyed, throw them there!
					if (broken != null) {
						broken.getWorld().dropItem(broken.getLocation(), item);
					} else {
						spigot.getWorld().dropItem(spigot.getLocation(), item);
					}
				}
			}
		}

		hazelcast.getList("barrels").remove(this);
	}



	public void saveToHazelcast() {
		IList<Barrel> barrels = hazelcast.getList(HazelcastCacheManager.CacheType.BARRELS.getHazelCastName());

		int i = 0;
		for (Barrel barrel : barrels) {
			if (barrel.getId().equals(id)) {
				barrels.set(i, this); // OPERATION SAVED
				System.out.println("Barrel saved to Hazelcast: " + this.id);
				return;
			}
			i++;
		}
	}


	/**
	 * Get the Barrel by Block, null if that block is not part of a barrel
	 */
	@Nullable
	public static Barrel get(Block block) {
		if (block == null) {
			return null;
		}
		Material type = block.getType();
		if (LegacyUtil.isFence(type) || LegacyUtil.isSign(type)) {
			return getBySpigot(block);
		} else {
			return getByWood(block);
		}
	}

	/**
	 * Get the Barrel by Sign or Spigot (Fastest)
	 */
	@Nullable
	public static Barrel getBySpigot(Block signOrSpigot) {
		// convert spigot if neccessary
		Block spigot = BarrelBody.getSpigotOfSign(signOrSpigot);

		final byte signOffset;
		if (!spigot.equals(signOrSpigot)) {
			signOffset = (byte) (signOrSpigot.getY() - spigot.getY());
		} else {
            signOffset = 0;
        }

		IList<Barrel> barrels = hazelcast.getList(HazelcastCacheManager.CacheType.BARRELS.getHazelCastName());

        int i = 0;
		for (Barrel barrel : barrels) {
			if (barrel == null) continue;

			// Barrel has no signOffset even though we clicked a sign, may be old
			// No fucking clue what this means ^ - Jsinco
			if (barrel.isSignOfBarrel(signOffset) && barrel.spigot.equals(spigot) && barrel.getSignOffset() == 0 && signOffset != 0) {
				barrel.setSignOffset(signOffset);
				barrels.set(i, barrel); // set hazelcastlist because hazelcast doesn't factor in for mutated objects // OPERATION SAVED
				moveMRU(barrels, i);

				return barrel;
			}
			i++;
		}
		return null;
	}

	/**
	 * Get the barrel by its corpus (Wood Planks, Stairs)
	 */
	@Nullable
	public static Barrel getByWood(Block wood) {
		if (LegacyUtil.isWoodPlanks(wood.getType()) || LegacyUtil.isWoodStairs(wood.getType())) {

			IList<Barrel> barrels = hazelcast.getList(HazelcastCacheManager.CacheType.BARRELS.getHazelCastName());

			int i = 0;
			for (Barrel barrel : barrels) {
				if (barrel.getSpigot().getWorld().equals(wood.getWorld()) && barrel.getBounds().contains(wood)) {
					moveMRU(barrels, i);
					return barrel;
				}
				i++;
			}
		}
		return null;
	}



	/**
	 * Creates a new Barrel out of a sign
	 * This is async because we need to check if the barrel already exists between all Brewery plugins
	 */
	public static boolean create(Block sign, Player player) {
		Block spigot = BarrelBody.getSpigotOfSign(sign);

		// Check for already existing barrel at this location
		if (Barrel.get(spigot) != null) {
			return false;
		}

		final byte signOffset;
		if (!spigot.equals(sign)) {
			signOffset = (byte) (sign.getY() - spigot.getY());
		} else {
            signOffset = 0;
        }

        Barrel barrel = getBySpigot(spigot);

        if (barrel != null) {
			if (signOffset != 0) {
				barrel.setSignOffset(signOffset);
				return true;
			}
			return false;
		} else {
			barrel = new Barrel(spigot, signOffset);
			if (barrel.getBrokenBlock(true) == null) {
				if (LegacyUtil.isSign(spigot.getType())) {
					if (!player.hasPermission("brewery.createbarrel.small")) {
						plugin.msg(player, plugin.languageReader.get("Perms_NoSmallBarrelCreate"));
						return false;
					}
				} else {
					if (!player.hasPermission("brewery.createbarrel.big")) {
						plugin.msg(player, plugin.languageReader.get("Perms_NoBigBarrelCreate"));
						return false;
					}
				}
				BarrelCreateEvent createEvent = new BarrelCreateEvent(barrel, player);
				Bukkit.getPluginManager().callEvent(createEvent);
				if (!createEvent.isCancelled()) {
					hazelcast.getList(HazelcastCacheManager.CacheType.BARRELS.getHazelCastName()).add(0, barrel); // OPERATION SAVED
					return true;
				}
			}
		}
		return false;
	}


	// Move Barrel that was recently used more towards the front of the List
	// Optimizes retrieve by Block over time
	private static void moveMRU(List<Barrel> barrels, int index) {
		if (index > 0) {
			// Swap entry at the index with the one next to it
			barrels.set(index - 1, barrels.set(index, barrels.get(index - 1)));
		}
	}

	// ATTRIBUTES

	@Override
	@NotNull
	public Inventory getInventory() {
		return inventory;
	}

	@NotNull
	public Block getSpigot() {
		return spigot;
	}

	public UUID getId() {
		return id;
	}

	@Override
	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	@Override
	public UUID getOwner() {
		return owner;
	}

	public float getTime() {
		return time;
	}

	public boolean isChecked() {
		return checked;
	}

	public boolean isLarge() {
		return !isSmall();
	}

	public int getIntendedInvSize() {
		if (isLarge()) {
			return 27;
		} else {
			return 9;
		}
	}

	public boolean isSmall() {
		return LegacyUtil.isSign(spigot.getType());
	}

	/**
	 * returns the fence above/below a block, itself if there is none
	 */
	public static Block getSpigotOfSign(Block block) {
		return BarrelBody.getSpigotOfSign(block);
	}


	public static void updateAllBarrels() {
		IList<Barrel> barrels = hazelcast.getList(HazelcastCacheManager.CacheType.BARRELS.getHazelCastName()); // Only update barrels we own

		int i = 0;
		for (Barrel barrel : barrels) {
			// A Minecraft day is 20 min, so add 1/20 to the time every minute
			if (barrel != null) {
				barrel.time += (float) (1.0 / BConfig.agingYearDuration);
				barrels.set(i, barrel); // OPERATION SAVED
			}
			i++;
		}
		int numBarrels = barrels.size();
		if (check == 0 && numBarrels > 0) {
			int index = (int) Math.floor(Math.random() * numBarrels);
			Barrel random = barrels.get(index);
			if (random != null) {
				// You have been selected for a random search
				// We want to check at least one barrel every time
				random.checked = false;
				barrels.set(index, random); // OPERATION SAVED
			}
			if (numBarrels > 50) {
				int indexOfTheBack = numBarrels - 1 - (int) (Math.random() * (numBarrels >>> 2));
				Barrel randomInTheBack = barrels.get(indexOfTheBack);
				if (randomInTheBack != null) {
					// Prioritize checking one of the less recently used barrels as well
					randomInTheBack.checked = false;
					barrels.set(indexOfTheBack, randomInTheBack); // OPERATION SAVED
				}
			}
			new BarrelCheck().runTaskTimer(plugin, 1, 1);
		}
	}


	public static class BarrelCheck extends UniversalRunnable {
		@Override
		public void run() { // Only check barrels owned by us
			boolean repeat = true;
			while (repeat) {
				if (check < HazelcastCacheManager.getOwnedBarrels().size()) {
					Barrel barrel = HazelcastCacheManager.getOwnedBarrels().get(check);
					if (!barrel.checked) {
						BreweryPlugin.getScheduler().runTask(barrel.getSpigot().getLocation(), () -> {
							Block broken = barrel.getBrokenBlock(false);
							if (broken != null) {
								plugin.debugLog("Barrel at " + broken.getWorld().getName() + "," + broken.getX() + "," + broken.getY() + "," + broken.getZ() + " has been destroyed unexpectedly, contents will drop");
								// remove the barrel if it was destroyed
								barrel.remove(broken, null, true);
							} else {
								// Don't check this barrel again, it's enough to check it once after every restart (and when randomly chosen)
								// as now this is only the backup if we don't register the barrel breaking,
								// for example when removing it with some world editor
								barrel.checked = true;
								hazelcast.getList(HazelcastCacheManager.CacheType.BARRELS.getHazelCastName()).set(check, barrel); // OPERATION SAVED
							}
						});
						repeat = false;
					}
					check++;
				} else {
					check = 0;
					repeat = false;
					cancel();
				}
			}

			plugin.log("Checked " + HazelcastCacheManager.getOwnedBarrels() + " owned by us");
		}

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Barrel barrel)) return false;
		return Objects.equals(id, barrel.id);
	}

	@Override
	public String toString() {
		return "Barrel{" +
				"id=" + id +
				", spigot=" + spigot +
				", signOffset=" + signOffset +
				", bounds=" + bounds +
				", owner=" + owner +
				", inventory=" + Arrays.toString(inventory.getContents()) +
				", checked=" + checked +
				", time=" + time +
				'}';
	}

	@Serial
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(owner);
		out.writeObject(id);
		out.writeObject(BukkitSerialization.toBase64(inventory)); // Write the Inventory
		out.writeBoolean(checked);
		out.writeFloat(time);
	}

	@Serial
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		owner = (UUID) in.readObject();
		id = (UUID) in.readObject();
		inventory = BukkitSerialization.fromBase64((String) in.readObject(), this, plugin.languageReader.get("Etc_Barrel")); // Read the Inventory
		checked = in.readBoolean();
		time = in.readFloat();
	}


	// this shit keeps getting in my way so im moving it to the bottom todo: organize this shitshow

	public void playOpeningSound() {
		float randPitch = (float) (Math.random() * 0.1);
		Location location = spigot.getLocation();
		if (location.getWorld() == null) return;
		if (isLarge()) {
			location.getWorld().playSound(location, Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.4f, 0.55f + randPitch);
			location.getWorld().playSound(location, Sound.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.4f, 0.45f + randPitch);
		} else {
			location.getWorld().playSound(location, Sound.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 0.5f, 0.8f + randPitch);
		}
	}

	public void playClosingSound() {
		float randPitch = (float) (Math.random() * 0.1);
		Location location = spigot.getLocation();
		if (location.getWorld() == null) return;
		if (isLarge()) {
			location.getWorld().playSound(location, Sound.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 0.5f, 0.5f + randPitch);
			location.getWorld().playSound(location, Sound.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.2f, 0.6f + randPitch);
		} else {
			location.getWorld().playSound(location, Sound.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 0.5f, 0.8f + randPitch);
		}
	}
}
