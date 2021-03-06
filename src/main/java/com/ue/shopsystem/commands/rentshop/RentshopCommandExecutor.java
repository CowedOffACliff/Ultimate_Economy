package com.ue.shopsystem.commands.rentshop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ue.exceptions.GeneralEconomyException;
import com.ue.exceptions.PlayerException;
import com.ue.exceptions.ShopSystemException;
import com.ue.exceptions.TownSystemException;
import com.ue.language.MessageWrapper;

public class RentshopCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (sender instanceof Player) {
	    Player player = (Player) sender;
	    try {
		if (args.length != 0) {
		    RentshopCommandEnum commandEnum = RentshopCommandEnum.getEnum(args[0]);
		    if (commandEnum != null) {
			return commandEnum.perform(label, args, player);
		    }
		}
		if (player.hasPermission("ultimate_economy.rentshop.admin")) {
		    player.sendMessage("/" + label + " [create/delete/move/resize/editShop]");
		} else {
		    player.sendMessage("/" + label + " [editShop]");
		}
	    } catch (NumberFormatException e) {
		player.sendMessage(MessageWrapper.getErrorString("invalid_parameter", ""));
	    } catch (ShopSystemException | PlayerException | GeneralEconomyException e) {
		player.sendMessage(e.getMessage());
	    } catch (TownSystemException e) {
	    }
	}
	return true;
    }
}
