package com.afforess.minecartmaniachestcontrol;

import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.Item;
import com.afforess.minecartmaniacore.MinecartManiaFurnace;
import com.afforess.minecartmaniacore.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemUtils;
import com.afforess.minecartmaniacore.utils.StringUtils;

public class InventoryUtils {
	
	public static boolean doInventoryTransaction(MinecartManiaInventory withdraw, MinecartManiaInventory deposit, Sign sign) {
		return doInventoryTransaction(withdraw, deposit, sign, CompassDirection.NO_DIRECTION);
	}
	
	public static boolean doInventoryTransaction(MinecartManiaInventory withdraw, MinecartManiaInventory deposit, Sign sign, CompassDirection facing) {
		boolean action = false;
		String[] lines = new String[3];
		for (int i = 1; i < 4; i++) {
			lines[i-1] = sign.getLine(i);
			if (!sign.getLine(i).trim().isEmpty()) {
				sign.setLine(i, StringUtils.addBrackets(sign.getLine(i)));
			}
			//Special case, exempt fuel and smelt commands on the same line from the transaction
			if (sign.getLine(i).toLowerCase().contains("fuel") || sign.getLine(i).toLowerCase().contains("smelt")) {
				if (withdraw instanceof MinecartManiaFurnace) {
					lines[i-1] = "";
				}
			}
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
					else if (!withdraw.canRemoveItem(item.getTypeId(), toAdd, item.getDurability())) {
						break; //if we are not allowed to remove the items, give up
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

	public static boolean doFurnaceTransaction(MinecartManiaInventory withdraw, MinecartManiaInventory deposit, int slot, String line) {
		Item[] items = ItemUtils.getItemStringToMaterial(line);
		for (Item m : items) {
			if (m != null) {
				if (withdraw.contains(m)) {
					if (deposit.getItem(slot) == null) {
						deposit.setItem(slot, withdraw.getItem(withdraw.first(m)));
						withdraw.setItem(withdraw.first(m), null);
						return true;
					}
					//Merge stacks together
					if (m.equals(deposit.getItem(slot).getType())){
						ItemStack item = withdraw.getItem(withdraw.first(m));
						if (deposit.getItem(slot).getAmount() + item.getAmount() <= 64) {
							deposit.setItem(slot, new ItemStack(item.getTypeId(), deposit.getItem(slot).getAmount() + item.getAmount(), item.getDurability()));
							item = null;
						}
						else {
							int diff = deposit.getItem(slot).getAmount() + item.getAmount() - 64;
							deposit.setItem(slot, new ItemStack(item.getTypeId(), deposit.getItem(slot).getAmount() + item.getAmount(), item.getDurability()));
							item = new ItemStack(item.getTypeId(), diff);
						}
						withdraw.setItem(withdraw.first(m), item);
						return true;
					}
				}
			}
		}
		return false;
	}
}
