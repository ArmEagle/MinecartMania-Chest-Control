package com.afforess.minecartmaniachestcontrol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Location;

import com.afforess.minecartmaniachestcontrol.itemcontainer.ItemCollectionManager;
import com.afforess.minecartmaniachestcontrol.signs.MaximumItemAction;
import com.afforess.minecartmaniachestcontrol.signs.MinimumItemAction;
import com.afforess.minecartmaniacore.world.Item;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.minecart.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;
import com.afforess.minecartmaniacore.event.ChestPoweredEvent;
import com.afforess.minecartmaniacore.event.MinecartActionEvent;
import com.afforess.minecartmaniacore.event.MinecartDirectionChangeEvent;
import com.afforess.minecartmaniacore.event.MinecartManiaListener;
import com.afforess.minecartmaniacore.event.MinecartManiaSignFoundEvent;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.signs.SignAction;
import com.afforess.minecartmaniacore.utils.BlockUtils;
import com.afforess.minecartmaniacore.utils.ComparableLocation;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;

public class MinecartManiaActionListener extends MinecartManiaListener{
	
	public void onChestPoweredEvent(ChestPoweredEvent event) {
		if (event.isPowered() && !event.isActionTaken()) {

			MinecartManiaChest chest = event.getChest();
			Item minecartType = SignCommands.getMinecartType(chest);
			Location spawnLocation = SignCommands.getSpawnLocationSignOverride(chest);
			if (spawnLocation == null) {
				spawnLocation = ChestStorage.getSpawnLocation(chest);
			}
			if (spawnLocation != null && chest.contains(minecartType)) {
				if (chest.canSpawnMinecart() && chest.removeItem(minecartType.getId())) {
					CompassDirection direction = SignCommands.getDirection(chest.getLocation(), spawnLocation);
					MinecartManiaMinecart minecart = MinecartManiaWorld.spawnMinecart(spawnLocation, minecartType, chest);
					minecart.setMotion(direction, (Double)MinecartManiaWorld.getConfigurationValue("SpawnAtSpeed"));
					event.setActionTaken(true);
				}
			}
		}
	}
	
	@Override
	public void onMinecartManiaSignFoundEvent(MinecartManiaSignFoundEvent event) {
		Sign sign = event.getSign();
		SignAction test = new MaximumItemAction(sign);
		if (test.valid(sign)) {
			sign.addSignAction(test);
		}
		test = new MinimumItemAction(sign);
		if (test.valid(sign)) {
			sign.addSignAction(test);
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
			}
			event.setActionTaken(action);
		}
	}
	
	public void onMinecartDirectionChangeEvent(MinecartDirectionChangeEvent event) {
		if (event.getMinecart().isStorageMinecart()) {
			ItemCollectionManager.updateContainerDirections((MinecartManiaStorageCart)event.getMinecart());
		}
	}
	
	private HashSet<ComparableLocation> calculateLocationsInRange(MinecartManiaStorageCart minecart) {
		HashSet<ComparableLocation> previousBlocks = toComparableLocation(BlockUtils.getAdjacentLocations(minecart.getPrevLocation(), minecart.getItemRange()));
		HashSet<ComparableLocation> current = toComparableLocation(BlockUtils.getAdjacentLocations(minecart.minecart.getLocation(), minecart.getItemRange()));
		current.removeAll(previousBlocks);
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
			if (!(temp.getBlock().getState() instanceof org.bukkit.block.Sign)) {
				i.remove();
			}
		}
	}

}
