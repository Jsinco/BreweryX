package com.dre.brewery.utility;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.files.Lang;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;


/**
 * Update Checker modified for BreweryX
 */

public class UpdateChecker {

	private static final BreweryPlugin plugin = BreweryPlugin.getInstance();
	private static final Lang lang = ConfigManager.getConfig(Lang.class);

	@Getter @Setter
	private static String latestVersion = plugin.getDescription().getVersion();
	@Getter @Setter
	private static boolean updateAvailable = false;

	private final int resourceID;

	public UpdateChecker(int resourceID) {
		this.resourceID = resourceID;
	}

	public static void notify(final Player player) {
		if (!updateAvailable || !player.hasPermission("brewery.update")) {
			return;
		}

		player.sendMessage(lang.getEntry("Etc_UpdateAvailable", "v" + plugin.getDescription().getVersion(), "v" + latestVersion));
	}

	/**
	 * Query the API to find the latest approved file's details.
	 */
	public void query(final Consumer<String> consumer) {
		BreweryPlugin.getScheduler().runTaskAsynchronously(() -> {
			try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceID + "/~").openStream(); Scanner scann = new Scanner(is)) {
				if (scann.hasNext()) {
					consumer.accept(scann.next());
				}
			} catch (IOException e) {
				plugin.getLogger().log(Level.WARNING, "Cannot look for updates: " + e);
			}
		});
	}

	public static int parseVersion(String version) {
		StringBuilder sb = new StringBuilder();
		for (char c : version.toCharArray()) {
			if (Character.isDigit(c)) {
				sb.append(c);
			}
		}
		return Integer.parseInt(sb.toString());
	}

	public static void run(int resourceID) {
		new UpdateChecker(resourceID).query(latestVersion -> {
			String currentVersion = plugin.getDescription().getVersion();

			if (UpdateChecker.parseVersion(latestVersion) > UpdateChecker.parseVersion(currentVersion)) {
				UpdateChecker.setUpdateAvailable(true);
				plugin.log(lang.getEntry("Etc_UpdateAvailable", "v" + currentVersion, "v" + latestVersion));
			}
			UpdateChecker.setLatestVersion(latestVersion);
		});
	}
}
