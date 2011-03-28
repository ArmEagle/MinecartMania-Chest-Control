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
					    
				        int required = (m.isInfinite() || m.getAmount() > 64) ? 64 : m.getAmount();
				        int required_temp = required;
				        while(withdraw.first(m) != -1 && required > 0) {
				            int temp_slot = withdraw.first(m);
				            ItemStack temp = withdraw.getItem(temp_slot);
				            if(required-temp.getAmount() < 0) {
				                temp.setAmount(temp.getAmount() - required);
				                required = 0;
				            } else {
				                withdraw.setItem(temp_slot, null);
				                required -= temp.getAmount();
				            }
				        }
				        
				        if(required > 0 && required < required_temp) {
				            deposit.setItem(slot, new ItemStack(m.toMaterial(),m.getAmount()-required));
				        } else if(required == 0) {
				            deposit.setItem(slot, new ItemStack(m.toMaterial(),m.getAmount()));
				        }

						return true;
					}//Merge stacks together
					else if (m.equals(deposit.getItem(slot).getType())){
					    ItemStack current = deposit.getItem(slot);
					    if(current.getAmount() >= m.getAmount())
					        return true;
					    
					    int remaining = m.getAmount() - current.getAmount();
					    int remaining_temp = remaining;
					    while(withdraw.first(m) != -1 && remaining > 0) {
                            int temp_slot = withdraw.first(m);
                            ItemStack temp = withdraw.getItem(temp_slot);
                            if(remaining-temp.getAmount() < 0) {
                                temp.setAmount(temp.getAmount() - remaining);
                                remaining = 0;
                            } else {
                                withdraw.setItem(temp_slot, null);
                                remaining -= temp.getAmount();
                            }
                        }
					    
					    if(remaining > 0 && remaining < remaining_temp) {
                            deposit.setItem(slot, new ItemStack(m.toMaterial(),m.getAmount()-remaining));
                        } else if(remaining == 0) {
                            deposit.setItem(slot, new ItemStack(m.toMaterial(),m.getAmount()));
                        }
						return true;
					}
				}
			}
		}
		return false;
	}
}
