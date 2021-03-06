package com.ue.shopsystem.commands.adminshop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.ue.shopsystem.controller.AdminshopController;

public class AdminshopTabCompleterImpl implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
	List<String> list = new ArrayList<>();
	if (args[0].equals("")) {
	    addAllCommandsToList(list);
	} else if (args[0].equals("delete") || args[0].equals("addItem") || args[0].equals("removeItem")
		|| args[0].equals("addSpawner") || args[0].equals("removeSpawner") || args[0].equals("editShop")
		|| args[0].equals("addEnchantedItem") || args[0].equals("addPotion") || args[0].equals("rename")
		|| args[0].equals("resize") || args[0].equals("changeProfession") || args[0].equals("editItem")
		|| args[0].equals("move")) {
	    if (args.length == 2) {
		if (command.getName().equals("adminshop")) {
		    list = getAdminShopList(args[1]);
		}
	    } else if (args.length == 3) {
		if (args[0].equals("addItem") || args[0].equals("addEnchantedItem")) {
		    list = getMaterialList(args[2]);
		} else if (args[0].equals("addPotion")) {
		    handleAddPotionPotionTypeTabComplete(args, list);
		} else if (args[0].equals("addSpawner")) {
		    list = getEntityList(args[2]);
		} else if (args[0].equals("changeProfession")) {
		    handleChangeProfessionTabComplete(args, list);
		}
	    } else if (args.length == 4 && args[0].equals("addPotion")) {
		handleAddPotionEffectTabComplete(args, list);
	    } else if (args.length == 5 && args[0].equals("addPotion")) {
		handleAddPotionPropertyTabComplete(args, list);
	    } else if (args[0].equals("addEnchantedItem") && args.length >= 7 && (args.length % 2) == 0) {
		handleAddEnchantedItemEnchantementTabComplete(args, list);
	    }
	} else if (args.length == 1) {
	    addMatchingCommandsToList(args, list);
	}
	return list;
    }

    private void addMatchingCommandsToList(String[] args, List<String> list) {
	addIfMatching(list, "create", args[0]);
	addIfMatching(list, "delete", args[0]);
	addIfMatching(list, "move", args[0]);
	addIfMatching(list, "addItem", args[0]);
	addIfMatching(list, "removeItem", args[0]);
	addIfMatching(list, "rename", args[0]);	
	addIfMatching(list, "changeProfession", args[0]);
	addIfMatching(list, "resize", args[0]);
	addIfMatching(list, "editItem", args[0]);
	addIfMatching(list, "editShop", args[0]);
	addIfMatching(list, "addEnchantedItem", args[0]);
	addIfMatching(list, "addPotion", args[0]);	
	addIfMatching(list, "addSpawner", args[0]);
	addIfMatching(list, "removeSpawner", args[0]);
    }

    private void addIfMatching(List<String> list, String command, String arg) {
	if (command.contains(arg)) {
	    list.add(command);
	}
    }

    private void handleAddPotionPotionTypeTabComplete(String[] args, List<String> list) {
	if (args[2].equals("")) {
	    for (PotionType pType : PotionType.values()) {
		list.add(pType.name().toLowerCase());
	    }
	} else {
	    for (PotionType pType : PotionType.values()) {
		if (pType.name().toLowerCase().contains(args[2])) {
		    list.add(pType.name().toLowerCase());
		}
	    }
	}
    }

    private void handleAddEnchantedItemEnchantementTabComplete(String[] args, List<String> list) {
	if (args[args.length - 1].equals("")) {
	    for (Enchantment enchantment : Enchantment.values()) {
		if (enchantment != null) {
		    list.add(enchantment.getKey().getKey());
		}
	    }
	} else {
	    for (Enchantment enchantment : Enchantment.values()) {
		if (enchantment != null && enchantment.getKey().getKey().contains(args[args.length - 1])) {
		    list.add(enchantment.getKey().getKey().toLowerCase());
		}
	    }
	}
    }

    private void handleAddPotionPropertyTabComplete(String[] args, List<String> list) {
	if (args[4].equals("")) {
	    list.add("extended");
	    list.add("upgraded");
	    list.add("none");
	} else {
	    addIfMatching(list, "extended", args[4]);
	    addIfMatching(list, "upgraded", args[4]);
	    addIfMatching(list, "none", args[4]);
	}
    }

    private void handleAddPotionEffectTabComplete(String[] args, List<String> list) {
	if (args[3].equals("")) {
	    for (PotionEffectType peType : PotionEffectType.values()) {
		if (peType != null) {
		    list.add(peType.getName().toLowerCase());
		}
	    }
	} else {
	    for (PotionEffectType peType : PotionEffectType.values()) {
		if (peType != null && peType.getName().toLowerCase().contains(args[3])) {
		    list.add(peType.getName().toLowerCase());
		}
	    }
	}
    }

    private void handleChangeProfessionTabComplete(String[] args, List<String> list) {
	if (args[2].equals("")) {
	    for (Profession profession : Profession.values()) {
		list.add(profession.name().toLowerCase());
	    }
	} else {
	    for (Profession profession : Profession.values()) {
		if (profession.name().toLowerCase().contains(args[2])) {
		    list.add(profession.name().toLowerCase());
		}
	    }
	}
    }

    private void addAllCommandsToList(List<String> list) {
	list.add("create");
	list.add("delete");
	list.add("move");
	list.add("editShop");
	list.add("addItem");
	list.add("removeItem");
	list.add("rename");
	list.add("resize");
	list.add("changeProfession");
	list.add("editItem");
	list.add("addEnchantedItem");
	list.add("addPotion");
	list.add("addSpawner");
	list.add("removeSpawner");
    }

    private List<String> getAdminShopList(String arg) {
	List<String> temp = AdminshopController.getAdminshopNameList();
	List<String> list = new ArrayList<>();
	if ("".equals(arg)) {
	    list = temp;
	} else {
	    for (String shopName : temp) {
		addIfMatching(list, shopName, arg);
	    }
	}
	return list;
    }

    private static List<String> getMaterialList(String arg) {
	Material[] materials = Material.values();
	List<String> list = new ArrayList<>();
	if ("".equals(arg)) {
	    for (Material material : materials) {
		list.add(material.name().toLowerCase());
	    }
	} else {
	    for (Material material : materials) {
		if (material.name().toLowerCase().contains(arg)) {
		    list.add(material.name().toLowerCase());
		}
	    }
	}
	return list;
    }

    private static List<String> getEntityList(String arg) {
	List<String> list = new ArrayList<>();
	EntityType[] entityTypes = EntityType.values();
	if ("".equals(arg)) {
	    for (EntityType entityname : entityTypes) {
		list.add(entityname.name().toLowerCase());
	    }
	} else {
	    for (EntityType entityname : entityTypes) {
		if (entityname.name().toLowerCase().contains(arg)) {
		    list.add(entityname.name().toLowerCase());
		}
	    }
	}
	return list;
    }
}
