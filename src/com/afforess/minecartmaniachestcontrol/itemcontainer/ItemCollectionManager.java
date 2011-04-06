package com.afforess.minecartmaniachestcontrol.itemcontainer;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;

import com.afforess.minecartmaniacore.MinecartManiaChest;
import com.afforess.minecartmaniacore.MinecartManiaDoubleChest;
import com.afforess.minecartmaniacore.MinecartManiaFurnace;
import com.afforess.minecartmaniacore.MinecartManiaInventory;
import com.afforess.minecartmaniacore.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.MinecartManiaWorld;
import com.afforess.minecartmaniacore.utils.BlockUtils;
import com.afforess.minecartmaniacore.utils.StringUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.SignUtils;

public class ItemCollectionManager {
	
	public static boolean isItemCollectionSign(Sign sign) {
		return sign.getLine(0).toLowerCase().contains("collect item");
	}
	
	public static boolean isItemDepositSign(Sign sign) {
		return sign.getLine(0).toLowerCase().contains("deposit item");
	}
	
	public static boolean isTrashItemSign(Sign sign) {
		return sign.getLine(0).toLowerCase().contains("trash item");
	}
	
	public static boolean isFurnaceFuelLine(String line) {
		return line.toLowerCase().contains("fuel:");
	}
	
	public static boolean isFurnaceSmeltLine(String line) {
		return line.toLowerCase().contains("smelt:");
	}
	
	public static MinecartManiaInventory getMinecartManiaInventory(Block block) {
		MinecartManiaInventory inventory = null;
		if (block.getState() instanceof Chest) {
			inventory = MinecartManiaWorld.getMinecartManiaChest((Chest)block.getState());
			//check for double chest
			if (inventory != null && ((MinecartManiaChest) inventory).getNeighborChest() != null) {
				inventory = new MinecartManiaDoubleChest((MinecartManiaChest) inventory, ((MinecartManiaChest) inventory).getNeighborChest());
			}
		}
		else if (block.getState() instanceof Dispenser) {
			inventory = MinecartManiaWorld.getMinecartManiaDispenser((Dispenser)block.getState());
		}
		else if (block.getState() instanceof Furnace) {
			inventory = MinecartManiaWorld.getMinecartManiaFurnace((Furnace)block.getState());
		}
		return inventory;
	}
	
	public static ArrayList<ItemContainer> getItemContainers(Location location, CompassDirection direction, boolean collection) {
		ArrayList<ItemContainer> containers = new ArrayList<ItemContainer>();
		ArrayList<Block> blocks = BlockUtils.getAdjacentBlocks(location, 1);
		for (Block block : blocks) {
			if (getMinecartManiaInventory(block) != null) {
				MinecartManiaInventory inventory = getMinecartManiaInventory(block);
				for (int line = 1; line < 4; line++) {
					String text = ((Sign)location.getBlock().getState()).getLine(line);
					if (!text.isEmpty() && !isFurnaceFuelLine(text) && !isFurnaceSmeltLine(text)) {
						ItemContainer temp = null;
						if (collection) {
							temp = new ItemCollectionContainer(inventory, text, direction);
						}
						else {
							if (inventory instanceof MinecartManiaFurnace) {
								temp = new FurnaceDepositItemContainer((MinecartManiaFurnace)inventory, text, direction);
							}
							else {
								temp = new ItemDepositContainer(inventory, text, direction);
							}
						}
						if (temp != null) {
							containers.add(temp);
						}
					}
				}
			}
		}
		return containers;
	}
	
	public static ArrayList<ItemContainer> getTrashItemContainers(Location location, CompassDirection direction) {
		ArrayList<ItemContainer> containers = new ArrayList<ItemContainer>();
		for (int line = 1; line < 4; line++) {
			String text = ((Sign)location.getBlock().getState()).getLine(line);
			if (!text.isEmpty() && !isFurnaceFuelLine(text) && !isFurnaceSmeltLine(text)) {
				containers.add(new TrashItemContainer(text, direction));
			}
		}
		return containers;
	}
	
	public static ArrayList<ItemContainer> getFurnaceContainers(Location location, CompassDirection direction) {
		ArrayList<ItemContainer> containers = new ArrayList<ItemContainer>();
		ArrayList<Block> blocks = BlockUtils.getAdjacentBlocks(location, 1);
		for (Block block : blocks) {
			if (getMinecartManiaInventory(block) != null && getMinecartManiaInventory(block) instanceof MinecartManiaFurnace) {
				MinecartManiaFurnace furnace = (MinecartManiaFurnace)getMinecartManiaInventory(block);
				for (int line = 0; line < 4; line++) {
					String text = ((Sign)location.getBlock().getState()).getLine(line);
					if (isFurnaceFuelLine(text)) {
						containers.add(new FurnaceFuelContainer(furnace, text, direction));
					}
					else if (isFurnaceSmeltLine(text)) {
						containers.add(new FurnaceSmeltContainer(furnace, text, direction));
					}
				}
			}
		}
		return containers;
	}
	
	private static void bracketizeSign(Sign sign) {
		for (int line = 0; line < 4; line++) {
			if (!sign.getLine(line).trim().isEmpty())
				sign.setLine(line, StringUtils.addBrackets(StringUtils.removeBrackets(sign.getLine(line))));
		}
	}

	
	public static void createItemContainers(MinecartManiaStorageCart minecart) {
		ArrayList<Sign> signs = SignUtils.getAdjacentSignList(minecart, minecart.getRange()+1);
		ArrayList<ItemContainer> containers = new ArrayList<ItemContainer>();
		for (Sign sign : signs) {
			if (isItemCollectionSign(sign)) {
				bracketizeSign(sign);
				containers.addAll(getItemContainers(sign.getBlock().getLocation(), minecart.getDirection(), true));
			}
			else if (isItemDepositSign(sign)) {
				bracketizeSign(sign);
				containers.addAll(getItemContainers(sign.getBlock().getLocation(), minecart.getDirection(), false));
			}
			else if (isTrashItemSign(sign)) {
				bracketizeSign(sign);
				containers.addAll(getTrashItemContainers(sign.getBlock().getLocation(), minecart.getDirection()));
			}
			containers.addAll(getFurnaceContainers(sign.getBlock().getLocation(), minecart.getDirection()));
		}
		minecart.setDataValue("ItemContainerList", containers);
	}
	
	@SuppressWarnings("unchecked")
	public static void updateContainerDirections(MinecartManiaStorageCart minecart) {
		ArrayList<ItemContainer> containers = (ArrayList<ItemContainer>) minecart.getDataValue("ItemContainerList");
		if (containers != null) {
			for (ItemContainer container : containers) {
				container.addDirection(minecart.getDirectionOfMotion());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void processItemContainer(MinecartManiaStorageCart minecart) {
		ArrayList<ItemContainer> containers = (ArrayList<ItemContainer>) minecart.getDataValue("ItemContainerList");
		if (containers != null) {
			for (ItemContainer container : containers) {
				container.doCollection(minecart);
			}
		}
	}

}
