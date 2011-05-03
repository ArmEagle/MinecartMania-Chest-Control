package com.afforess.minecartmaniachestcontrol.itemcontainer;

import java.util.HashSet;

import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.utils.ItemUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;

public abstract class GenericItemContainer implements ItemContainer{
	protected String line;
	protected final HashSet<CompassDirection> directions = new HashSet<CompassDirection>(4);
	public GenericItemContainer(String line, CompassDirection direction) {
		directions.add(direction);
		this.line = line;
	}

	@Override
	public boolean hasDirectionCondition() {
		return line.contains("+");
	}

	@Override
	public boolean hasAmountCondition() {
		return line.contains("@");
	}

	@Override
	public AbstractItem[] getRawItemList() {
		return ItemUtils.getItemStringToMaterial(line);
	}

	@Override
	public AbstractItem[] getItemList(CompassDirection direction) {
		String[] list = {line};
		return ItemUtils.getItemStringListToMaterial(list, direction);
	}

	@Override
	public void addDirection(CompassDirection direction) {
		if (hasDirectionCondition()) {
			directions.add(direction);
		}
	}
}
