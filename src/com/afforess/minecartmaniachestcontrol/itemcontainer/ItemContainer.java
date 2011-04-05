package com.afforess.minecartmaniachestcontrol.itemcontainer;

import com.afforess.minecartmaniacore.AbstractItem;
import com.afforess.minecartmaniacore.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;

public interface ItemContainer {
	
	public boolean hasDirectionCondition();
	
	public boolean hasAmountCondition();
	
	public AbstractItem[] getRawItemList();
	
	public AbstractItem[] getItemList(CompassDirection direction);
	
	public void addDirection(CompassDirection direction);
	
	public void doCollection(MinecartManiaInventory other);

}
