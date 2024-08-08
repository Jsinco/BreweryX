package com.dre.brewery;

import com.dre.brewery.hazelcast.HazelcastCacheManager;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.utility.BUtil;
import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

// Wtf is this even for, and why is it not in the BPlayer class? - Jsinco
public class Wakeup implements Serializable { // Wakeups aren't ticked as far as I can tell so there's no reason to make these ownable for the moment

	@Serial
	private static final long serialVersionUID = -8998830092919696237L;

	private static final HazelcastInstance hazelcast = BreweryPlugin.getHazelcast();
	private static final BreweryPlugin plugin = BreweryPlugin.getInstance();

	public static int checkId = -1;
	public static Player checkPlayer = null;

	private Location loc;
	private UUID id;
	private boolean active = true;

	public Wakeup(Location loc) {
		this.loc = loc;
		this.id = UUID.randomUUID();
	}

	// load from save data
	public Wakeup(Location loc, UUID id) {
		this.loc = loc;
		this.id = id;
	}

	public Location getLoc() {
		return loc;
	}

	public UUID getId() {
		return id;
	}


	public void saveToHazelcast() {
		IList<Wakeup> wakeups = hazelcast.getList(HazelcastCacheManager.CacheType.WAKEUPS.getHazelcastName());
		int i = 0;
		for (Wakeup barrel : wakeups) {
			if (barrel.getId().equals(id)) {
				wakeups.set(i, this); // OPERATION SAVED
				System.out.println("Barrel saved to Hazelcast: " + this.id);
				return;
			}
			i++;
		}
	}


	// get the nearest of two random Wakeup-Locations
	public static Location getRandom(Location playerLoc) {
		IList<Wakeup> wakeups = hazelcast.getList(HazelcastCacheManager.CacheType.WAKEUPS.getHazelcastName());

		if (wakeups.isEmpty()) {
			return null;
		}

		List<Wakeup> worldWakes = wakeups.stream()
			.filter(w -> w.active)
			.filter(w -> w.loc.getWorld().equals(playerLoc.getWorld()))
			.collect(Collectors.toList());

		if (worldWakes.isEmpty()) {
			return null;
		}

		Wakeup w1 = calcRandom(worldWakes);
		worldWakes.remove(w1);
		if (w1 == null) return null;

		while (!w1.check()) {
			plugin.errorLog("Please Check Wakeup-Location with id: &6" + wakeups.indexOf(w1));

			w1 = calcRandom(worldWakes);
			if (w1 == null) {
				return null;
			}
			worldWakes.remove(w1);
		}

		Wakeup w2 = calcRandom(worldWakes);
		if (w2 != null) {
			worldWakes.remove(w2);

			while (!w2.check()) {
				plugin.errorLog("Please Check Wakeup-Location with id: &6" + wakeups.indexOf(w2));

				w2 = calcRandom(worldWakes);
				if (w2 == null) {
					return w1.loc;
				}
				worldWakes.remove(w2);
			}


			if (w1.loc.distanceSquared(playerLoc) > w2.loc.distanceSquared(playerLoc)) {
				return w2.loc;
			}
		}
		return w1.loc;
	}

	public static Wakeup calcRandom(List<Wakeup> worldWakes) {
		if (worldWakes.isEmpty()) {
			return null;
		}
		return worldWakes.get((int) Math.round(Math.random() * ((float) worldWakes.size() - 1.0)));
	}

	public static void set(CommandSender sender) {
		if (sender instanceof Player player) {
			IList<Wakeup> wakeups = hazelcast.getList(HazelcastCacheManager.CacheType.WAKEUPS.getHazelcastName());

            wakeups.add(new Wakeup(player.getLocation()));
			plugin.msg(sender, plugin.languageReader.get("Player_WakeCreated", "" + (wakeups.size() - 1)));

		} else {
			plugin.msg(sender, plugin.languageReader.get("Error_PlayerCommand"));
		}
	}

	public static void remove(CommandSender sender, int id) {
		IList<Wakeup> wakeups = hazelcast.getList(HazelcastCacheManager.CacheType.WAKEUPS.getHazelcastName());

		if (wakeups.isEmpty() || id < 0 || id >= wakeups.size()) {
			plugin.msg(sender, plugin.languageReader.get("Player_WakeNotExist", "" + id));//"&cDer Aufwachpunkt mit der id: &6" + id + " &cexistiert nicht!");
			return;
		}

		Wakeup wakeup = wakeups.get(id);

		if (wakeup.active) {
			wakeup.active = false;
			wakeups.set(id, wakeup); // OPERATION SAVED
			plugin.msg(sender, plugin.languageReader.get("Player_WakeDeleted", "" + id));

		} else {
			plugin.msg(sender, plugin.languageReader.get("Player_WakeAlreadyDeleted", "" + id));
		}
	}

	public static void list(CommandSender sender, int page, String worldOnly) {
		IList<Wakeup> wakeups = hazelcast.getList(HazelcastCacheManager.CacheType.WAKEUPS.getHazelcastName());

		if (wakeups.isEmpty()) {
			plugin.msg(sender, plugin.languageReader.get("Player_WakeNoPoints"));
			return;
		}

		ArrayList<String> locs = new ArrayList<>();
		for (int id = 0; id < wakeups.size(); id++) {

			Wakeup wakeup = wakeups.get(id);

			String s = "&m";
			if (wakeup.active) {
				s = "";
			}

			String world = wakeup.loc.getWorld().getName();

			if (worldOnly == null || world.equalsIgnoreCase(worldOnly)) {
				int x = (int) wakeup.loc.getX();
				int y = (int) wakeup.loc.getY();
				int z = (int) wakeup.loc.getZ();

				locs.add("&6" + s + id + "&f" + s + ": " + world + " " + x + "," + y + "," + z);
			}
		}
		BUtil.list(sender, locs, page);
	}

	public static void check(CommandSender sender, int id, boolean all) {
		IList<Wakeup> wakeups = hazelcast.getList(HazelcastCacheManager.CacheType.WAKEUPS.getHazelcastName());
		if (!(sender instanceof Player player)) {
			plugin.msg(sender, plugin.languageReader.get("Error_PlayerCommand"));
			return;
		}

		if (!all) {
			if (wakeups.isEmpty() || id >= wakeups.size()) {
				plugin.msg(sender, plugin.languageReader.get("Player_WakeNotExist", "" + id));
				return;
			}

			Wakeup wakeup = wakeups.get(id);
			if (wakeup.check()) {
				player.teleport(wakeup.loc);
			} else {
				String world = wakeup.loc.getWorld().getName();
				int x = (int) wakeup.loc.getX();
				int y = (int) wakeup.loc.getY();
				int z = (int) wakeup.loc.getZ();
				plugin.msg(sender, plugin.languageReader.get("Player_WakeFilled", "" + id, world, "" + x , "" + y, "" + z));
			}

		} else {
			if (wakeups.isEmpty()) {
				plugin.msg(sender, plugin.languageReader.get("Player_WakeNoPoints"));
				return;
			}
			if (checkPlayer != null && checkPlayer != player) {
				checkId = -1;
			}
			checkPlayer = player;
			tpNext();
		}
	}

	public boolean check() {
		return (!loc.getBlock().getType().isSolid() && !loc.getBlock().getRelative(0, 1, 0).getType().isSolid());
	}

	public static void tpNext() {
		IList<Wakeup> wakeups = hazelcast.getList(HazelcastCacheManager.CacheType.WAKEUPS.getHazelcastName());

		checkId++;
		if (checkId >= wakeups.size()) {
			plugin.msg(checkPlayer, plugin.languageReader.get("Player_WakeLast"));
			checkId = -1;
			checkPlayer = null;
			return;
		}

		Wakeup wakeup = wakeups.get(checkId);
		if (!wakeup.active) {
			tpNext();
			return;
		}

		String world = wakeup.loc.getWorld().getName();
		int x = (int) wakeup.loc.getX();
		int y = (int) wakeup.loc.getY();
		int z = (int) wakeup.loc.getZ();

		if (wakeup.check()) {
			plugin.msg(checkPlayer, plugin.languageReader.get("Player_WakeTeleport", "" + checkId, world, "" + x , "" + y, "" + z));
			checkPlayer.teleport(wakeup.loc);
		} else {
			plugin.msg(checkPlayer, plugin.languageReader.get("Player_WakeFilled", "" + checkId, world, "" + x , "" + y, "" + z));
		}
		plugin.msg(checkPlayer, plugin.languageReader.get("Player_WakeHint1"));
		plugin.msg(checkPlayer, plugin.languageReader.get("Player_WakeHint2"));
	}

	public static void cancel(CommandSender sender) {
		if (checkPlayer != null) {
			checkPlayer = null;
			checkId = -1;
			plugin.msg(sender, plugin.languageReader.get("Player_WakeCancel"));
			return;
		}
		plugin.msg(sender, plugin.languageReader.get("Player_WakeNoCheck"));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Wakeup wakeup)) return false;
        return Objects.equals(id, wakeup.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}


	@Serial
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(DataManager.serializeLocation(loc));
		out.writeObject(id);
		out.writeBoolean(active);
	}

	@Serial
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		loc = DataManager.deserializeLocation((String) in.readObject());
		id = (UUID) in.readObject();
		active = in.readBoolean();
	}
}
