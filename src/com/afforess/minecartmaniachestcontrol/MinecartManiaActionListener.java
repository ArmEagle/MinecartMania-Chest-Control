package com.afforess.minecartmaniachestcontrol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import com.afforess.minecartmaniachestcontrol.itemcontainer.ItemCollectionManager;
import com.afforess.minecartmaniacore.Item;
import com.afforess.minecartmaniacore.MinecartManiaChest;
import com.afforess.minecartmaniacore.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.MinecartManiaWorld;
import com.afforess.minecartmaniacore.event.ChestPoweredEvent;
import com.afforess.minecartmaniacore.event.MinecartActionEvent;
import com.afforess.minecartmaniacore.event.MinecartDirectionChangeEvent;
import com.afforess.minecartmaniacore.event.MinecartManiaListener;
import com.afforess.minecartmaniacore.event.MinecartNearEntityEvent;
import com.afforess.minecartmaniacore.utils.ComparableLocation;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.BlockUtils;
import com.afforess.minecartmaniacore.utils.MinecartUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils;

public class MinecartManiaActionListener extends MinecartManiaListener{
	
	public void onChestPoweredEvent(ChestPoweredEvent event) {
		if (event.isPowered() && !event.isActionTaken()) {

			MinecartManiaChest chest = event.getChest();
			Item minecartType = SignCommands.getMinecartType(chest);
			Location spawnLocation = SignCommands.getSpawnLocationSignOverride(chest);
			if (spawnLocation == null && MinecartUtils.validMinecartTrack(chest.getWorld(), chest.getX() - 1, chest.getY(), chest.getZ(), 2, DirectionUtils.CompassDirection.NORTH)){
				spawnLocation = new Location(chest.getWorld(), chest.getX() - 1, chest.getY(), chest.getZ());
			}
			if (spawnLocation == null && MinecartUtils.validMinecartTrack(chest.getWorld(), chest.getX() + 1, chest.getY(), chest.getZ(), 2, DirectionUtils.CompassDirection.SOUTH)){
				spawnLocation = new Location(chest.getWorld(), chest.getX() + 1, chest.getY(), chest.getZ());
			}
			if (spawnLocation == null && MinecartUtils.validMinecartTrack(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ() - 1, 2, DirectionUtils.CompassDirection.EAST)){
				spawnLocation = new Location(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ() - 1);
			}
			if (spawnLocation == null && MinecartUtils.validMinecartTrack(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ() + 1, 2, DirectionUtils.CompassDirection.WEST)){
				spawnLocation = new Location(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ() + 1);
			}
			if (spawnLocation != null && chest.contains(minecartType)) {
				if (chest.canSpawnMinecart() && chest.removeItem(minecartType.getId())) {
					CompassDirection direction = SignCommands.getDirection(chest.getLocation(), spawnLocation);
					MinecartManiaMinecart minecart = MinecartManiaWorld.spawnMinecart(spawnLocation, minecartType, chest);
					minecart.setMotion(direction, MinecartManiaWorld.getDoubleValue(MinecartManiaWorld.getConfigurationValue("SpawnAtSpeed")));
					event.setActionTaken(true);
				}
			}
		}
	}
	
	public void onMinecartNearEntityEvent(MinecartNearEntityEvent event) {
		if (event.isActionTaken()) {
			return;
		}
		if (event.getEntity() instanceof org.bukkit.entity.Item) {
			org.bukkit.entity.Item item = (org.bukkit.entity.Item)event.getEntity();
			if (event.getMinecart().isStorageMinecart()) {
				MinecartManiaStorageCart minecart = (MinecartManiaStorageCart) event.getMinecart();
				if (minecart.addItem(item.getItemStack())) {
					item.remove();
					event.setActionTaken(true);
				}
			}
		}
	}
	
	public void onMinecartActionEvent(MinecartActionEvent event) {
		if (!event.isActionTaken()) {
			final MinecartManiaMinecart minecart = event.getMinecart();
			
			boolean action = false;
			
			if (!action) {
				action = ChestStorage.doMinecartCollection(minecart);
			}
			if (!action) {
				action = ChestStorage.doCollectParallel(minecart);
			}
			if (!action && minecart.isStorageMinecart()) {

				ItemCollectionManager.processItemContainer((MinecartManiaStorageCart)event.getMinecart());
				HashSet<ComparableLocation> locations = calculateLocationsInRange((MinecartManiaStorageCart)event.getMinecart());
				findSigns(locations);
				ItemCollectionManager.createItemContainers((MinecartManiaStorageCart)event.getMinecart(), locations);
				ChestStorage.doItemCompression((MinecartManiaStorageCart) minecart);
				ChestStorage.doEmptyChestInventory((MinecartManiaStorageCart) minecart);
				ChestStorage.setMaximumItems((MinecartManiaStorageCart) minecart);
				ChestStorage.setMinimumItems((MinecartManiaStorageCart) minecart);
			}
			event.setActionTaken(action);
		}
	}
	
	public void onMinecartDirectionChangeEvent(MinecartDirectionChangeEvent event) {
		if (event.getMinecart().isStorageMinecart()) {
			ItemCollectionManager.updateContainerDirections((MinecartManiaStorageCart)event.getMinecart());
		}
	}
	
	@SuppressWarnings("unchecked")
	private HashSet<ComparableLocation> calculateLocationsInRange(MinecartManiaStorageCart minecart) {
		HashSet<ComparableLocation> previousBlocks = null;
		if (minecart.getDataValue("Previous Item Locations") != null) {
			previousBlocks = (HashSet<ComparableLocation>)minecart.getDataValue("Previous Item Locations");
		}
		HashSet<ComparableLocation> current = toComparableLocation(BlockUtils.getAdjacentLocations(minecart.minecart.getLocation(), minecart.getItemRange()));
		if (previousBlocks != null) {
			for (ComparableLocation loc : previousBlocks) {
				current.remove(loc);
			}
		}
		
		if (previousBlocks != null) {
			Iterator<ComparableLocation> i = previousBlocks.iterator();
			while(i.hasNext()) {
				Location temp = i.next();
				if (temp.toVector().distance(minecart.minecart.getLocation().toVector()) > minecart.getItemRange()) {
					i.remove();
				}
			}
		}
		else {
			previousBlocks = new HashSet<ComparableLocation>();
		}
		previousBlocks.addAll(current);
		minecart.setDataValue("Previous Item Locations", previousBlocks);
		return current;
	}
	
	private static HashSet<ComparableLocation> toComparableLocation(HashSet<Location> set) {
		HashSet<ComparableLocation> newSet = new HashSet<ComparableLocation>(set.size());
		for (Location loc : set) {
			newSet.add(new ComparableLocation(loc));
		}
		return newSet;
	}
	
	private void findSigns(Collection<ComparableLocation> locations) {
		Iterator<ComparableLocation> i = locations.iterator();
		while (i.hasNext()) {
			Location temp = i.next();
			if (!(temp.getBlock().getState() instanceof Sign)) {
				i.remove();
			}
		}
	}

}
