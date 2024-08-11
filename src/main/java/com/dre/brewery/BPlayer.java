package com.dre.brewery;

import com.dre.brewery.api.events.PlayerEffectEvent;
import com.dre.brewery.api.events.PlayerPukeEvent;
import com.dre.brewery.api.events.PlayerPushEvent;
import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.hazelcast.HazelcastCacheManager;
import com.dre.brewery.lore.BrewLore;
import com.dre.brewery.recipe.BEffect;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.MinecraftVersion;
import com.dre.brewery.utility.PermissionUtil;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// should already be serializable
// Todo: tasks should only be done by node that own this
public class BPlayer implements Serializable, Ownable {

	@Serial
	private static final long serialVersionUID = -3022726699310234549L;

	private static final MinecraftVersion VERSION = BreweryPlugin.getMCVersion();
	private static final BreweryPlugin plugin = BreweryPlugin.getInstance();
	private static final HazelcastInstance hazelcast = BreweryPlugin.getHazelcast();


	private static final ConcurrentHashMap<Player, Integer> pukeTasks = new ConcurrentHashMap<>(); // Player and count
	private static MyScheduledTask task;
	private static Random pukeRand;

	private UUID owner;
	private UUID uuid;
	private Vector push = new Vector(0, 0, 0);
	private int quality = 0;// = quality of drunkenness * drunkenness
	private int drunkenness = 0;// = amount of drunkenness
	private int offlineDrunk = 0;// drunkenness when gone offline
	private int alcRecovery = -1; // Drunkeness reduce per minute
	private int time = 20;

	public BPlayer(UUID uuid, int quality, int drunkenness, int offlineDrunk, UUID owner) {
		this.quality = quality;
		this.drunkenness = drunkenness;
		this.offlineDrunk = offlineDrunk;
		this.uuid = uuid;
		this.owner = owner;
	}

	// reading from file
	public BPlayer(UUID uuid, int quality, int drunkenness, int offlineDrunk) {
		this.quality = quality;
		this.drunkenness = drunkenness;
		this.offlineDrunk = offlineDrunk;
		this.uuid = uuid;
		this.owner = HazelcastCacheManager.getNextOwner();
	}

	public BPlayer(UUID uuid) {
		this.uuid = uuid;
		this.owner = HazelcastCacheManager.getNextOwner();
	}

	public void saveToHazelcast() {
		hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName()).put(uuid, this);
	}

	@Nullable
	public static BPlayer get(OfflinePlayer player) {
		IMap<UUID, BPlayer> players = hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName());
		return players.get(player.getUniqueId());
	}

	public static boolean hasPlayer(OfflinePlayer player) {
		return hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName()).containsKey(player.getUniqueId());
	}

	public static boolean isEmpty() {
		return hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName()).isEmpty();
	}


	// This method may be slow and should not be used if not needed
	@Nullable
	public static BPlayer getByName(String playerName) {
		IMap<UUID, BPlayer> players = hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName());
		for (Map.Entry<UUID, BPlayer> entry : players) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
			String name = p.getName();
			if (name != null) {
				if (name.equalsIgnoreCase(playerName)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}



	// Create a new BPlayer and add it to the list
	public static BPlayer addPlayer(OfflinePlayer player) {
		BPlayer bPlayer = new BPlayer(player.getUniqueId());
		hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName()).put(player.getUniqueId(), bPlayer); // OPERATION SAVED
		return bPlayer;
	}

	public static void remove(OfflinePlayer player) {
		hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName()).remove(player.getUniqueId()); // OPERATION SAVED
	}

	public void remove() {
		hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName()).remove(this.uuid); // OPERATION SAVED
	}

	public static int numDrunkPlayers() {
		return hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName()).size();
	}


	public static void clear() {
		hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName()).clear(); // OPERATION SAVED
	}

	// Drink a brew and apply effects, etc.
	public static boolean drink(Brew brew, ItemMeta meta, Player player) {
		BPlayer bPlayer = get(player);
		if (bPlayer == null) {
			bPlayer = addPlayer(player);
		}
		// In this event the added alcohol amount is calculated, based on the sensitivity permission
		BrewDrinkEvent drinkEvent = new BrewDrinkEvent(brew, meta, player, bPlayer);
		if (meta != null) {
			Bukkit.getPluginManager().callEvent(drinkEvent);
			if (brew != drinkEvent.getBrew()) brew = drinkEvent.getBrew();
			if (drinkEvent.isCancelled()) {
				if (bPlayer.drunkenness <= 0) {
					bPlayer.remove();
				}
				return false;
			}
		}

		if (brew.hasRecipe()) {
			brew.getCurrentRecipe().applyDrinkFeatures(player, brew.getQuality());
		}
		plugin.stats.forDrink(brew);

		int brewAlc = drinkEvent.getAddedAlcohol();
		int quality = drinkEvent.getQuality();
		List<PotionEffect> effects = getBrewEffects(brew.getEffects(), quality);

		applyEffects(effects, player, PlayerEffectEvent.EffectType.DRINK);
		if (brewAlc < 0) {
			// If the Drink has negative alcohol, drain some alcohol
			bPlayer.drain(player, -brewAlc);
		} else if (brewAlc > 0) {
			bPlayer.drunkenness += brewAlc;
			if (quality > 0) {
				bPlayer.quality += quality * brewAlc;
			} else {
				bPlayer.quality += brewAlc;
			}

			applyEffects(getQualityEffects(quality, brewAlc), player, PlayerEffectEvent.EffectType.QUALITY);
		}

		if (bPlayer.drunkenness > 100) {
			bPlayer.drinkCap(player);
		}

		if (BConfig.showStatusOnDrink) {
			// Only show the Player his drunkenness if he is already drunk, or this drink changed his drunkenness
			if (brewAlc != 0 || bPlayer.drunkenness > 0) {
				bPlayer.showDrunkenness(player);
			}
		}

		bPlayer.saveToHazelcast();
		if (bPlayer.drunkenness <= 0) {
			bPlayer.remove();
		}
		return true;
	}

	/**
	 * Show the Player his current drunkenness and quality as an Actionbar graphic or when unsupported, in chat
	 */
	public void showDrunkenness(Player player) {
		try {
			// If this returns false, then the Action Bar is not supported. Do not repeat the message as it was sent into chat
			if (sendDrunkennessMessage(player)) {
				BreweryPlugin.getScheduler().runTaskLater(() -> sendDrunkennessMessage(player), 40);
				BreweryPlugin.getScheduler().runTaskLater(() -> sendDrunkennessMessage(player), 80);
			}
		} catch (Exception e) {
			plugin.errorLog("Failed to show drunkenness to " + player.getName(), e);
		}
	}


	// Player has drunken too much
	public void drinkCap(Player player) {
		quality = getQuality() * 100;
		drunkenness = 100;
		if (BConfig.overdrinkKick && !player.hasPermission("brewery.bypass.overdrink")) {
			BreweryPlugin.getScheduler().runTaskLater(() -> passOut(player), 1);
		} else {
			addPuke(player, 60 + (int) (Math.random() * 60.0));
			BreweryPlugin.getInstance().msg(player, plugin.languageReader.get("Player_CantDrink"));
		}
	}

	// push the player around if he moves
	public static void playerMove(PlayerMoveEvent event) {
		BPlayer bPlayer = get(event.getPlayer());
		if (bPlayer != null) {
			bPlayer.move(event);
		}
	}

	// Eat something to drain the drunkenness
	public void drainByItem(Player player, Material mat) {
		int strength = BConfig.drainItems.get(mat);
		if (drain(player, strength)) {
			remove(player);
		}
	}

	// drain the drunkenness by amount, returns true when player has to be removed
	public boolean drain(@Nullable Player player, int amount) {
		if (drunkenness > 0) {
			quality -= getQuality() * amount;
		}
		drunkenness -= amount;
		if (drunkenness > 0) {
			if (offlineDrunk == 0) {
				if (player == null) {
					offlineDrunk = drunkenness;
				}
			}
		} else {
			if (offlineDrunk == 0) {
				return true;
			}
			if (drunkenness == 0) {
				drunkenness--;
			}
			quality = getQuality();
			if (drunkenness <= -offlineDrunk) {
				return drunkenness <= -BConfig.hangoverTime;
			}
		}
		// TODO: Savehere?
		return false;
	}



	public void passOut(Player player) {
		player.kickPlayer(BreweryPlugin.getInstance().languageReader.get("Player_DrunkPassOut"));
		offlineDrunk = drunkenness;
	}


	// #### Login ####

	public boolean canJoinSimpleStatus() {
		return canJoin() == 0;
	}

	// can the player login or is he too drunk
	public int canJoin() {
		if (drunkenness <= 70) {
			return 0;
		}
		if (!BConfig.enableLoginDisallow) {
			if (drunkenness <= 100) {
				return 0;
			} else {
				return 3;
			}
		}
		if (drunkenness <= 90) {
			if (Math.random() > 0.4) {
				return 0;
			} else {
				return 2;
			}
		}
		if (drunkenness <= 100) {
			if (Math.random() > 0.6) {
				return 0;
			} else {
				return 2;
			}
		}
		return 3;
	}

	// player joins
	public void join(final Player player) {
		if (offlineDrunk == 0) {
			return;
		}
		// delayed login event as the player is not fully accessible pre login
		BreweryPlugin.getScheduler().runTaskLater(() -> login(player), 1L);
	}

	// he may be having a hangover
	public void login(final Player player) {
		if (drunkenness < 10) {
			if (offlineDrunk > 60) {
				if (BConfig.enableHome && !player.hasPermission("brewery.bypass.teleport")) {
					goHome(player);
				}
			}
			if (offlineDrunk > 20) {
				hangoverEffects(player);
				showDrunkenness(player);
			}
			if (drunkenness <= 0) {
				remove(player);
			}

		} else if (offlineDrunk - drunkenness >= 30) {
			if (BConfig.enableWake && !player.hasPermission("brewery.bypass.teleport")) {
				Location randomLoc = Wakeup.getRandom(player.getLocation());
				if (randomLoc != null) {
					player.teleport(randomLoc);
					plugin.msg(player, plugin.languageReader.get("Player_Wake"));
				}
			}
        }
		offlineDrunk = 0;
	}

	public void disconnecting() {
		offlineDrunk = drunkenness;
	}

	public void goHome(final Player player) {
		String homeType = BConfig.homeType;
		if (homeType != null) {
			Location home = null;
			if (homeType.equalsIgnoreCase("bed")) {
				home = player.getBedSpawnLocation();
			} else if (homeType.startsWith("cmd: ")) {
				player.performCommand(homeType.substring(5));
			} else if (homeType.startsWith("cmd:")) {
				player.performCommand(homeType.substring(4));
			} else {
				plugin.errorLog("Config.yml 'homeType: " + homeType + "' unknown!");
			}
			if (home != null) {
				player.teleport(home);
			}
		}
	}

	public void recalculateAlcRecovery(@Nullable Player player) {
		setAlcRecovery(2);
		if (player != null) {
			int rec = PermissionUtil.getAlcRecovery(player);
			if (rec > -1) {
				setAlcRecovery(rec);
			}
		}
	}


	// #### Puking ####

	// Chance that players puke on big drunkenness
	// runs every 6 sec, average chance is 15%, so should puke about every 40 sec
	// good quality can decrease the chance by up to 15%
	public void drunkPuke(Player player) {
		if (drunkenness >= 90) {
			// chance between 20% and 10%
			if (Math.random() < 0.20f - (getQuality() / 100f)) {
				addPuke(player, 20 + (int) (Math.random() * 40));
			}
		} else if (drunkenness >= 80) {
			// chance between 15% and 0%
			if (Math.random() < 0.15f - (getQuality() / 66f)) {
				addPuke(player, 10 + (int) (Math.random() * 30));
			}
		} else if (drunkenness >= 70) {
			// chance between 10% at 1 quality and 0% at 6 quality
			if (Math.random() < 0.10f - (getQuality() / 60f)) {
				addPuke(player, 10 + (int) (Math.random() * 20));
			}
		}
	}

	// make a Player puke "count" items
	public static void addPuke(Player player, int count) {
		if (!BConfig.enablePuke) {
			return;
		}

		PlayerPukeEvent event = new PlayerPukeEvent(player, count);
		BreweryPlugin.getInstance().getServer().getPluginManager().callEvent(event);
		if (event.isCancelled() || event.getCount() < 1) {
			return;
		}
		BUtil.reapplyPotionEffect(player, PotionEffectType.HUNGER.createEffect(80, 4), true);

		if (pukeTasks.isEmpty()) {
			task = BreweryPlugin.getScheduler().runTaskTimer(player, BPlayer::pukeTask, 1L, 1L);
		}
		pukeTasks.put(player, event.getCount());
	}

	public static void pukeTask() {
		for (Iterator<Map.Entry<Player, Integer>> iter = pukeTasks.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry<Player, Integer> entry = iter.next();
			Player player = entry.getKey();
			int count = entry.getValue();
			if (!player.isValid() || !player.isOnline()) {
				iter.remove();
			}
			puke(player);
			if (count <= 1) {
				iter.remove();
			} else {
				entry.setValue(count - 1);
			}
		}
		if (pukeTasks.isEmpty()) {
			task.cancel();
		}
	}

	public static void puke(Player player) {
		if (pukeRand == null) {
			pukeRand = new Random();
		}
		if (BConfig.pukeItem == null || BConfig.pukeItem.isEmpty()) {
			BConfig.pukeItem = List.of(Material.SOUL_SAND);
		}
		Location loc = player.getLocation();
		loc.setY(loc.getY() + 1.1);
		loc.setPitch(loc.getPitch() - 10 + pukeRand.nextInt(20));
		loc.setYaw(loc.getYaw() - 10 + pukeRand.nextInt(20));
		Vector direction = loc.getDirection();
		direction.multiply(0.5);
		loc.add(direction);

		Item item = player.getWorld().dropItem(loc, new ItemStack(BConfig.pukeItem.get(new Random().nextInt(BConfig.pukeItem.size()))));
		item.setVelocity(direction);
		item.setPickupDelay(32767); // Item can never be picked up when pickup delay is 32767
		item.setMetadata("brewery_puke", new FixedMetadataValue(BreweryPlugin.getInstance(), true));
		if (VERSION.isOrLater(MinecraftVersion.V1_14)) item.setPersistent(false); // No need to save Puke items

		int pukeDespawntime = BConfig.pukeDespawntime;
		if (pukeDespawntime >= 5800) {
			return;
		}

		// Setting the age determines when an item is despawned. At age 6000 it is removed.
		if (pukeDespawntime <= 0) {
			// Just show the item for a few ticks
			item.setTicksLived(5996);
		} else if (pukeDespawntime <= 120) {
			// it should despawn in less than 6 sec. Add up to half of that randomly
			item.setTicksLived(6000 - pukeDespawntime + pukeRand.nextInt((int) (pukeDespawntime / 2F)));
		} else {
			// Add up to 5 sec randomly
			item.setTicksLived(6000 - pukeDespawntime + pukeRand.nextInt(100));
		}
	}


	// #### Effects ####

	public static void applyEffects(List<PotionEffect> effects, Player player, PlayerEffectEvent.EffectType effectType) {
		PlayerEffectEvent event = new PlayerEffectEvent(player, effectType, effects);
		Bukkit.getPluginManager().callEvent(event);
		effects = event.getEffects();
		if (event.isCancelled() || effects == null) {
			return;
		}
		for (PotionEffect effect : effects) {
			BUtil.reapplyPotionEffect(player, effect, true);
		}
	}

	public void drunkEffects(Player player) {
		int duration = 10 - getQuality();
		duration += drunkenness / 2;
		duration *= 5;
		if (duration > 240) {
			duration *= 5;
		} else if (duration < 115) {
			duration = 115;
		}
		if (VERSION.isOrEarlier(MinecraftVersion.V1_14)) {
			duration *= 4;
		}
		List<PotionEffect> l = new ArrayList<>(1);
		l.add(PotionEffectType.CONFUSION.createEffect(duration, 0));

		PlayerEffectEvent event = new PlayerEffectEvent(player, PlayerEffectEvent.EffectType.ALCOHOL, l);
		Bukkit.getPluginManager().callEvent(event);
		l = event.getEffects();
		if (event.isCancelled() || l == null) {
			return;
		}
		for (PotionEffect effect : l) {
			BreweryPlugin.getScheduler().runTask(player, () -> effect.apply(player)); // Fix can't add effect to entities Async
		}
		saveToHazelcast();
	}

	public static List<PotionEffect> getQualityEffects(int quality, int brewAlc) {
		List<PotionEffect> out = new ArrayList<>(2);
		int duration = 7 - quality;
		if (quality == 0) {
			duration *= 125;
		} else if (quality <= 5) {
			duration *= 62;
		} else {
			duration = 25;
			if (brewAlc <= 10) {
				duration = 0;
			}
		}
		if (VERSION.isOrEarlier(MinecraftVersion.V1_14)) {
			duration *= 4;
		}
		if (duration > 0) {
			out.add(PotionEffectType.POISON.createEffect(duration, 0));
		}

		if (brewAlc > 10) {
			if (quality <= 5) {
				duration = 10 - quality;
				duration += brewAlc;
				duration *= 15;
			} else {
				duration = 30;
			}
			if (VERSION.isOrEarlier(MinecraftVersion.V1_14)) {
				duration *= 4;
			}
			out.add(PotionEffectType.BLINDNESS.createEffect(duration, 0));
		}
		return out;
	}

	public static void addQualityEffects(int quality, int brewAlc, Player player) {
		List<PotionEffect> list = getQualityEffects(quality, brewAlc);
		PlayerEffectEvent event = new PlayerEffectEvent(player, PlayerEffectEvent.EffectType.QUALITY, list);
		Bukkit.getPluginManager().callEvent(event);
		list = event.getEffects();
		if (event.isCancelled() || list == null) {
			return;
		}
		for (PotionEffect effect : list) {
			BUtil.reapplyPotionEffect(player, effect, true);
		}
	}

	public static List<PotionEffect> getBrewEffects(List<BEffect> effects, int quality) {
		List<PotionEffect> out = new ArrayList<>();
		if (effects != null) {
			for (BEffect effect : effects) {
				PotionEffect e = effect.generateEffect(quality);
				if (e != null) {
					out.add(e);
				}
			}
		}
		return out;
	}

	public static void addBrewEffects(Brew brew, Player player) {
		List<BEffect> effects = brew.getEffects();
		if (effects != null) {
			for (BEffect effect : effects) {
				effect.apply(brew.getQuality(), player);
			}
		}
	}

	public void hangoverEffects(final Player player) {
		int duration = offlineDrunk * 25 * getHangoverQuality();
		if (VERSION.isOrEarlier(MinecraftVersion.V1_14)) {
			duration *= 2;
		}
		int amplifier = getHangoverQuality() / 3;

		List<PotionEffect> list = new ArrayList<>(2);
		list.add(PotionEffectType.SLOW.createEffect(duration, amplifier));
		list.add(PotionEffectType.HUNGER.createEffect(duration, amplifier));

		PlayerEffectEvent event = new PlayerEffectEvent(player, PlayerEffectEvent.EffectType.HANGOVER, list);
		BreweryPlugin.getInstance().getServer().getPluginManager().callEvent(event);
		list = event.getEffects();
		if (event.isCancelled() || list == null) {
			return;
		}
		for (PotionEffect effect : list) {
			BUtil.reapplyPotionEffect(player, effect, true);
		}
	}


	// #### Scheduled ####

	public static void drunkenness() {
		Map<UUID, BPlayer> players = HazelcastCacheManager.getOwnedPlayers();
		for (Map.Entry<UUID, BPlayer> entry : players.entrySet()) {
			BPlayer bplayer = entry.getValue();

			if (bplayer.drunkenness > 30) {
				if (bplayer.offlineDrunk == 0) {
					Player player = Bukkit.getPlayer(entry.getKey());
					if (player != null) {

						bplayer.drunkEffects(player);

						if (BConfig.enablePuke) {
							bplayer.drunkPuke(player);
						}

					}
				}
			}
			bplayer.saveToHazelcast();
		}
	}

	// decreasing drunkenness over time
	public static void onUpdate() {
		IMap<UUID, BPlayer> players = hazelcast.getMap(HazelcastCacheManager.CacheType.PLAYERS.getHazelcastName());
		if (players.isEmpty()) {
			return;
		}

		for (Map.Entry<UUID, BPlayer> entry : HazelcastCacheManager.getOwnedPlayers().entrySet()) { // lil jank
			UUID uuid = entry.getKey();
			BPlayer bplayer = entry.getValue();
			Player playerIfOnline = Bukkit.getPlayer(uuid);


			if (bplayer.getAlcRecovery() == -1) {
				bplayer.recalculateAlcRecovery(playerIfOnline);
			}
			if (bplayer.drain(playerIfOnline, bplayer.getAlcRecovery())) {
				players.remove(uuid);
			}
		}
	}


	// #### getter/setter ####


	public UUID getUuid() {
		return uuid;
	}

	public int getDrunkeness() {
		return drunkenness;
	}

	public void setDrunkeness(int value) {
		drunkenness = value;
	}

	public void setData(int drunkenness, int quality) {
		if (quality > 0) {
			this.quality = quality * drunkenness;
		} else {
			if (this.quality == 0) {
				this.quality = 5 * drunkenness;
			} else {
				this.quality = getQuality() * drunkenness;
			}
		}
		this.drunkenness = drunkenness;
	}

	public int getQuality() {
		if (drunkenness == 0) {
			BreweryPlugin.getInstance().errorLog("drunkenness should not be 0!");
			return quality;
		}
		if (drunkenness < 0) {
			return quality;
		}
		return Math.round((float) quality / (float) drunkenness);
	}

	public int getQualityData() {
		return quality;
	}

	public void setQuality(int value) {
		quality = value;
		saveToHazelcast(); // OPERATION SAVED
	}

	// opposite of quality
	public int getHangoverQuality() {
		if (drunkenness < 0) {
			return quality + 11;
		}
		return -getQuality() + 11;
	}

	/**
	 * Drunkeness at the time he went offline
	 */
	public int getOfflineDrunkeness() {
		return offlineDrunk;
	}

	public int getAlcRecovery() {
		return alcRecovery;
	}

	public void setAlcRecovery(int alcRecovery) {
		this.alcRecovery = alcRecovery;
		saveToHazelcast(); // OPERATION SAVED
	}


	// player is drunk
	public void move(PlayerMoveEvent event) {
		// has player more alc than 10
		if (drunkenness >= 10 && BConfig.stumbleModifier > 0.001f) {
			if (drunkenness <= 100) {
				if (time > 1) {
					time--;
				} else {
					// Is he moving
					if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
						Player player = event.getPlayer();
						// We have to cast here because it had issues otherwise on previous versions of Minecraft
						// Don't know if that's still the case, but we better leave it
						// not in midair
						if (((Entity) player).isOnGround()) {
							time--;
							if (time == 0) {
								// push him only to the side? or any direction
								// like now
								if (VERSION.isOrLater(MinecraftVersion.V1_9)) { // Pushing is way stronger in 1.9
									push.setX((Math.random() - 0.5) / 2.0);
									push.setZ((Math.random() - 0.5) / 2.0);
								} else {
									push.setX(Math.random() - 0.5);
									push.setZ(Math.random() - 0.5);
								}
								push.multiply(BConfig.stumbleModifier);
								PlayerPushEvent pushEvent = new PlayerPushEvent(player, push, this);
								BreweryPlugin.getInstance().getServer().getPluginManager().callEvent(pushEvent);
								push = pushEvent.getPush();
								if (pushEvent.isCancelled() || push.lengthSquared() <= 0) {
									time = -10;
									return;
								}
								player.setVelocity(push);
							} else if (time < 0 && time > -10) {
								// push him some more in the same direction
								player.setVelocity(push);
							} else {
								// when more alc, push him more often
								time = (int) (Math.random() * (201.0 - (drunkenness * 2)));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Send one Message to the player, showing his drunkenness or hangover
	 *
	 * @param player The Player to send the message to
	 * @return false if the message should not be repeated.
	 */
	public boolean sendDrunkennessMessage(Player player) {
		StringBuilder b = new StringBuilder(100);

		int strength = drunkenness;
		boolean hangover = false;
		if (offlineDrunk > 0) {
			strength = offlineDrunk;
			hangover = true;
		}

		b.append(BreweryPlugin.getInstance().languageReader.get(hangover ? "Player_Hangover" : "Player_Drunkeness"));

		// Drunkenness or Hangover Strength Bars
		b.append(": §7[");
		b.append(generateBars(strength, hangover));
		b.append("§7] ");

		int quality;
		if (hangover) {
			quality = 11 - getHangoverQuality();
		} else {
			quality = strength > 0 ? getQuality() : 0;
		}

		// Quality Stars
		b.append("§7[");
		b.append(generateStars(quality));
		b.append("§7]");

		final String text = b.toString();
		if (hangover && VERSION.isOrLater(MinecraftVersion.V1_11)) {
			BreweryPlugin.getScheduler().runTaskLater(() -> player.sendTitle("", text, 30, 100, 90), 160);
			return false;
		}
		try {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
			return true;
		} catch (UnsupportedOperationException | NoSuchMethodError e) {
			player.sendMessage(text);
			return false;
		}
	}

	private String generateBars(int strength, boolean hangover) {
		// Generate 25 Bars, color one per 4 drunkenness
		StringBuilder b = new StringBuilder();
		int bars;
		if (strength <= 0) {
			bars = 0;
		} else if (strength == 1) {
			bars = 1;
		} else {
			bars = Math.round(strength / 4.0f);
		}
		int noBars = 25 - bars;
		if (bars > 0) {
			b.append(hangover ? "§c" : "§6");
		}
		for (int addedBars = 0; addedBars < bars; addedBars++) {
			b.append("|");
			if (addedBars == 20) {
				// color the last 4 bars red
				b.append("§c");
			}
		}
		if (noBars > 0) {
			b.append("§0");
			for (; noBars > 0; noBars--) {
				b.append("|");
			}
		}
		return b.toString();
	}

	public String generateBars() {
		return generateBars(offlineDrunk > 0 ? offlineDrunk : drunkenness, offlineDrunk > 0);
	}

	private String generateStars(int quality) {
		// Generate stars representing the quality
		StringBuilder b = new StringBuilder();
		int stars = quality / 2;
		boolean half = quality % 2 > 0;
		int noStars = 5 - stars - (half ? 1 : 0);

		b.append(BrewLore.getQualityColor(quality));
		for (; stars > 0; stars--) {
			b.append("⭑");
		}
		if (half) {
			b.append("⭒");
		}
		if (noStars > 0) {
			b.append("§0");
			for (; noStars > 0; noStars--) {
				b.append("⭑");
			}
		}

		return b.toString();
	}

	public String generateStars() {
		return generateStars(offlineDrunk > 0 ? 11 - getHangoverQuality() : drunkenness > 0 ? getQuality() : 0);
	}


	@Override
	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	@Override
	public UUID getOwner() {
		return owner;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BPlayer player)) return false;
		return Objects.equals(uuid, player.uuid);
	}

	@Override
	public String toString() {
		return "BPlayer{" +
				"owner=" + owner +
				", uuid='" + uuid + '\'' +
				", push=" + push +
				", quality=" + quality +
				", drunkenness=" + drunkenness +
				", offlineDrunk=" + offlineDrunk +
				", alcRecovery=" + alcRecovery +
				", time=" + time +
				'}';
	}


	@Serial
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(owner);
		out.writeObject(uuid);
		out.writeObject(push.serialize());
		out.writeInt(quality);
		out.writeInt(drunkenness);
		out.writeInt(offlineDrunk);
		out.writeInt(alcRecovery);
		out.writeInt(time);
	}

	@Serial
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		owner = (UUID) in.readObject();
		uuid = (UUID) in.readObject();
		push = Vector.deserialize((Map<String, Object>) in.readObject());
		quality = in.readInt();
		drunkenness = in.readInt();
		offlineDrunk = in.readInt();
		alcRecovery = in.readInt();
		time = in.readInt();
	}

}

