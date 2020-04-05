package com.ue.shopsystem.impl;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ue.exceptions.GeneralEconomyException;
import com.ue.exceptions.GeneralEconomyExceptionMessageEnum;
import com.ue.exceptions.PlayerException;
import com.ue.exceptions.PlayerExceptionMessageEnum;
import com.ue.exceptions.ShopExceptionMessageEnum;
import com.ue.exceptions.ShopSystemException;
import com.ue.exceptions.TownExceptionMessageEnum;
import com.ue.exceptions.TownSystemException;
import com.ue.player.api.EconomyPlayer;
import com.ue.shopsystem.api.PlayershopController;
import com.ue.townsystem.town.api.Town;
import com.ue.townsystem.townworld.api.Townworld;
import com.ue.townsystem.townworld.api.TownworldController;

public class ShopValidationHandler {

    /**
     * Check for one price greater then zero if both prices are available.
     * 
     * @param sellPrice
     * @param buyPrice
     * @throws ShopSystemException
     */
    public void checkForOnePriceGreaterThenZeroIfBothAvailable(String sellPrice, String buyPrice)
	    throws ShopSystemException {
	if (!"none".equals(sellPrice) && !"none".equals(buyPrice) && Double.valueOf(sellPrice) == 0
		&& Double.valueOf(buyPrice) == 0) {
	    throw ShopSystemException.getException(ShopExceptionMessageEnum.INVALID_PRICES);
	}
    }

    /**
     * Check for at least one price greater then zero.
     * 
     * @param sellPrice
     * @param buyPrice
     * @throws ShopSystemException
     */
    public void checkForPricesGreaterThenZero(double sellPrice, double buyPrice) throws ShopSystemException {
	if (buyPrice == 0 && sellPrice == 0) {
	    throw ShopSystemException.getException(ShopExceptionMessageEnum.INVALID_PRICES);
	}
    }

    /**
     * Check for slot is not empty.
     * 
     * @param slot
     * @param inventory
     * @param reservedSlots
     * @throws GeneralEconomyException
     * @throws ShopSystemException
     */
    public void checkForSlotIsNotEmpty(int slot, Inventory inventory, int reservedSlots)
	    throws GeneralEconomyException, ShopSystemException {
	if (isSlotEmpty(slot, inventory, reservedSlots)) {
	    throw ShopSystemException.getException(ShopExceptionMessageEnum.INVENTORY_SLOT_EMPTY);
	}
    }

    /**
     * Check for slot is empty.
     * 
     * @param slot
     * @param inventory
     * @param reservedSlots
     * @throws PlayerException
     * @throws GeneralEconomyException
     * @throws ShopSystemException
     */
    public void checkForSlotIsEmpty(int slot, Inventory inventory, int reservedSlots)
	    throws GeneralEconomyException, PlayerException {
	if (!isSlotEmpty(slot, inventory, reservedSlots)) {
	    throw PlayerException.getException(PlayerExceptionMessageEnum.INVENTORY_SLOT_OCCUPIED);
	}
    }

    /**
     * Returns, if the slot is empty or not.
     * 
     * @param slot
     * @param inventory
     * @param reservedSlots
     * @return true, if the slot is empty, false, if the slot is not empty
     * @throws GeneralEconomyException
     * @throws ShopSystemException
     */
    public boolean isSlotEmpty(int slot, Inventory inventory, int reservedSlots) throws GeneralEconomyException {
	checkForValidSlot(slot, inventory.getSize(), reservedSlots);
	boolean isEmpty = false;
	if (inventory.getItem(slot) == null || inventory.getItem(slot).getType() == Material.AIR) {
	    isEmpty = true;
	}
	return isEmpty;
    }

    /**
     * Check for valid amount.
     * 
     * @param amount
     * @throws GeneralEconomyException
     */
    public void checkForValidAmount(String amount) throws GeneralEconomyException {
	if (!"none".equals(amount) && (Integer.valueOf(amount) <= 0 || Integer.valueOf(amount) > 64)) {
	    throw GeneralEconomyException.getException(GeneralEconomyExceptionMessageEnum.INVALID_PARAMETER, amount);
	}
    }

    /**
     * Check for valid price.
     * 
     * @param price
     * @throws GeneralEconomyException
     */
    public void checkForValidPrice(String price) throws GeneralEconomyException {
	if (!"none".equals(price) && Double.valueOf(price) < 0) {
	    throw GeneralEconomyException.getException(GeneralEconomyExceptionMessageEnum.INVALID_PARAMETER, price);
	}
    }

    /**
     * Check for item exists in the shop.
     * 
     * @param itemName
     * @param itemList
     * @throws ShopSystemException
     */
    public void checkForItemExists(String itemName, List<String> itemList) throws ShopSystemException {
	if (!itemList.contains(itemName)) {
	    throw ShopSystemException.getException(ShopExceptionMessageEnum.ITEM_DOES_NOT_EXIST);
	}
    }

    /**
     * Check for valid size.
     * 
     * @param size
     * @throws GeneralEconomyException
     */
    public void checkForValidSize(int size) throws GeneralEconomyException {
	if (size % 9 != 0) {
	    throw GeneralEconomyException.getException(GeneralEconomyExceptionMessageEnum.INVALID_PARAMETER, size);
	}
    }

    /**
     * Check for valid slot.
     * 
     * @param slot
     * @param size
     * @param reservedSlots
     * @throws GeneralEconomyException
     */
    public void checkForValidSlot(int slot, int size, int reservedSlots) throws GeneralEconomyException {
	if (slot > (size - 1) || slot < 0) {
	    // +1 for player readable style
	    throw GeneralEconomyException.getException(GeneralEconomyExceptionMessageEnum.INVALID_PARAMETER, slot + 1);
	}
    }

    /**
     * Check for resize possible.
     * 
     * @param inventory
     * @param oldSize
     * @param newSize
     * @param reservedSlots
     * @throws ShopSystemException
     * @throws GeneralEconomyException
     */
    public void checkForResizePossible(Inventory inventory, int oldSize, int newSize, int reservedSlots)
	    throws ShopSystemException, GeneralEconomyException {
	int diff = oldSize - newSize;
	if (oldSize > newSize) {
	    for (int i = 1; i <= diff; i++) {
		ItemStack stack = inventory.getItem(oldSize - i - reservedSlots);
		if (stack != null && stack.getType() != Material.AIR) {
		    throw ShopSystemException.getException(ShopExceptionMessageEnum.RESIZING_FAILED);
		}
	    }
	}
    }

    /**
     * Check for world exists.
     * 
     * @param world
     * @throws TownSystemException
     */
    public void checkForWorldExists(World world) throws TownSystemException {
	if (world == null) {
	    throw TownSystemException.getException(TownExceptionMessageEnum.WORLD_DOES_NOT_EXIST, "<unknown>");
	}
    }

    /**
     * Check for item does not exist in shop.
     * 
     * @param itemString
     * @param itemList
     * @throws ShopSystemException
     */
    public void checkForItemDoesNotExist(String itemString, List<String> itemList) throws ShopSystemException {
	if (itemList.contains(itemString)) {
	    throw ShopSystemException.getException(ShopExceptionMessageEnum.ITEM_ALREADY_EXISTS);
	}
    }

    /**
     * Check for item can be deleted.
     * 
     * @param slot
     * @param size
     * @throws ShopSystemException
     */
    public void checkForItemCanBeDeleted(int slot, int size) throws ShopSystemException {
	if ((slot + 1) == size) {
	    throw ShopSystemException.getException(ShopExceptionMessageEnum.ITEM_CANNOT_BE_DELETED);
	}
    }

    /**
     * Check for positive value.
     * 
     * @param value
     * @throws GeneralEconomyException
     */
    public void checkForPositiveValue(double value) throws GeneralEconomyException {
	if (value < 0) {
	    throw GeneralEconomyException.getException(GeneralEconomyExceptionMessageEnum.INVALID_PARAMETER, value);
	}
    }

    /**
     * Check for valid stock decrease.
     * 
     * @param entireStock
     * @param stock
     * @throws GeneralEconomyException
     */
    public void checkForValidStockDecrease(int entireStock, int stock) throws GeneralEconomyException {
	if ((entireStock - stock) < 0) {
	    throw GeneralEconomyException.getException(GeneralEconomyExceptionMessageEnum.INVALID_PARAMETER, stock);
	}
    }

    /**
     * Check for change owner is possible.
     * 
     * @param newOwner
     * @param shopName
     * @throws ShopSystemException
     */
    public void checkForChangeOwnerIsPossible(EconomyPlayer newOwner, String shopName) throws ShopSystemException {
	if (PlayershopController.getPlayerShopUniqueNameList().contains(shopName + "_" + newOwner.getName())) {
	    throw ShopSystemException.getException(ShopExceptionMessageEnum.SHOP_CHANGEOWNER_ERROR);
	}
    }

    /**
     * Check for valid shop name.
     * 
     * @param name
     * @throws ShopSystemException
     */
    public void checkForValidShopName(String name) throws ShopSystemException {
	if (name.contains("_")) {
	    throw ShopSystemException.getException(ShopExceptionMessageEnum.INVALID_CHAR_IN_SHOP_NAME);
	}
    }

    /**
     * Check for shop name is free.
     * 
     * @param name
     * @param owner
     * @throws GeneralEconomyException
     */
    public void checkForShopNameIsFree(String name, EconomyPlayer owner) throws GeneralEconomyException {
	if (PlayershopController.getPlayerShopUniqueNameList().contains(name + "_" + owner.getName())
		|| PlayershopController.getPlayerShopUniqueNameList().contains(name)) {
	    throw GeneralEconomyException.getException(GeneralEconomyExceptionMessageEnum.ALREADY_EXISTS,
		    name + owner.getName());
	}
    }

    /**
     * Check for player has permission at location.
     * 
     * @param location
     * @param owner
     * @throws PlayerException
     * @throws TownSystemException
     */
    public void checkForPlayerHasPermissionAtLocation(Location location, EconomyPlayer owner)
	    throws PlayerException, TownSystemException {
	Townworld townworld = TownworldController.getTownWorldByName(location.getWorld().getName());
	if (townworld.isChunkFree(location.getChunk())) {
	    throw PlayerException.getException(PlayerExceptionMessageEnum.NO_PERMISSION);
	} else {
	    Town town = townworld.getTownByChunk(location.getChunk());
	    if (!town.hasBuildPermissions(owner,
		    town.getPlotByChunk(location.getChunk().getX() + "/" + location.getChunk().getZ()))) {
		throw PlayerException.getException(PlayerExceptionMessageEnum.NO_PERMISSION);
	    }
	}
    }

    /**
     * Check for is rentable.
     * 
     * @param isRentable
     * @throws ShopSystemException
     */
    public void checkForIsRentable(boolean isRentable) throws ShopSystemException {
	if (!isRentable) {
	    throw ShopSystemException.getException(ShopExceptionMessageEnum.ALREADY_RENTED);
	}
    }
}