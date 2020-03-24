package com.ue.shopsystem.impl;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.ue.exceptions.GeneralEconomyException;
import com.ue.exceptions.ShopSystemException;
import com.ue.language.MessageWrapper;
import com.ue.shopsystem.api.Rentshop;
import com.ue.shopsystem.controller.RentshopController;

public class RentDailyTask extends BukkitRunnable {

    @Override
    public void run() {
	for (Rentshop shop : RentshopController.getRentShops()) {
	    if (!shop.isRentable()) {
		if (Calendar.getInstance().getTimeInMillis() >= shop.getRentUntil()) {
		    try {
			shop.resetShop();
		    } catch (ShopSystemException | GeneralEconomyException e) {
			Bukkit.getLogger().warning("[Ultimate_Economy] Error on rent task");
			Bukkit.getLogger().warning("[Ultimate_Economy] Caused by: " + e.getMessage());
		    }
		} else if ((shop.getRentUntil() - Calendar.getInstance().getTimeInMillis()) < 600000) {
		    if (shop.getOwner().isOnline()) {
			shop.getOwner().getPlayer().sendMessage(MessageWrapper.getString("rent_reminder"));
		    }
		}
	    }
	}
    }
}
