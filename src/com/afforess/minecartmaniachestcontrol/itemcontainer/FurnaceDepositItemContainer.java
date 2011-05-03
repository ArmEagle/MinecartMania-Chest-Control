package com.afforess.minecartmaniachestcontrol.itemcontainer;

import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;
import com.afforess.minecartmaniacore.inventory.MinecartManiaFurnace;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;

public class FurnaceDepositItemContainer extends GenericItemContainer implements ItemContainer{
	private MinecartManiaFurnace furnace;
	private static final int SLOT = 2;
	public FurnaceDepositItemContainer(MinecartManiaFurnace furnace,String line, CompassDirection direction) {
		super(line, direction);
		this.furnace = furnace;
	}

	@Override
	public void doCollection(MinecartManiaInventory deposit) {
		for (CompassDirection direction : directions) {
			AbstractItem[] list = getItemList(direction);
			for (AbstractItem item : list) {
				if (item != null) {
					short data = (short) (item.hasData() ? item.getData() : -1);
					//does not match the item already in the slot, continue
					if (furnace.getItem(SLOT) == null || !item.equals(Item.getItem(furnace.getItem(SLOT)))) {
						continue;
					}
					int toRemove = furnace.getItem(SLOT).getAmount();
					if (!item.isInfinite() && item.getAmount() < toRemove) {
						toRemove = item.getAmount();
					}
					item.setAmount(toRemove);
					if (furnace.canRemoveItem(item.getId(), toRemove, data)) {
						if (deposit.canAddItem(item.toItemStack())) {
							furnace.setItem(SLOT, null);
							deposit.addItem(item.toItemStack());
							return;
						}
					}
					
				}
			}
		}
	}

}
