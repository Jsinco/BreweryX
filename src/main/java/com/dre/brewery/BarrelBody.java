package com.dre.brewery;

import com.dre.brewery.storage.DataManager;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.BoundingBox;
import com.dre.brewery.utility.LegacyUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * The Blocks that make up a Barrel in the World
 */
public abstract class BarrelBody implements Serializable {

	@Serial
	private static final long serialVersionUID = 5265214885523380505L;

	protected Block spigot;
	protected BoundingBox bounds;
	protected byte signOffset;

	public BarrelBody(Block spigot, byte signOffset) {
		this.signOffset = signOffset;
		this.spigot = spigot;
		this.bounds = new BoundingBox(0, 0, 0, 0, 0, 0);
	}

	/**
	 * Loading from file
	 */
	public BarrelBody(Block spigot, byte signOffset, BoundingBox bounds) {
		this(spigot, signOffset);

		if (boundsSeemBad(bounds)) {
			if (!Bukkit.isPrimaryThread()) {
				this.bounds = null;
				return;
			}
			// If loading from old data, or block locations are missing, or other error, regenerate BoundingBox
			// This will only be done in those extreme cases.
			regenerateBounds();
		} else {
			this.bounds = bounds;
		}
	}

	/**
	 * Regenerate the Barrel Bounds.
	 */
	public abstract void regenerateBounds();

	public Block getSpigot() {
		return spigot;
	}

	@NotNull
	public BoundingBox getBounds() {
		return bounds;
	}

	public void setBounds(@NotNull BoundingBox bounds) {
		Objects.requireNonNull(bounds);
		this.bounds = bounds;
	}

	public byte getSignOffset() {
		return signOffset;
	}

	public void setSignOffset(byte signOffset) {
		this.signOffset = signOffset;
	}

	/**
	 * If the Sign of a Large Barrel gets destroyed, set signOffset to 0
	 */
	public void destroySign() {
		signOffset = 0;
	}

	/**
	 * Quick check if the bounds are valid or seem corrupt
	 */
	public static boolean boundsSeemBad(BoundingBox bounds) {
		if (bounds == null) return true;
		long area = bounds.area();
		return area > 64 || area < 4;
	}

	/**
	 * direction of the barrel from the spigot
	 */
	public static int getDirection(Block spigot) {
		int direction = 0;// 1=x+ 2=x- 3=z+ 4=z-
		Material type = spigot.getRelative(0, 0, 1).getType();
		if (LegacyUtil.isWoodPlanks(type) || LegacyUtil.isWoodStairs(type)) {
			direction = 3;
		}
		type = spigot.getRelative(0, 0, -1).getType();
		if (LegacyUtil.isWoodPlanks(type) || LegacyUtil.isWoodStairs(type)) {
			if (direction == 0) {
				direction = 4;
			} else {
				return 0;
			}
		}
		type = spigot.getRelative(1, 0, 0).getType();
		if (LegacyUtil.isWoodPlanks(type) || LegacyUtil.isWoodStairs(type)) {
			if (direction == 0) {
				direction = 1;
			} else {
				return 0;
			}
		}
		type = spigot.getRelative(-1, 0, 0).getType();
		if (LegacyUtil.isWoodPlanks(type) || LegacyUtil.isWoodStairs(type)) {
			if (direction == 0) {
				direction = 2;
			} else {
				return 0;
			}
		}
		return direction;
	}

	/**
	 * woodtype of the block the spigot is attached to
	 */
	public byte getWood() {
		Block wood;
		switch (getDirection(spigot)) { // 1=x+ 2=x- 3=z+ 4=z-
			case 0:
				return 0;
			case 1:
				wood = spigot.getRelative(1, 0, 0);
				break;
			case 2:
				wood = spigot.getRelative(-1, 0, 0);
				break;
			case 3:
				wood = spigot.getRelative(0, 0, 1);
				break;
			default:
				wood = spigot.getRelative(0, 0, -1);
		}
		try {
			return LegacyUtil.getWoodType(wood);
		} catch (NoSuchFieldError | NoClassDefFoundError noSuchFieldError) {
			// Using older minecraft versions some fields and classes do not exist
			return 0;
		}
	}

	/**
	 * Returns true if this Block is part of this Barrel
	 *
	 * @param block the block to check
	 * @return true if the given block is part of this Barrel
 	 */
	public boolean hasBlock(Block block) {
		if (block != null) {
			if (spigot.equals(block)) {
				return true;
			}
			if (spigot.getWorld().equals(block.getWorld())) {
				return bounds != null && bounds.contains(block.getX(), block.getY(), block.getZ());
			}
		}
		return false;
	}

	/**
	 * Returns true if the Offset of the clicked Sign matches the Barrel.
	 * <p>This prevents adding another sign to the barrel and clicking that.
	 */
	public boolean isSignOfBarrel(byte offset) {
		return offset == 0 || signOffset == 0 || signOffset == offset;
	}

	/**
	 * returns the Sign of a large barrel, the spigot if there is none
	 */
	public Block getSignOfSpigot() {
		if (signOffset != 0) {
			if (LegacyUtil.isSign(spigot.getType())) {
				return spigot;
			}

			if (LegacyUtil.isSign(spigot.getRelative(0, signOffset, 0).getType())) {
				return spigot.getRelative(0, signOffset, 0);
			} else {
				signOffset = 0;
			}
		}
		return spigot;
	}

	/**
	 * returns the fence above/below a block, itself if there is none
	 */
	public static Block getSpigotOfSign(Block block) {

		int y = -2;
		while (y <= 1) {
			// Fence and Netherfence
			Block relative = block.getRelative(0, y, 0);
			if (LegacyUtil.isFence(relative.getType())) {
				return (relative);
			}
			y++;
		}
		return block;
	}



	/**
	 * returns null if Barrel is correctly placed; the block that is missing when not.
	 * <p>the barrel needs to be formed correctly
	 *
	 * @param force to also check even if chunk is not loaded
	 */
	public Block getBrokenBlock(boolean force) {
		if (force || BUtil.isChunkLoaded(spigot)) {
			//spigot = getSpigotOfSign(spigot);
			if (LegacyUtil.isSign(spigot.getType())) {
				return checkSBarrel();
			} else {
				return checkLBarrel();
			}
		}
		return null;
	}

	public Block checkSBarrel() {
		int direction = getDirection(spigot);// 1=x+ 2=x- 3=z+ 4=z-
		if (direction == 0) {
			return spigot;
		}
		int startX;
		int startZ;
		int endX;
		int endZ;

		if (direction == 1) {
			startX = 1;
			startZ = -1;
		} else if (direction == 2) {
			startX = -2;
			startZ = 0;
		} else if (direction == 3) {
			startX = 0;
			startZ = 1;
		} else {
			startX = -1;
			startZ = -2;
		}
		endX = startX + 1;
		endZ = startZ + 1;

		Material type;
		int x = startX;
		int y = 0;
		int z = startZ;
		while (y <= 1) {
			while (x <= endX) {
				while (z <= endZ) {
					Block block = spigot.getRelative(x, y, z);
					type = block.getType();

					if (LegacyUtil.isWoodStairs(type)) {
						if (y == 0) {
							// stairs have to be upside down
							if (!LegacyUtil.areStairsInverted(block)) {
								return block;
							}
						}
						z++;
					} else {
						return spigot.getRelative(x, y, z);
					}
				}
				z = startZ;
				x++;
			}
			z = startZ;
			x = startX;
			y++;
		}
		bounds = new BoundingBox(
			spigot.getX() + startX,
			spigot.getY(),
			spigot.getZ() + startZ,
			spigot.getX() + endX,
			spigot.getY() + 1,
			spigot.getZ() + endZ);
		return null;
	}

	public Block checkLBarrel() {
		int direction = getDirection(spigot);// 1=x+ 2=x- 3=z+ 4=z-
		if (direction == 0) {
			return spigot;
		}
		int startX;
		int startZ;
		int endX;
		int endZ;

		if (direction == 1) {
			startX = 1;
			startZ = -1;
		} else if (direction == 2) {
			startX = -4;
			startZ = -1;
		} else if (direction == 3) {
			startX = -1;
			startZ = 1;
		} else {
			startX = -1;
			startZ = -4;
		}
		if (direction == 1 || direction == 2) {
			endX = startX + 3;
			endZ = startZ + 2;
		} else {
			endX = startX + 2;
			endZ = startZ + 3;
		}

		Material type;
		int x = startX;
		int y = 0;
		int z = startZ;
		while (y <= 2) {
			while (x <= endX) {
				while (z <= endZ) {
					Block block = spigot.getRelative(x, y, z);
					type = block.getType();
					if (direction == 1 || direction == 2) {
						if (y == 1 && z == 0) {
							z++;
							continue;
						}
					} else {
						if (y == 1 && x == 0) {
							z++;
							continue;
						}
					}
					if (LegacyUtil.isWoodPlanks(type) || LegacyUtil.isWoodStairs(type)) {
						z++;
					} else {
						return block;
					}
				}
				z = startZ;
				x++;
			}
			z = startZ;
			x = startX;
			y++;
		}
		bounds = new BoundingBox(
			spigot.getX() + startX,
			spigot.getY(),
			spigot.getZ() + startZ,
			spigot.getX() + endX,
			spigot.getY() + 2,
			spigot.getZ() + endZ);

		return null;
	}


	@Serial
	private void writeObject(ObjectOutputStream out) throws IOException {
		// Write the non-transient fields
		out.writeObject(DataManager.serializeBlock(spigot)); // Ensure Block is Serializable
		out.writeObject(bounds);
		out.writeByte(signOffset);
	}

	@Serial
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		// Read the non-transient fields

		// Deserialize custom fields, e.g., Block, BarrelBody, Inventory
		spigot = DataManager.deserializeBlock((String) in.readObject());
		bounds = (BoundingBox) in.readObject();
		signOffset = in.readByte();
	}
}
