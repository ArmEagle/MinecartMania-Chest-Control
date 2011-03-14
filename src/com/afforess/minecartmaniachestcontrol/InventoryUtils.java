package com.afforess.minecartmaniachestcontrol;

import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.Item;
import com.afforess.minecartmaniacore.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemUtils;
import com.afforess.minecartmaniacore.utils.StringUtils;

public class InventoryUtils {

	public static boolean doInventoryTransaction(MinecartManiaInventory withdraw, MinecartManiaInventory deposit, Sign sign, CompassDirection facing) {
		boolean action = false;
		String[] lines = new String[3];
		for (int i = 1; i < 4; i++) {
			lines[i-1] = sign.getLine(i);
			if (!sign.getLine(i).trim().isEmpty())
				sign.setLine(i, StringUtils.addBrackets(sign.getLine(i)));
		}
		sign.update();
		
		Item[] items = ItemUtils.getItemStringListToMaterial(lines, facing);
		for (Item m : items) {
			if (m != null) {
				int amount = m.getAmount();
				while (withdraw.contains(m) && (m.isInfinite() || amount > 0) ) {
					ItemStack item = withdraw.getItem(withdraw.first(m));
					int toAdd = m.isInfinite() ? item.getAmount() : (item.getAmount() > amount ? amount : item.getAmount());
					if (deposit == null) {
						//do nothing, just remove it from the withdraw inventory
					}
					else if (!deposit.addItem(new ItemStack(item.getTypeId(), toAdd, item.getDurability()))) {
						break;
					}
					withdraw.removeItem(item.getTypeId(), toAdd, item.getDurability());
					amount -= toAdd;
					action = true;
				}
			}
		}
		return action;
	}
}
