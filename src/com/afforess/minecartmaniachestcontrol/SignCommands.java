package com.afforess.minecartmaniachestcontrol;

import java.util.ArrayList;

import org.bukkit.Location;

import com.afforess.minecartmaniacore.Item;
import com.afforess.minecartmaniacore.MinecartManiaChest;
import com.afforess.minecartmaniacore.signs.MinecartTypeSign;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.utils.MinecartUtils;
import com.afforess.minecartmaniacore.utils.SignUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;

public class SignCommands {
	
	public static boolean isNoCollection(MinecartManiaChest chest) {
		ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(chest.getLocation(), 2);
		for (Sign sign : signList) {
			for (int i = 0; i < sign.getNumLines(); i++) {
				if (sign.getLine(i).toLowerCase().contains("no collection")) {
					sign.setLine(i, "[No Collection]");
					return true;
				}
			}
		}
		return false;
	}

	public static Item getMinecartType(MinecartManiaChest chest) {
		ArrayList<com.afforess.minecartmaniacore.signs.Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(chest.getLocation(), 2);
		for (com.afforess.minecartmaniacore.signs.Sign sign : signList) {
			if (sign instanceof MinecartTypeSign) {
				MinecartTypeSign type = (MinecartTypeSign)sign;
				if (type.canDispenseMinecartType(Item.MINECART)) {
					if (chest.contains(Item.MINECART)) {
						return Item.MINECART;
					}
				}
				if (type.canDispenseMinecartType(Item.POWERED_MINECART)) {
					if (chest.contains(Item.POWERED_MINECART)) {
						return Item.POWERED_MINECART;
					}
				}
				if (type.canDispenseMinecartType(Item.STORAGE_MINECART)) {
					if (chest.contains(Item.STORAGE_MINECART)) {
						return Item.STORAGE_MINECART;
					}
				}
			}
		}
			

		//Returns standard minecart by default
		return Item.MINECART;
	}

	public static Location getSpawnLocationSignOverride(MinecartManiaChest chest) {
		ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(chest.getLocation(), 2);
		Location spawn = chest.getChest().getBlock().getLocation();

		for (Sign sign : signList) {
			for (int i = 0; i < sign.getNumLines(); i++) {
				if (sign.getLine(i).toLowerCase().contains("spawn north")) {
					sign.setLine(i, "[Spawn North]");
					spawn.setX(spawn.getX() - 1);
					//this may be the wrong end of a double chest, keep trying
					if (!MinecartUtils.isTrack(spawn)) {
						spawn.setX(spawn.getX() - 1);
					}
					if (!MinecartUtils.isTrack(spawn)) {
						return null;
					}
					return spawn;
				}
				if (sign.getLine(i).toLowerCase().contains("spawn east")) {
					sign.setLine(i, "[Spawn East]");
					spawn.setZ(spawn.getZ() - 1);
					if (!MinecartUtils.isTrack(spawn)) {
						spawn.setZ(spawn.getZ() - 1);
					}
					if (!MinecartUtils.isTrack(spawn)) {
						return null;
					}
					return spawn;
				}
				if (sign.getLine(i).toLowerCase().contains("spawn south")) {
					sign.setLine(i, "[Spawn South]");
					spawn.setX(spawn.getX() + 1);
					if (!MinecartUtils.isTrack(spawn)) {
						spawn.setX(spawn.getX() + 1);
					}
					if (!MinecartUtils.isTrack(spawn)) {
						return null;
					}
					return spawn;
				}
				if (sign.getLine(i).toLowerCase().contains("spawn west")) {
					sign.setLine(i, "[Spawn West]");
					spawn.setZ(spawn.getZ() + 1);
					if (!MinecartUtils.isTrack(spawn)) {
						spawn.setZ(spawn.getZ() + 1);
					}
					if (!MinecartUtils.isTrack(spawn)) {
						return null;
					}
					return spawn;
				}
			}
		}
		
		
		return null;
	}

	public static CompassDirection getDirection(Location loc1,	Location loc2) {
		if (loc1.getBlockX() - loc2.getBlockX() > 0) {
			return CompassDirection.NORTH;
		}
		if (loc1.getBlockX() - loc2.getBlockX() < 0) {
			return CompassDirection.SOUTH;
		}
		if (loc1.getBlockZ() - loc2.getBlockZ() > 0) {
			return CompassDirection.EAST;
		}
		if (loc1.getBlockZ() - loc2.getBlockZ() < 0) {
			return CompassDirection.WEST;
		}
		
		return CompassDirection.NO_DIRECTION;
	}

}
