package com.afforess.minecartmaniachestcontrol.itemcontainer;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.AbstractItem;
import com.afforess.minecartmaniacore.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ListUtils;

public class ItemCollectionContainer extends GenericItemContainer implements ItemContainer{
	private MinecartManiaInventory inventory;

	public ItemCollectionContainer(MinecartManiaInventory inventory, String line, CompassDirection direction) {
		super(line, direction);
		this.inventory = inventory;
	}
	
	public void doCollection(MinecartManiaInventory withdraw) {
		@SuppressWarnings("unchecked")
		ArrayList<AbstractItem> rawList = (ArrayList<AbstractItem>) ListUtils.toArrayList(Arrays.asList(getRawItemList()));
		for (CompassDirection direction : directions) {
			AbstractItem[] list = getItemList(direction);
			for (AbstractItem item : list) {
				if (item != null && rawList.contains(item)) {
					int amount = item.getAmount();
					while (withdraw.contains(item.type()) && (item.isInfinite() || amount > 0) ) {
						ItemStack itemStack = withdraw.getItem(withdraw.first(item.type()));
						int toAdd = item.isInfinite() ? itemStack.getAmount() : (itemStack.getAmount() > amount ? amount : itemStack.getAmount());
						if (!withdraw.canRemoveItem(itemStack.getTypeId(), toAdd, itemStack.getDurability())) {
							break; //if we are not allowed to remove the items, give up
						}
						else if (!inventory.addItem(new ItemStack(itemStack.getTypeId(), toAdd, itemStack.getDurability()))) {
							break;
						}
						withdraw.removeItem(itemStack.getTypeId(), toAdd, itemStack.getDurability());
						amount -= toAdd;
					}
					rawList.remove(item);
				}
			}
		}
	}
}
