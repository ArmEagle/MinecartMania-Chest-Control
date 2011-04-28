package com.afforess.minecartmaniachestcontrol;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

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
		Location spawn = chest.getLocation();
		Location result = null;
		Block neighbor = chest.getNeighborChest() != null ? chest.getNeighborChest().getLocation().getBlock() : null;

		for (Sign sign : signList) {
			for (int i = 0; i < sign.getNumLines(); i++) {
				if (sign.getLine(i).toLowerCase().contains("spawn north")) {
					sign.setLine(i, "[Spawn North]");
					result = getAdjacentTrack(spawn.getBlock(), BlockFace.NORTH);
					if (result == null && neighbor != null) return getAdjacentTrack(neighbor, BlockFace.NORTH);
					else return result;
				}
				if (sign.getLine(i).toLowerCase().contains("spawn east")) {
					sign.setLine(i, "[Spawn East]");
					result = getAdjacentTrack(spawn.getBlock(), BlockFace.EAST);
					if (result == null && neighbor != null) return getAdjacentTrack(neighbor, BlockFace.EAST);
					else return result;
				}
				if (sign.getLine(i).toLowerCase().contains("spawn south")) {
					sign.setLine(i, "[Spawn South]");
					result = getAdjacentTrack(spawn.getBlock(), BlockFace.SOUTH);
					if (result == null && neighbor != null) return getAdjacentTrack(neighbor, BlockFace.SOUTH);
					else return result;
				}
				if (sign.getLine(i).toLowerCase().contains("spawn west")) {
					sign.setLine(i, "[Spawn West]");
					result = getAdjacentTrack(spawn.getBlock(), BlockFace.WEST);
					if (result == null && neighbor != null) return getAdjacentTrack(neighbor, BlockFace.WEST);
					else return result;
				}
			}
		}
		
		
		return null;
	}
	
	private static Location getAdjacentTrack(Block center, BlockFace dir) {
		if (MinecartUtils.isTrack(center.getFace(dir))) {
			return center.getFace(dir).getLocation();
		}
		if (center.getFace(dir).getTypeId() == Item.CHEST.getId() && MinecartUtils.isTrack(center.getFace(dir).getFace(dir))) {
			return center.getFace(dir).getFace(dir).getLocation();
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
