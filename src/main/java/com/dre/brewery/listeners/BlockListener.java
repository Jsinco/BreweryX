package com.dre.brewery.listeners;

import com.dre.brewery.BPlayer;
import com.dre.brewery.BSealer;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.DistortChat;
import com.dre.brewery.api.events.barrel.BarrelDestroyEvent;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.integration.barrel.BlocklockerBarrel;
import com.dre.brewery.Barrel;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockListener implements Listener {

	private static final MinecraftVersion VERSION = BreweryPlugin.getMCVersion();
	private static final BreweryPlugin plugin = BreweryPlugin.getInstance();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();

		String[] lines = event.getLines();
		if (hasBarrelLine(lines) || !BConfig.requireKeywordOnSigns) {

			if (!player.hasPermission("brewery.createbarrel.small") && !player.hasPermission("brewery.createbarrel.big")) {
				plugin.msg(player, plugin.languageReader.get("Perms_NoBarrelCreate"));
				return;
			}

			if (Barrel.create(event.getBlock(), player)) {
				plugin.msg(player, plugin.languageReader.get("Player_BarrelCreated"));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onSignChangeLow(SignChangeEvent event) {

		if (DistortChat.doSigns) {
			if (BPlayer.hasPlayer(event.getPlayer())) {
				DistortChat.signWrite(event);
			}
		}
		if (BConfig.useBlocklocker) {
			String[] lines = event.getLines();
			if (hasBarrelLine(lines) || !BConfig.requireKeywordOnSigns) {
				BlocklockerBarrel.createdBarrelSign(event.getBlock());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (VERSION.isOrEarlier(MinecraftVersion.V1_14) || event.getBlock().getType() != Material.SMOKER) return;
		BSealer.blockPlace(event.getItemInHand(), event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!BUtil.blockDestroy(event.getBlock(), event.getPlayer(), BarrelDestroyEvent.Reason.PLAYER)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {

		if (!BUtil.blockDestroy(event.getBlock(), null, BarrelDestroyEvent.Reason.BURNED)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		if (!event.isSticky()) { // Ignore blocks from other servers
			return;
		}
		for (Block block : event.getBlocks()) {
			if (Barrel.get(block) != null) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {

		for (Block block : event.getBlocks()) {
			if (Barrel.get(block) != null) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockChange(EntityChangeBlockEvent event) {
		if (Barrel.get(event.getBlock()) != null) {
			event.setCancelled(true);
		}
	}

	public static boolean hasBarrelLine(String[] lines) {
		for (String line : lines) {
			if (line.equalsIgnoreCase("Barrel") || line.equalsIgnoreCase(plugin.languageReader.get("Etc_Barrel"))) {
				return true;
			}
		}
		return false;
	}

}
