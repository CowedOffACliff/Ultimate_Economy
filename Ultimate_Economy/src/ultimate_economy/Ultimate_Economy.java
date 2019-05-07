package ultimate_economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.ue.exceptions.JobSystemException;
import com.ue.exceptions.PlayerException;
import com.ue.exceptions.TownSystemException;
import com.ue.exceptions.banksystem.TownHasNotEnoughMoneyException;
import com.ue.jobsystem.Job;
import com.ue.jobsystem.JobCenter;
import com.ue.player.EconomyPlayer;
import com.ue.townsystem.Plot;
import com.ue.townsystem.Town;
import com.ue.townsystem.TownWorld;

import shop.AdminShop;
import shop.PlayerShop;
import shop.Shop;
import shop.Spawner;

public class Ultimate_Economy extends JavaPlugin implements Listener{
	
	/*
	 * Lukas Heubach
	 * permission tp to town
	 * buyable regions | add coOwner<-
	 * leaderboard
	 * double to String method
	 * town bank amount in scoreboard f�r town owner und town coowner
	 * job crafter,enchanter
	 * op should remove playershops (more op commands)
	 * limit playershops per player
	 * check inventory name for right cancel 
	 * 
	 */

	private Player player = null;
	private List<String> playershopNames,adminShopNames,playerlist,spawnerlist,townNames;
	private List<AdminShop> adminShopList;
	private List<PlayerShop> playerShopList;
	private File spawner;
	private FileConfiguration config;

	public void onEnable() {	
		Bukkit.getPluginManager().registerEvents(this,this);
		adminShopNames = new ArrayList<>();    
		adminShopList = new ArrayList<>();
		playerlist = new ArrayList<>();
		playerShopList = new ArrayList<>();
		playershopNames = new ArrayList<>();
		townNames = new ArrayList<>();
		spawnerlist = new ArrayList<>();

		if(!getDataFolder().exists()) {
			getDataFolder().mkdirs();
			//TODO vllt �erfl�ssig
			getConfig().set("MaxHomes", 3);
			getConfig().set("MaxJobs", 2);
			getConfig().set("MaxJoinedTowns", 1);
			//
			saveConfig();
		}
		//can be removed in a future update
		else {
			getConfig().set("ItemList", null);
			saveConfig();
		}
		
		try {
			JobCenter.loadAllJobCenters(getServer(), getConfig(), getDataFolder());
			Job.loadAllJobs(getDataFolder(), getConfig());
			TownWorld.loadAllTownWorlds(getDataFolder(), getConfig());
			EconomyPlayer.loadAllEconomyPlayers(getDataFolder());
			EconomyPlayer.setupConfig(getConfig());
		} catch (JobSystemException | TownSystemException e) {
			Bukkit.getLogger().log(Level.WARNING, e.getMessage(), e);
		}
		
		adminShopNames.addAll(getConfig().getStringList("ShopNames"));
		for(String shopname1:adminShopNames) {
			adminShopList.add(new AdminShop(this,shopname1,player,"9"));
			}
		playershopNames.addAll(getConfig().getStringList("PlayerShopNames"));
		for(String shopname1:playershopNames) {
			playerShopList.add(new PlayerShop(this,shopname1,player,"9"));
			}		
		
		townNames.addAll(getConfig().getStringList("TownNames"));

		config = YamlConfiguration.loadConfiguration(EconomyPlayer.getPlayerFile());
		spawner = new File(getDataFolder() , "SpawnerLocations.yml");
		if(!spawner.exists()) {
			try {
				spawner.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileConfiguration spawnerconfig = YamlConfiguration.loadConfiguration(spawner);
		for(String spawnername:getConfig().getStringList("Spawnerlist")) {
			spawnerlist.add(spawnername);
			World world = getServer().getWorld(spawnerconfig.getString(spawnername + ".World"));
			Location location = new Location(world, spawnerconfig.getDouble(spawnername + ".X"), spawnerconfig.getDouble(spawnername + ".Y"), spawnerconfig.getDouble(spawnername + ".Z"));			world.getBlockAt(location).setMetadata("name", new FixedMetadataValue(this, spawnerconfig.getString(spawnername + ".player")));
			world.getBlockAt(location).setMetadata("entity", new FixedMetadataValue(this, spawnerconfig.getString(spawnername + ".EntityType")));
		}
		playerlist = config.getStringList("Player");
		getConfig().options().copyDefaults(true);
		saveConfig();	
	}
	public void onDisable() {
		for(AdminShop s:adminShopList) {
			s.despawnVillager();
		}
		for(PlayerShop s:playerShopList) {
			s.despawnVillager();
		}
		
		JobCenter.despawnAllVillagers();
		TownWorld.despawnAllVillagers();

		saveConfig();
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if(command.getName().equals("jobcenter")) {
			if(args.length == 1) {
				if(args[0].equals("")) {
					list.add("create");
					list.add("delete");
					list.add("move");
					list.add("job");
					list.add("addJob");
					list.add("removeJob");
				}
				else if(args.length == 1){
					if("create".contains(args[0])) {
						list.add("create");
					}
					if("delete".contains(args[0])) {
						list.add("delete");
					}
					if("move".contains(args[0])) {
						list.add("move");
					}
					if("job".contains(args[0])) {
						list.add("job");
					}
					if("addJob".contains(args[0])) {
						list.add("addJob");
					}
				}
			}
			else if(args[0].equals("delete") || args[0].equals("move") || args[0].equals("removeJob") || args[0].equals("addJob")) {
				if(args.length == 2) {
					List<String> temp = getConfig().getStringList("JobCenterNames");
					if(args[1].equals("")) {
						list = temp;
					}
					else {
						for(String jobname:temp) {
							if(jobname.contains(args[1])) {
								list.add(jobname);								
							}
						}
					}
				}
				else if(args[0].equals("removeJob") || args[0].equals("addJob")) {
					if(args.length == 3) {
						list = getJobList(args[2]);
					}
					else if(args.length == 4 && args[0].equals("addJob")) {
						list = getMaterialList(args[3]);
					}
				}	
			}
			else if(args[0].equals("job")) {
				if(args[1].equals("")) {
					list.add("createJob");
					list.add("delJob");
					list.add("addItem");
					list.add("removeItem");
					list.add("addFisher");
					list.add("removeFisher");
					list.add("addMob");
					list.add("removeMob");
				}
				else if(args[1].equals("addItem") || args[1].equals("removeItem") || args[1].equals("addFisher") || args[1].equals("removeFisher") || args[1].equals("removeFisher") || args[1].equals("addMob") || args[1].equals("removeMob")) {
					if(args.length == 3) {
						list = getJobList(args[2]);
					}
					else if(args[1].equals("addItem") || args[1].equals("removeItem") || args[1].equals("addItem")){
						if(args.length == 4) {
							list = getMaterialList(args[3]);
						}
					}
					else if(args[1].equals("addMob") || args[1].equals("removeMob")) {
						if(args.length == 4) {
							list = getEntityList(args[3]);
						}
					}
					else if(args[1].equals("addFisher") || args[1].equals("removeFisher")) {
						if(args.length == 4) {
							if(args[3].equals("")) {
								list.add("fish");
								list.add("treasure");
								list.add("junk");
							}
							else {
								if("fish".contains(args[3])) {
									list.add("fish");
								}
								if("treasure".contains(args[3])) {
									list.add("treasure");
								}
								if("junk".contains(args[3])) {
									list.add("junk");
								}
							}
						}
					}
				}
				else if(args[1].equals("delJob")) {
					if(args.length == 3) {
						list = getJobList(args[2]);
					}
				}
				else if (args.length == 2) {
					if("createJob".contains(args[1])) {
						list.add("createJob");
					}
					if("delJob".contains(args[1])) {
						list.add("delJob");
					}
					if("addItem".contains(args[1])) {
						list.add("addItem");
					}
					if("removeItem".contains(args[1])) {
						list.add("removeItem");
					}
					if("addFisher".contains(args[1])) {
						list.add("addFisher");
					}
					if("delFisher".contains(args[1])) {
						list.add("delFisher");
					}
					if("addMob".contains(args[1])) {
						list.add("addMob");
					}
					if("removeMob".contains(args[1])) {
						list.add("removeMob");
					}
				}
			}
		}
		else if(command.getName().equals("adminshop") || command.getName().equals("playershop")) {
			if(args[0].equals("")) {
				list.add("create");
				list.add("delete");
				list.add("move");
				list.add("editShop");
				list.add("addItem");
				list.add("removeItem");
				list.add("editItem");
				list.add("addEnchantedItem");
				list.add("addPotion");
				if(command.getName().equals("adminshop")) {
					list.add("addSpawner");
					list.add("removeSpawner");
				}
				else {
					list.add("changeOwner");
				}
			}
			else if(args[0].equals("delete") || args[0].equals("addItem") || args[0].equals("removeItem") || args[0].equals("addSpawner")  || args[0].equals("removeSpawner")  || args[0].equals("editShop") || args[0].equals("addEnchantedItem") || args[0].equals("addPotion") || args[0].equals("changeOwner")) {
				if(args.length == 2) {
					if(command.getName().equals("adminshop")) {
						list = getAdminShopList(args[1]);
					}
					else {
						list = getPlayerShopList(args[1],sender.getName());
					}
				}
				else if(args.length == 3) {
					if(args[0].equals("addItem") || args[0].equals("addEnchantedItem")) {
						list = getMaterialList(args[2]);
					}
					else if(args[0].equals("addPotion")) {
						if(args[2].equals("")) {
							for(PotionType pType: PotionType.values()) {
								list.add(pType.name().toLowerCase());
							}
						}
						else  {
							for(PotionType pType: PotionType.values()) {
								if(pType.name().toLowerCase().contains(args[2])) {
									list.add(pType.name().toLowerCase());
								}
							}
						}
					}
					else if(args[0].equals("addSpawner")) {
						list = getEntityList(args[2]);
					}
				}
				else if(args.length == 4 && args[0].equals("addPotion")) {
					if(args[3].equals("")) {
						for(PotionEffectType peType: PotionEffectType.values()) {
							if(peType != null) {
								list.add(peType.getName().toLowerCase());
							}
						}
					}
					else  {
						for(PotionEffectType peType: PotionEffectType.values()) {
							if(peType != null && peType.getName().toLowerCase().contains(args[3])) {
								list.add(peType.getName().toLowerCase());
							}
						}
					}
				}
				else if(args.length == 5 && args[0].equals("addPotion")) {
					if(args[4].equals("")) {
						list.add("extended");
						list.add("upgraded");
						list.add("none");
					}
					else  {
						if("extended".contains(args[4])) {
							list.add("extended");
						}
						if("upgraded".contains(args[4])) {
							list.add("upgraded");
						}
						if("none".contains(args[4])) {
							list.add("none");
						}
					}
				}
				else if(args[0].equals("addEnchantedItem") && args.length >= 7 && (args.length % 2) == 0) {
					if(args[args.length-1].equals("")) {
						for(Enchantment enchantment: Enchantment.values()) {
							if(enchantment != null) {
								list.add(enchantment.getKey().getKey());
							}
						}
					}
					else  {
						for(Enchantment enchantment: Enchantment.values()) {
							if(enchantment != null && enchantment.getKey().getKey().contains(args[args.length-1])) {
								list.add(enchantment.getKey().getKey().toLowerCase());
							}
						}
					}
				}
			}
			else if(args.length == 1){
				if("create".contains(args[0])) {
					list.add("create");
				}
				if("delete".contains(args[0])) {
					list.add("delete");
				}
				if("move".contains(args[0])) {
					list.add("move");
				}
				if("addItem".contains(args[0])) {
					list.add("addItem");
				}
				if("removeItem".contains(args[0])) {
					list.add("removeItem");
				}
				if("editItem".contains(args[0])) {
					list.add("editItem");
				}
				if("editShop".contains(args[0])) {
					list.add("editShop");
				}
				if("addEnchantedItem".contains(args[0])) {
					list.add("addEnchantedItem");
				}
				if("addPotion".contains(args[0])) {
					list.add("addPotion");
				}
				if(command.getName().equals("adminshop")) {
					if("addSpawner".contains(args[0])) {
						list.add("addSpawner");
					}
					if("removeSpawner".contains(args[0])) {
						list.add("removeSpawner");
					}
				}
				else {
					if("changeOwner".contains(args[0])) {
						list.add("changeOwner");
					}
				}
			}
		}
		else if(command.getName().equals("bank")) {
			if(args[0].equals("")) {
				list.add("on");
				list.add("off");
			}
			else if(args.length == 1){
				if("on".contains(args[0])) {
					list.add("on");
				}
				if("off".contains(args[0])) {
					list.add("off");
				}
			}
		}
		else if(command.getName().equals("shop")) {
			if(args.length <= 1) {
				list = getAdminShopList(args[0]);
			}
		}
		else if(command.getName().equals("jobInfo")) {
			if(args.length <= 1) {
				list = getJobList(args[0]);
			}
		}
		else if(command.getName().equals("delHome") || command.getName().equals("home")) {
			if(args.length <= 1) {
				list = getHomeList(args[0],sender.getName());
			}
		}
		else if(command.getName().equalsIgnoreCase("townWorld")) {
			if(args[0].equals("")) {
				list.add("enable");
				list.add("disable");
				list.add("setFoundationPrice");
				list.add("setExpandPrice");
			}
			else if(args.length == 1){
				if("enable".contains(args[0])) {
					list.add("enable");
				}
				if("disable".contains(args[0])) {
					list.add("disable");
				}
				if("setFoundationPrice".contains(args[0])) {
					list.add("setFoundationPrice");
				}
				if("setExpandPrice".contains(args[0])) {
					list.add("setExpandPrice");
				}
			}
			else if(args[0].equals("enable") || args[0].equals("disable") || args[0].equals("setFoundationPrice") || args[0].equals("setExpandPrice")) {
				if(args[1].equals("")) {
					for(World world:Bukkit.getWorlds()) {
						list.add(world.getName());
					}
				}
				else if(args.length == 2){
					for(World world:Bukkit.getWorlds()) {
						if(world.getName().contains(args[1])) {
							list.add(world.getName());
						}
					}
				}
			}
		}
		else if(command.getName().equals("town")) {
			if(args[0].equals("")) {
				list.add("create");
				list.add("delete");
				list.add("expand");
				list.add("setTownSpawn");
				list.add("moveTownManager");
				list.add("plot");
				list.add("pay");
				list.add("tp");
				list.add("bank");
			}
			else if(args.length == 1){
				if("create".contains(args[0])) {
					list.add("create");
				}
				if("delete".contains(args[0])) {
					list.add("delete");
				}
				if("expand".contains(args[0])) {
					list.add("expand");
				}
				if("setTownSpawn".contains(args[0])) {
					list.add("setTownSpawn");
				}
				if("moveTownManager".contains(args[0])) {
					list.add("moveTownManager");
				}
				if("plot".contains(args[0])) {
					list.add("plot");
				}
				if("pay".contains(args[0])) {
					list.add("pay");
				}
				if("tp".contains(args[0])) {
					list.add("tp");
				}
				if("bank".contains(args[0])) {
					list.add("bank");
				}
			}
			else if(args[0].equals("delete") || args[0].equals("expand") || args[0].equals("setTownSpawn") || args[0].equals("bank")) {
				if(args[1].equals("")) {
					config = YamlConfiguration.loadConfiguration(playerFile);
					list.addAll(config.getStringList(sender.getName() + ".joinedTowns"));
				}
				else if(args.length == 2){
					config = YamlConfiguration.loadConfiguration(playerFile);
					List<String> list2 = config.getStringList(sender.getName() + ".joinedTowns");
					for(String string:list2) {
						if(string.contains(args[1])) {
							list.add(string);
						}
					}
				}
			}
			else if(args[0].equals("plot")) {
				if(args[1].equals("")) {
					list.add("setForSale");
				}
				else if(args.length == 2){
					if("setForSale".contains(args[1])) {
						list.add("setForSale");
					}
				}
			}
		}
		return list;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			player = (Player) sender;
			EconomyPlayer ecoPlayer = EconomyPlayer.getEconomyPlayerByName(player.getName());
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//Commands
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////single commands
			if(label.equalsIgnoreCase("bank")) {
				if(args.length == 1) {
					if(args[0].equals("on") || args[0].equals("off")) {
						if(args[0].equals("on")) {
							ecoPlayer.setScoreBoardDisabled(false);
						}
						else {
							ecoPlayer.setScoreBoardDisabled(true);
						}
						ecoPlayer.updateScoreBoard(player);
					}
					else {
						player.sendMessage(ChatColor.RED + "/bank on/off");
					}
				}
				else {
					player.sendMessage("/bank <on/off>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("money")) {
				if(args.length == 0) {
					config = YamlConfiguration.loadConfiguration(EconomyPlayer.getPlayerFile());
					player.sendMessage(ChatColor.GOLD + "Money: " + ChatColor.GREEN + String.valueOf(config.getDouble(player.getName() + ".account amount")));
				}
				else {
					player.sendMessage("/money");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("maxHomes")) {
				if(args.length == 1) {
					EconomyPlayer.setMaxHomes(Integer.valueOf(args[0]));
				}
				else {
					player.sendMessage("/maxHomes <number>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("maxJobs")) {
				if(args.length == 1) {
					EconomyPlayer.setMaxJobs(Integer.valueOf(args[0]));
				}
				else {
					player.sendMessage("/maxJobs <number>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("maxJoinedTowns")) {
				if(args.length == 1) {
					EconomyPlayer.setMaxJoinedTowns(Integer.valueOf(args[0]));
				}
				else {
					player.sendMessage("/maxJoinedTowns <number>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("townWorld")) {
				if(args.length > 1) {
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					if(args[0].equals("enable")) {
						if(args.length == 2) {
							try {
								TownWorld.createTownWorld(getDataFolder(), args[1]);
								player.sendMessage(ChatColor.GREEN + args[1] + ChatColor.GOLD + " is now a TownWold.");
							} catch (TownSystemException e) {
								player.sendMessage(ChatColor.RED + e.getMessage());
							}
							
						}
						else {
							player.sendMessage("/townWorld enable <worldname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("disable")) {
						if(args.length == 2) {
							try {
								TownWorld.deleteTownWorld(args[1]);
								player.sendMessage(ChatColor.GREEN + args[1] + ChatColor.GOLD + " is no longer a TownWold.");
							} catch (TownSystemException e) {
								player.sendMessage(ChatColor.RED + e.getMessage());
							}
						}
						else {
							player.sendMessage("/townWorld disable <worldname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("setFoundationPrice")) {
						if(args.length == 3) {
							try {
								TownWorld.getTownWorldByName(args[1]).setFoundationPrice(Double.valueOf(args[2]),true);
								player.sendMessage(ChatColor.GOLD + "FoundationPrice changed to " + ChatColor.GREEN + args[2]);
							} catch (NumberFormatException e) {
								Bukkit.getLogger().log(Level.WARNING, e.getMessage(), e);
							} catch (TownSystemException e) {
								player.sendMessage(ChatColor.RED + e.getMessage());
							}
						}
						else {
							player.sendMessage("/townWorld setFoundationPrice <worldname> <price>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("setExpandPrice")) {
						if(args.length == 3) {
							try {
								TownWorld.getTownWorldByName(args[1]).setExpandPrice(Double.valueOf(args[2]),true);
								player.sendMessage(ChatColor.GOLD + "ExpandPrice changed to " + ChatColor.GREEN + args[2]);
							} catch (NumberFormatException e) {
								Bukkit.getLogger().log(Level.WARNING, e.getMessage(), e);
							} catch (TownSystemException e) {
								player.sendMessage(ChatColor.RED + e.getMessage());
							}
						}
						else {
							player.sendMessage("/townWorld setExpandPrice <worldname> <price per chunk");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					getConfig().set("TownWorlds", TownWorld.getTownWorldList());
					saveConfig();

				}
				else if(args.length == 1 && args[0].equals("enable")){
					player.sendMessage("/townWorld enable <worldname>");
				}
				else if(args.length == 1 && args[0].equals("disable")){
					player.sendMessage("/townWorld disable <worldname>");
				}
				else if(args.length == 1 && args[0].equals("setFoundationPrice")){
					player.sendMessage("/townWorld setFoundationPrice <price>");
				}
				else if(args.length == 1 && args[0].equals("setExpandPrice")){
					player.sendMessage("/townWorld setExpandPrice <price per chunk>");
				}
				else {
					player.sendMessage("/townWorld <enable/disable/setFoundationPrice/setExpandPrice>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("myJobs")) {
				if(args.length == 0) {
					List<String> jobNames = ecoPlayer.getJobList();
					String jobString = jobNames.toString();
					jobString = jobString.replace("[", "");
					jobString = jobString.replace("]", "");
					if(jobNames.size() > 0) {
						player.sendMessage(ChatColor.GOLD + "Joined jobs: " + ChatColor.GREEN + jobString);
					}
					else {
						player.sendMessage(ChatColor.GOLD + "No Jobs");
					}
				}
				else {
					player.sendMessage("/myJobs");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("shop")) {
				if(args.length == 1) {
					if(ecoPlayer.hasJob(args[0])) {
						for(AdminShop shop5: adminShopList) {
							if(shop5.getName().equals(args[0])) {
								shop5.openInv(player); 
								break;
							}
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You not joined this job!");
					}
				}
				else {
					player.sendMessage("/shop <shopname>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("ShopList")) {
				String namelist = null;
				for(String name: adminShopNames) {
					if(namelist == null) {
						namelist = name;
					}
					else {
						namelist = namelist + "," + name;
					}
				}
				if(namelist != null) {
					player.sendMessage(ChatColor.GOLD + namelist);
				}
				else {
					player.sendMessage(ChatColor.RED + "No shops exist!");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("giveMoney")) {
				if(player.isOp()) {
					if(args.length == 2) {
						double amount = Double.valueOf(args[1]);
						EconomyPlayer receiver = EconomyPlayer.getEconomyPlayerByName(args[0]);
						if(amount < 0) {
							receiver.decreasePlayerAmount(-amount);
						}
						else {
							receiver.increasePlayerAmount(amount);
						}
						Player p = Bukkit.getPlayer(args[0]);
						if(p.isOnline()) {
							p.sendMessage(ChatColor.GOLD + "You got " + ChatColor.GREEN + amount + " $");
						}
					}
					else {
						player.sendMessage("/giveMoney <player> <amount>");
					}
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("home")) {
				String pname = player.getName();
				if(args.length == 1) {
					Location location = ecoPlayer.getHome(args[0]);
					player.teleport(location);
				}
				else {
					player.sendMessage("/home <homename>");
					Set<String> homes = ecoPlayer.getHomeList().keySet();
					String homeString = String.join(",", homes);
					player.sendMessage(ChatColor.GOLD + "Your homes: " + ChatColor.GREEN + homeString);
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("sethome")) {
				if(args.length == 1) {
					ecoPlayer.addHome(args[0], player.getLocation());
					player.sendMessage(ChatColor.GOLD + "You created the home  " + ChatColor.GREEN + args[0] + ChatColor.GOLD + ".");
				}
				else {
					player.sendMessage("/sethome <homename>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("delhome")) {
				if(args.length == 1) {
					ecoPlayer.removeHome(args[0]);
					player.sendMessage(ChatColor.GOLD + "Your home " + ChatColor.GREEN + args[0] + ChatColor.GOLD + " was deleted.");
				}
				else {
					player.sendMessage("/deletehome <homename>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("jobList")) {
				List<String> jobNames = Job.getJobNameList();
				String jobString = jobNames.toString();
				jobString = jobString.replace("[", "");
				jobString = jobString.replace("]", "");
				if(jobNames.size() > 0) {
					player.sendMessage(ChatColor.GOLD + "All available jobs: " + ChatColor.GREEN + jobString);
				}
				else {
					player.sendMessage(ChatColor.GOLD + "No jobs exist!");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("jobInfo")) {
				if(args.length == 1) {
					try {
						Job job = Job.getJobByName(args[0]);
						player.sendMessage("");
						player.sendMessage(ChatColor.GOLD +"Jobinfo for " + ChatColor.GREEN + job.getName() + ChatColor.GOLD + ":");
						for(String string:job.getItemList()) {
							player.sendMessage(ChatColor.GOLD + string.toLowerCase() + " " + ChatColor.GREEN + job.getItemPrice(string) + "$");
						}
						for(String string:job.getFisherList()) {
							player.sendMessage(ChatColor.GOLD + "Fishing " + string.toLowerCase() + " " + ChatColor.GREEN + job.getFisherPrice(string) + "$");
						}
						for(String string:job.getEntityList()) {
							player.sendMessage(ChatColor.GOLD + "Kill " + string.toLowerCase() + " " + ChatColor.GREEN + job.getKillPrice(string) + "$");
						}
					} catch (JobSystemException e) {
						player.sendMessage(ChatColor.RED + e.getMessage());
					}
				}
				else {
					player.sendMessage("/jobInfo <jobname>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(label.equalsIgnoreCase("pay")) {
				if(args.length == 2) {
					double money = Double.valueOf(args[1]);
					ecoPlayer.payToOtherPlayer(EconomyPlayer.getEconomyPlayerByName(args[0]), money);
					Player p = Bukkit.getPlayer(args[0]);
					if(p.isOnline()) {
						p.sendMessage(ChatColor.GOLD + "You got " + ChatColor.GREEN  + " " + money +  " $ " + ChatColor.GOLD + "from " + ChatColor.GREEN + player.getName());
					}
					player.sendMessage(ChatColor.GOLD + "You gave " + ChatColor.GREEN + args[0] + " "+ money + " $ ");
				}
				else {
					player.sendMessage("/pay <name> <amount>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////towns
			else if(label.equalsIgnoreCase("town")) {
				if(args.length != 0) {
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					if(args[0].equals("create")) {
						if(args.length == 2) {
							if(!townNames.contains(args[1])) {
								TownWorld tWorld = TownWorld.getTownWorldByName(player.getWorld().getName());
								tWorld.createTown(args[1], player.getLocation(),ecoPlayer);
								townNames.add(args[1]);
								getConfig().set("TownNames", townNames);
								saveConfig();
								player.sendMessage(ChatColor.GOLD + "Congratulation! You founded the new town " + ChatColor.GREEN + args[1] + ChatColor.GOLD + "!");
						}
							else {
								player.sendMessage(ChatColor.RED + "The town " + args[1] + " already exists on this server!");
							}
						}
						else {
							player.sendMessage("/town create <townname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("delete")) {
						if(args.length == 2) {
							if(townNames.contains(args[1])) {
								try {
									TownWorld tWorld = TownWorld.getTownWorldByName(player.getWorld().getName());
									tWorld.dissolveTown(args[1],player.getName());
									townNames.remove(args[1]);
									getConfig().set("TownNames", townNames);
									saveConfig();
									player.sendMessage(ChatColor.GOLD + "The town " + ChatColor.GREEN + args[1] + ChatColor.GOLD + " was dissolved!");
								} catch (TownSystemException e) {
									player.sendMessage(ChatColor.RED + e.getMessage());
								}
							}
							else {
								player.sendMessage(ChatColor.RED + "The town " + args[1] + " does not exists on this server!");
							}
						}
						else {
							player.sendMessage("/town delete <townname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("expand")) {
						if(args.length == 2) {
							try {
								TownWorld tWorld = TownWorld.getTownWorldByName(player.getWorld().getName());
								tWorld.expandTown(args[1], player.getLocation().getChunk(),player.getName());
								player.sendMessage(ChatColor.GOLD + "Congratulation! You expanded your town with a new chunk!");
							} catch (TownSystemException | TownHasNotEnoughMoneyException e) {
								player.sendMessage(ChatColor.RED + e.getMessage());
							}	
						}
						else {
							player.sendMessage("/town expand <townname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("setTownSpawn")) {
						if(args.length == 2) {
							try {
								TownWorld tWorld = TownWorld.getTownWorldByName(player.getWorld().getName());
								Town town = tWorld.getTownByName(args[1]);
								if(town.hasCoOwnerPermission(player.getName())) {
									File file = town.setTownSpawn(tWorld.getSaveFile(),player.getLocation());
									tWorld.setSaveFile(file);
									player.sendMessage(ChatColor.GOLD + "The townspawn was set to " + ChatColor.GREEN + (int) player.getLocation().getX() + "/" + (int) player.getLocation().getY() + "/" + (int) player.getLocation().getZ() + ChatColor.GOLD + ".");
								}
							} catch (TownSystemException e) {
								player.sendMessage(ChatColor.RED + e.getMessage());
							}							}
						else {
							player.sendMessage("/town setTownSpawn <townname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("setTax")) {
						//TODO
						if(args.length == 3) {
							
						}
						else {
							
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equalsIgnoreCase("moveTownManager")) {
						if(args.length == 1) {
							try {
								TownWorld townWorld = TownWorld.getTownWorldByName(player.getWorld().getName());
								Town town = townWorld.getTownByChunk(player.getLocation().getChunk());
								File file = town.moveTownManagerVillager(townWorld.getSaveFile(), player.getLocation(), player.getName());
								townWorld.setSaveFile(file);
							} catch (TownSystemException e) {
								player.sendMessage(ChatColor.RED + e.getMessage());
							}
						}
						else {
							player.sendMessage("/town moveTownManager");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("plot")) {
						if(args.length > 1) {
							if(args[1].equals("setForSale")) {
								if(args.length == 3) {
									try {
										TownWorld townWorld = TownWorld.getTownWorldByName(player.getWorld().getName());
										Town town = townWorld.getTownByChunk(player.getLocation().getChunk());
										File file = town.setPlotForSale(townWorld.getSaveFile(), Double.valueOf(args[2]),player.getName(),player.getLocation());
										townWorld.setSaveFile(file);
										player.sendMessage(ChatColor.GOLD + "This plot is now for sale!");
									} catch (TownSystemException e) {
										player.sendMessage(ChatColor.RED + e.getMessage());
									}			
								}
								else {
									player.sendMessage("/town plot setForSale <price> ");
								}
							}
							//////////////////////////////////////////////////////////////////////////////////////////////////////////////
							else if(args[1].equals("setForRent")) {
								if(args.length == 4) {
									//TODO
								}
								else {
									player.sendMessage("/town plot setForRent <townname> <price/24h>");
								}
							}
						}
						else {
							player.sendMessage("/town plot <setForSale/setForRent>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("tp")) {
						if(args.length == 2) {
							try {
								TownWorld townWorld = TownWorld.getTownWorldByName(args[1]);
								player.teleport(townWorld.getTownByName(args[1]).getTownSpawn());
							} catch (TownSystemException e) {
								player.sendMessage(ChatColor.RED + e.getMessage());
							}
						}
						else {
							player.sendMessage("/town tp <townname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("pay")) {
						if(args.length == 3) {
							double amount = Double.valueOf(args[2]);
							ecoPlayer.decreasePlayerAmount(amount);
							TownWorld tWorld = TownWorld.getTownWorldByName(player.getWorld().getName());
							tWorld.setSaveFile(tWorld.getTownByName(args[1]).increaseTownBankAmount(tWorld.getSaveFile(), amount));
							player.sendMessage(ChatColor.GOLD + "The town " + args[1] + " got "+ ChatColor.GREEN + amount + " $" + ChatColor.GOLD + " from you!");
						}
						else {
							player.sendMessage("/town pay <townname> <amount>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("bank")) {
						if(args.length == 2) {
							try {
								TownWorld townWorld = TownWorld.getTownWorldByName(args[1]);
								Town town = townWorld.getTownByName(args[1]);
								if(town.hasCoOwnerPermission(player.getName())) {
									player.sendMessage(ChatColor.GOLD + "Town money: " + ChatColor.GREEN + town.getTownBankAmount() + ChatColor.GOLD + " $");
								}
							} catch (TownSystemException e) {
								player.sendMessage(ChatColor.RED + e.getMessage());
							}
						}
						else {
							player.sendMessage("/town bank <townname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else {
						player.sendMessage("/town <create/delete/expand/setTownSpawn/setTax/moveTownManager/plot/pay/tp/bank>");
					}
				}
				else {
					player.sendMessage("/town <create/delete/expand/setTownSpawn/setTax/moveTownManager/plot/pay/tp/bank>");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////adminshop
			else if(label.equalsIgnoreCase("adminshop")) {
				if(player.isOp()) {
					if(args.length != 0) {
						if(args[0].equals("create")) {
							if(args.length == 3) {
								if(adminShopNames.contains(args[1])) {
									player.sendMessage(ChatColor.RED + "This shop already exists");
								}
								else {
									if(Integer.valueOf(args[2])%9 == 0) {
										AdminShop adminShop = new AdminShop(this,args[1],player,args[2]);
										adminShopNames.add(args[1]);
										adminShopList.add(adminShop);
										getConfig().set("ShopNames", adminShopNames);
										saveConfig();
										player.sendMessage(ChatColor.GOLD + "The shop " + ChatColor.GREEN + args[1] + ChatColor.GOLD + " was created.");
									}
									else {
										player.sendMessage(ChatColor.RED + args[2] + " is not a multiple of 9!");
									}
								}
							}
							else {
								player.sendMessage("/adminshop create <shopname> <size (9,18,27...)>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("delete")) {
							if(args.length == 2) {
								if(adminShopNames.contains(args[1])) {
									adminShopNames.remove(args[1]);
									getConfig().set("ShopNames", adminShopNames);
									saveConfig();
									Iterator<AdminShop> iter2 = adminShopList.iterator();
									while(iter2.hasNext()) {
										AdminShop s = iter2.next();
										if(s.getName().equals(args[1])) {
											s.deleteShop();
											iter2.remove();
											player.sendMessage(ChatColor.GOLD + "The shop " + ChatColor.GREEN + args[1] + ChatColor.GOLD + " was deleted."); break;
										}
									}
								}
								else {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
							}
							else {
								player.sendMessage("/adminshop delete <shopname>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("move")) {
							if(args.length == 5) {
								boolean exists = false;
								for(AdminShop shop: adminShopList) {
									if(shop.getName().equals(args[1])) {
										exists = true;
										shop.moveShop(Integer.valueOf(args[2]), Integer.valueOf(args[3]), Integer.valueOf(args[4]));
										break;
									}
								}
								if(!exists) {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
							}
							else {
								player.sendMessage("/adminshop move <shopname> <x> <y> <z>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("editShop")) {
							if(args.length == 2) {
								boolean exists = false;
								for(AdminShop shop: adminShopList) {
									if(shop.getName().equals(args[1])) {
										exists = true;
										shop.openEditor(player);
										break;
									}
								}
								if(!exists) {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
							} 
							else {
								player.sendMessage("/adminshop editShop <shopname>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("addItem")) {
							boolean exists = false;
							if(args.length == 7 && Integer.valueOf(args[3]) <= 64) {
								for(AdminShop shop5:adminShopList) {
									if(shop5.getName().equals(args[1])) {  
										exists = true;
										if(checkValidation(player,shop5,args[2],args[3], args[4], args[5], args[6])) {
											ItemStack itemStack = new ItemStack(Material.getMaterial(args[2].toUpperCase()),Integer.valueOf(args[4]));
											shop5.addItem(player, Integer.valueOf(args[3]) - 1, Double.valueOf(args[5]), Double.valueOf(args[6]), itemStack);
										}
										break;
									}
								}
								if(!exists) {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
							}
							else if(args.length != 7){
								player.sendMessage("/adminshop addItem <shopname> <material> <slot> <amount> <sellPrice> <buyPrice>");
								player.sendMessage("If you only want to buy/sell something set sell/buyPrice = 0.0");
							}	
							else {
								player.sendMessage(ChatColor.RED + "The item amount is highter then 64!");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("removeItem")) {
							if(args.length == 3 && Integer.valueOf(args[2]) >= 0) {
								boolean shopExists = false;
								boolean size = false;
								for(AdminShop s : adminShopList) {
									if(s.getName().equals(args[1])) {
										shopExists = true;
										s.removeItem(Integer.valueOf(args[2]) - 1, player);
										if(s.getSize() >= Integer.valueOf(args[2])) {
											size = true;
										}
										break;
									}
								}
								if(!shopExists) {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
								else if(!size) {
									player.sendMessage(ChatColor.RED + "This slot doesn't exist in this shop!");
								}
							}
							else {
								player.sendMessage("/adminshop removeItem <shopname> <slot (> 0)>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("addPotion")) {
							if(args.length == 9) {
								boolean shopExists = false;
								for(AdminShop s: adminShopList) {
									if(s.getName().equals(args[1])) {
										shopExists = true;
										handleAddPotion(s, args);
										break;
									}
								}
								if(!shopExists) {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
							}
							else {
								player.sendMessage("/adminshop addPotion <shopname> <potionType> <potionEffect> <extended/upgraded/none> <slot> <amount> <sellprice> <buyprice>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("addEnchantedItem")) {
							if(args.length >= 9) {
								boolean shopExists = false;
								for(AdminShop s: adminShopList) {
									if(s.getName().equals(args[1])) {
										shopExists = true;
										handleAddEnchantedItem(player,args, s);
										break;
									}
								}
								if(!shopExists) {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
							}
							else {
								player.sendMessage("/adminshop addEnchantedItem <shopname> <material> <slot> <amount> <sellPrice> <buyPrice> [<enchantment> <lvl>]");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("addSpawner")) {
							boolean exists = false;
							if(args.length == 5) {	
								for(AdminShop shop5:adminShopList) {
									if(shop5.getName().equals(args[1])) {
										exists = true;
										if(Integer.valueOf(args[3]) <= 0) {
											player.sendMessage(ChatColor.RED + "The slot should be higher then 0!");
										}
										else if(!shop5.slotIsEmpty(Integer.valueOf(args[3]))) {
											player.sendMessage(ChatColor.RED + "This slot is occupied!");
										}
										else if(Double.valueOf(args[4]) <= 0) {
											player.sendMessage(ChatColor.RED + "The buyPrice should higher then 0!");
										}
										else {
											ItemStack itemStack = new ItemStack(Material.SPAWNER,1);
											ItemMeta meta = itemStack.getItemMeta();
											meta.setDisplayName(args[2].toUpperCase());
											itemStack.setItemMeta(meta);
											shop5.addItem(player, Integer.valueOf(args[3]) - 1, 0.0,Double.valueOf(args[4]), itemStack);
										}
										break;
									}
								}
								if(!exists) {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
							}
							else {
								player.sendMessage("/adminshop addSpawner <shopname> <entity type> <slot> <buyPrice>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("removeSpawner")) {
							if(args.length == 3) {
								boolean exists = false;
								for(AdminShop shop5:adminShopList) {
									if(shop5.getName().equals(args[1])) {
										exists = true;
										if(Integer.valueOf(args[2]) <= 0) {
											player.sendMessage(ChatColor.RED + "The slot should be higher then 0!");
										}
										else {
											shop5.removeItem(Integer.valueOf(args[2]) - 1, player); 
										}
										break;
									}
								}
								if(!exists) {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
							}
							else {
								player.sendMessage("/adminshop removeSpawner <shopname> <slot>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("editItem")) {
							if(args.length == 6) {
								boolean exists = false;	
								for(AdminShop shop5:adminShopList) {
									if(shop5.getName().equals(args[1])) {
										exists = true;
										if(Integer.valueOf(args[2]) <= 0) {
											player.sendMessage(ChatColor.RED + "The slot should be higher then 0!");
										}
										else if(shop5.slotIsEmpty(Integer.valueOf(args[2]))) {
											player.sendMessage(ChatColor.RED + "This slot is empty!");
										}
										else if(!args[3].equals("none") && Integer.valueOf(args[3]) <= 0) {
											player.sendMessage(ChatColor.RED + "The amount should be higher then 0!");
										}
										else if(!args[4].equals("none") && Integer.valueOf(args[4]) < 0) {
											player.sendMessage(ChatColor.RED + "The sellPrice should be 0 or highter!");
										}
										else if(!args[5].equals("none") && Integer.valueOf(args[5]) < 0) {
											player.sendMessage(ChatColor.RED + "The buyPrice should be 0 or highter!");
										}
										else {
											player.sendMessage(shop5.editItem(Integer.valueOf(args[2]),args[3],args[4],args[5]));
										}
									}
									break;
								}
								if(!exists) {
									player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
								}
							}
							else {
								player.sendMessage("/adminshop editItem <shopname> <slot> <amount> <sellPrice> <buyPrice>");
								player.sendMessage("If you didn't want to change one of these values set the value = none");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else {
							player.sendMessage("/adminshop <create/delete/move/editShop/addItem/addEnchantedItem/addPotion/editItem/removeItem/addSpawner/removeSpawner>");
						}
					}
					else {
						player.sendMessage("/adminshop <create/delete/move/editShop/addItem/addEnchantedItem/addPotion/editItem/removeItem/addSpawner/removeSpawner>");
					}
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////playershop
			if(label.equalsIgnoreCase("playershop")) {
				if(args.length != 0) {
					if(args[0].equals("create")) {
						if(args.length == 3) {
							if(playershopNames.contains(args[1] + "_" + player.getName())) {
								player.sendMessage(ChatColor.RED + "This shop already exist!");
							}
							else {
								if(Integer.valueOf(args[2])%9 == 0) {
									PlayerShop shop = new PlayerShop(this, args[1] + "_" + player.getName(), player,args[2]);
									playerShopList.add(shop);
									playershopNames.add(args[1] + "_" + player.getName());
									getConfig().set("PlayerShopNames", playershopNames);
									saveConfig();
									player.sendMessage(ChatColor.GOLD + "The shop " + ChatColor.GREEN + args[1] + ChatColor.GOLD + " was created.");
								}
								else {
									player.sendMessage(ChatColor.RED + args[2] + " is not a multiple of 9!");
								}
							}
						}
						else {
							player.sendMessage("/playershop create <shopname> <size (9,18,27...)>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if (args[0].equals("delete")) {
						if(args.length == 2) {
							Iterator<PlayerShop> iter2 = playerShopList.iterator();
							if(playershopNames.contains(args[1] + "_" + player.getName())) {
								playershopNames.remove(args[1] + "_" + player.getName());
								getConfig().set("PlayerShopNames", adminShopNames);
								saveConfig(); 
								while(iter2.hasNext()) {
									PlayerShop s = iter2.next();
									if(s.getName().equals(args[1] + "_" + player.getName())) {
										s.deleteShop();
										iter2.remove();
										player.sendMessage(ChatColor.GOLD + "The shop " + ChatColor.GREEN + args[1] + ChatColor.GOLD + " was deleted."); break;
									}
								}
							}
							else {
								player.sendMessage(ChatColor.RED +"This shop doesn't exist!");
							}
						}
						else {
							player.sendMessage("/playershop delete <shopname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("move")) {
						if(args.length == 5) {
							boolean exists = false;
							for(PlayerShop shop: playerShopList) {
								if(shop.getName().equals(args[1] + "_" + player.getName())) {
									exists = true;
									shop.moveShop(Integer.valueOf(args[2]), Integer.valueOf(args[3]), Integer.valueOf(args[4]));
									break;
								}
							}
							if(!exists) {
								player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
							}
						}
						else {
							player.sendMessage("/playershop move <shopname> <x> <y> <z>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("changeOwner")) {
						if(args.length == 3) {
							boolean exists = false;
							for(PlayerShop shop: playerShopList) {
								if(shop.getName().equals(args[1] + "_" + player.getName())) {
									exists = true;
									config = YamlConfiguration.loadConfiguration(playerFile);
									List<String> playerList = config.getStringList("Player");
									if(playerList.contains(args[2])) {
										List<String> psNames = getConfig().getStringList("PlayerShopNames");
										if(psNames.contains(args[1] + "_" + args[2])) {
											player.sendMessage(ChatColor.RED + "The player " + ChatColor.GREEN + args[2] + ChatColor.RED + " has already a shop with the same name!");
										}
										else {
											playershopNames.remove(shop.getName());
											shop.setOwner(args[2]);
											playershopNames.add(shop.getName());
											getConfig().set("PlayerShopNames", playershopNames);
											saveConfig();
											player.sendMessage(ChatColor.GOLD + "The new owner of your shop is " + ChatColor.GREEN + args[2] + ChatColor.GOLD + ".");
											for(Player pl:Bukkit.getOnlinePlayers()) {
												if(pl.getName().equals(args[2])) {
													pl.sendMessage(ChatColor.GOLD + "You got the shop " + ChatColor.GREEN  + args[1] + ChatColor.GOLD + "from " + ChatColor.GREEN + player.getName()); break;
												}
											}
										}
									}
									else {
										player.sendMessage(ChatColor.RED + "This player was never on this server!");
									}
									break;
								}
							}
							if(!exists) {
								player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
							}
						}
						else {
							player.sendMessage("/playershop changeOwner <shopname> <new owner>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("editShop")) {
						if(args.length == 2) {
							boolean exists = false;
							for(PlayerShop shop: playerShopList) {
								if(shop.getName().equals(args[1] + "_" + player.getName())) {
									exists = true;
									shop.openEditor(player);
									break;
								}
							}
							if(!exists) {
								player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
							}
						} 
						else {
							player.sendMessage("/player editShop <shopname>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if (args[0].equals("addItem")) {
						boolean exists = false;
						if(args.length == 7 && Integer.valueOf(args[3]) <= 64) {
							for(PlayerShop shop5:playerShopList) {
								if(shop5.getName().equals(args[1] + "_" + player.getName())) {  
									exists = true;
									if(checkValidation(player, shop5,args[2], args[3], args[4], args[5], args[6])) {
										ItemStack itemStack = new ItemStack(Material.getMaterial(args[2].toUpperCase()),Integer.valueOf(args[4]));
										shop5.addItem(player, Integer.valueOf(args[3]) - 1, Double.valueOf(args[5]), Double.valueOf(args[6]), itemStack);
									}
									break;
								}
							}
							if(!exists) {
								player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
							}
						}
						else if(args.length != 7){
							player.sendMessage("/playershop addItem <shopname> <material> <slot> <amount> <sellPrice> <buyPrice>");
							player.sendMessage("If you only want to buy/sell something set sell/buyPrice = 0.0");
						}	
						else {
							player.sendMessage(ChatColor.RED + "The item amount is highter then 64!");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("addPotion")) {
						if(args.length == 9) {
							boolean shopExists = false;
							for(PlayerShop s: playerShopList) {
								if(s.getName().equals(args[1] + "_" + player.getName())) {
									shopExists = true;
									handleAddPotion(s,args);
									break;
								}
							}
							if(!shopExists) {
								player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
							}
						}
						else {
							player.sendMessage("/playershop addPotion <shopname> <potionType> <potionEffect> <extended/upgraded/none> <slot> <amount> <sellprice> <buyprice>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("addEnchantedItem")) {
						if(args.length >= 9) {
							boolean shopExists = false;
							for(PlayerShop s: playerShopList) {
								if(s.getName().equals(args[1] + "_" + player.getName())) {
									shopExists = true;
									handleAddEnchantedItem(player,args, s);
									break;
								}
							}
							if(!shopExists) {
								player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
							}
						}
						else {
							player.sendMessage("/playershop addEnchantedItem <shopname> <material> <slot> <amount> <sellPrice> <buyPrice> [<enchantment> <lvl>]");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if (args[0].equals("removeItem")) {
						if(args.length == 3 && Integer.valueOf(args[2]) >= 0) {
							boolean shopExists = false;
							boolean size = false;
							for(PlayerShop s : playerShopList) {
								if(s.getName().equals(args[1] + "_" + player.getName())) {
									shopExists = true;
									if(Integer.valueOf(args[2]) <= 0) {
										player.sendMessage(ChatColor.RED + "The slot should be higher then 0!");
									}
									else {
										s.removeItem(Integer.valueOf(args[2]) - 1, player);
										if(s.getSize() >= Integer.valueOf(args[2])) {
											size = true;
										}
									}
									break;
								}
							}
							if(!shopExists) {
								player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
							}
							else if(!size) {
								player.sendMessage(ChatColor.RED + "This slot doesn't exist in this shop!");
							}
						}
						else {
							player.sendMessage("/playershop removeItem <shopname> <slot (> 0)>");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else if(args[0].equals("editItem")) {
						if(args.length == 6) {
							boolean exists = false;
							for(PlayerShop shop5:playerShopList) {
								if(shop5.getName().equals(args[1] + "_" + player.getName())) {
									exists = true;
									if(Integer.valueOf(args[2]) <= 0) {
										player.sendMessage(ChatColor.RED + "The slot should be higher then 0!");
									}
									else if(shop5.slotIsEmpty(Integer.valueOf(args[2]))) {
										player.sendMessage(ChatColor.RED + "This slot is empty!");
									}
									else if(!args[3].equals("none") && Integer.valueOf(args[3]) <= 0) {
										player.sendMessage(ChatColor.RED + "The amount should be higher then 0!");
									}
									else if(!args[4].equals("none") && Integer.valueOf(args[4]) < 0) {
										player.sendMessage(ChatColor.RED + "The sellPrice should be 0 or highter!");
									}
									else if(!args[5].equals("none") && Integer.valueOf(args[5]) < 0) {
										player.sendMessage(ChatColor.RED + "The buyPrice should be 0 or highter!");
									}
									else {
										player.sendMessage(shop5.editItem(Integer.valueOf(args[2]),args[3],args[4],args[5]));
									}
								}
								break;
							}
							if(!exists) {
								player.sendMessage(ChatColor.RED + "This shop doesn't exist!");
							}
						}
						else {
							player.sendMessage("/playershop editItem <shopname> <slot> <amount> <sellPrice> <buyPrice>");
							player.sendMessage("If you didn't want to change one of these value set the value = none");
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////
					else {
						player.sendMessage("/playershop <create/delete/move/editShop/addItem/addEnchantedItem/addPotion/removeItem/editItem>");
					}
				}
				else {
					player.sendMessage("/playershop <create/delete/move/editShop/addItem/addEnchantedItem/addPotion/removeItem/editItem>");
				}
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Jobcenter
			if(label.equalsIgnoreCase("jobcenter")) {
				if(player.isOp()) {
					if(args.length != 0) {
						if(args[0].equals("create")) {
							if(args.length == 3) {
								try {
									JobCenter.createJobCenter(getServer(), getDataFolder(), args[1], player.getLocation(), Integer.parseInt(args[2]));
									getConfig().set("JobCenterNames", JobCenter.getJobCenterNameList());
									saveConfig();
									player.sendMessage(ChatColor.GOLD + "The shopcenter " + ChatColor.GREEN + args[1] + ChatColor.GOLD + " was created.");
								} catch (NumberFormatException | JobSystemException e) {
									player.sendMessage(ChatColor.RED + e.getMessage());
								}
							}
							else {
								player.sendMessage("/jobcenter create <name> <size (9,18,27...)>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("delete")) {
							if(args.length == 2) {
								try {
									JobCenter.deleteJobCenter(args[1]);
									getConfig().set("JobCenterNames", JobCenter.getJobCenterNameList());
									saveConfig();
									player.sendMessage(ChatColor.GOLD + "The shopcenter " + ChatColor.GREEN + args[1] + ChatColor.GOLD + " was deleted.");
								
								} catch (JobSystemException e) {
									player.sendMessage(ChatColor.RED + e.getMessage());
								}
							}
							else {
								player.sendMessage("/jobcenter delete <name>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("move")) {
							if(args.length == 5) {
								try {
									JobCenter jobCenter = JobCenter.getJobCenterByName(args[1]);
									jobCenter.moveShop(Integer.valueOf(args[2]), Integer.valueOf(args[3]), Integer.valueOf(args[4]));
								} catch (JobSystemException e) {
									player.sendMessage(ChatColor.RED + e.getMessage());
								}
							}
							else {
								player.sendMessage("/jobcenter move <name> <x> <y> <z>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("addJob")) {
							if(args.length == 5) {
								try {
									JobCenter jobCenter = JobCenter.getJobCenterByName(args[1]);
									jobCenter.addJob(args[2], args[3], Integer.valueOf(args[4]));
									player.sendMessage(ChatColor.GOLD + "The job " + ChatColor.GREEN + args[2] + ChatColor.GOLD + " was added to the JobCenter " + ChatColor.GREEN + args[1] + ".");
								
								} catch (JobSystemException e) {
									player.sendMessage(ChatColor.RED + e.getMessage());
								}
							}
							else {
								player.sendMessage("/jobcenter addJob <jobcentername> <jobname> <material> <slot>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("removeJob")) {
							if(args.length == 3) {
								try {
									JobCenter jobCenter = JobCenter.getJobCenterByName(args[1]);
									jobCenter.removeJob(args[2]);
									player.sendMessage(ChatColor.GOLD + "The job " + ChatColor.GREEN + args[2] + ChatColor.GOLD + " was removed.");
								} catch (JobSystemException e) {
									player.sendMessage(ChatColor.RED + e.getMessage());
								}
							}
							else {
								player.sendMessage("/jobcenter removeJob <jobcentername> <jobname>");
							}
						}
						//////////////////////////////////////////////////////////////////////////////////////////////////////////////
						else if(args[0].equals("job")) {
							if(args.length == 1) {
								player.sendMessage("/jobcenter job <createJob/delJob/addItem/removeItem/addMob/removeMob/addFisher/removeFisher>");
							}
							else {
								if(args[1].equals("createJob")) {
									if(args.length == 3) {
										try {
											Job.createJob(this.getDataFolder(), args[2]);
											getConfig().set("JobList", Job.getJobNameList());
											saveConfig();
											player.sendMessage(ChatColor.GOLD + "The job " + ChatColor.GREEN + args[2] + ChatColor.GOLD + " was created.");
										} catch (JobSystemException e) {
											player.sendMessage(ChatColor.RED + e.getMessage());
										}
									}
									else {
										player.sendMessage("/jobcenter job createJob <jobname>");
									}
								}
								//////////////////////////////////////////////////////////////////////////////////////////////////////////////
								else if(args[1].equals("delJob")) {
									if(args.length == 3) {
										try {
											Job.deleteJob(args[2]);
											getConfig().set("JobList", Job.getJobNameList());
											saveConfig();		
											player.sendMessage(ChatColor.GOLD + "The job " + ChatColor.GREEN + args[2] + ChatColor.GOLD + " was deleted.");
										} catch (JobSystemException e) {
											player.sendMessage(ChatColor.RED + e.getMessage());
										}
									}
									else {
										player.sendMessage("/jobcenter job delJob <jobname>");
									}
								}
								//////////////////////////////////////////////////////////////////////////////////////////////////////////////
								else if(args[1].equals("addMob")) {
									if(args.length == 5) {
										try {
											Job job = Job.getJobByName(args[2]);
											job.addMob(args[3], Double.valueOf(args[4]));
											player.sendMessage(ChatColor.GOLD + "The entity " + ChatColor.GREEN + args[0] + ChatColor.GOLD + " was added to the job.");
										} catch (JobSystemException e) {
											player.sendMessage(ChatColor.RED + e.getMessage());
										}
									}
									else {
										player.sendMessage("/jobcenter job addMob <jobname> <entity> <price>");
									}
								}
								//////////////////////////////////////////////////////////////////////////////////////////////////////////////
								else if(args[1].equals("removeMob")) {
									if(args.length == 4) {
										try {
											Job job = Job.getJobByName(args[2]);
											job.deleteMob(args[3]);
											player.sendMessage(ChatColor.GOLD + "The entity " + ChatColor.GREEN + args[3] + ChatColor.GOLD + " was deleted from the job " + ChatColor.GREEN + job.getName() + ".");
										
										} catch (JobSystemException e) {
											player.sendMessage(ChatColor.RED + e.getMessage());
										}
									}
									else {
										player.sendMessage("/jobcenter job removeMob <jobname> <entity>");
									}
								}
								//////////////////////////////////////////////////////////////////////////////////////////////////////////////
								else if(args[1].equals("addItem")) {
									if(args.length == 5) {
										try {
											Job job = Job.getJobByName(args[2]);
											job.addItem(args[3],Double.valueOf(args[4]));
											player.sendMessage(ChatColor.GOLD + "The item " + ChatColor.GREEN + args[3] + ChatColor.GOLD + " was added to the job " + ChatColor.GREEN + job.getName() + ".");
										
										} catch (JobSystemException e) {
											player.sendMessage(ChatColor.RED + e.getMessage());
										}
									}
									else {
										player.sendMessage("/jobcenter job addItem <jobname> <material> <price>");
									}
								}
								//////////////////////////////////////////////////////////////////////////////////////////////////////////////
								else if(args[1].equals("removeItem")) {
									if(args.length == 4) {
										try {
											Job job = Job.getJobByName(args[2]);
											job.deleteItem(args[3]);
											player.sendMessage(ChatColor.GOLD + "The item " + ChatColor.GREEN + args[3] + ChatColor.GOLD + " was deleted from the job " + ChatColor.GREEN + job.getName() + ".");
										
										} catch (JobSystemException e) {
											player.sendMessage(ChatColor.RED + e.getMessage());
										}
									}
									else {
										player.sendMessage("/jobcenter job removeItem <jobname> <material>");
									}
								}
								//////////////////////////////////////////////////////////////////////////////////////////////////////////////
								else if(args[1].equals("addFisher")) {
									if(args.length == 5) {
										try {
											Job job = Job.getJobByName(args[2]);
											job.addFisherLootType(args[3], Double.valueOf(args[4]));
										} catch (JobSystemException e) {
											player.sendMessage(ChatColor.RED + e.getMessage());
										}
									}
									else {
										player.sendMessage("/jobcenter job addFisher <jobname> <fish/treasure/junk> <price>");
									}
								}
								//////////////////////////////////////////////////////////////////////////////////////////////////////////////
								else if(args[1].equals("removeFisher")) {
									if(args.length == 4) {
										try {
											Job job = Job.getJobByName(args[2]);
											job.delFisherLootType(args[3]);;
										} catch (JobSystemException e) {
											player.sendMessage(ChatColor.RED + e.getMessage());
										}
									}
									else {
										player.sendMessage("/jobcenter job removeFisher <jobname> <fish/treasure/junk>");
									}
								}
								//////////////////////////////////////////////////////////////////////////////////////////////////////////////
								else {
									player.sendMessage("/jobcenter job <createJob/delJob/addItem/addFisher/removeFisher/removeItem/addMob/removeMob>");
								}
							}
						}
						else {
							player.sendMessage("/jobcenter <create/delete/move/job/addJob>");
						}
					}
					else {
						player.sendMessage("/jobcenter <create/delete/move/job/addjob>");
					}
				}
			}
		}
		return false;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Events
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@EventHandler
	public void PlayerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() != null && !event.getPlayer().hasPermission("ultimate_economy.wilderness")) {
			Location location = event.getClickedBlock().getLocation();
			try {
				TownWorld townWorld = TownWorld.getTownWorldByName(location.getWorld().getName());
				if(townWorld.chunkIsFree(location.getChunk())) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.RED + "You are in the wilderness!");
				}
				else {
					Town town = townWorld.getTownByChunk(location.getChunk());
					if(!town.isPlayerCitizen(event.getPlayer().getName()) || !town.hasBuildPermissions(event.getPlayer().getName(), location.getChunk().getX() + "/" + location.getChunk().getZ())) {
						event.setCancelled(true);
						event.getPlayer().sendMessage(ChatColor.RED + "You have no permission on this plot!");
					}
				}
			} catch (TownSystemException e) {}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@EventHandler
	public void NPCOpenInv(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if(entity instanceof Villager && ((Villager) entity).getProfession() == Profession.NITWIT) {
			String name = entity.getCustomName();
			for(AdminShop shop4:adminShopList) {
				if(shop4.getName().equals(name)) {   
				event.setCancelled(true);
				shop4.openInv(event.getPlayer());
				break;
				}
			}
			for(PlayerShop shop4:playerShopList) {
				if(shop4.getName().equals(name)) {   
				event.setCancelled(true);
				shop4.openInv(event.getPlayer());
				break;
				}
			}
			
			try {
				JobCenter jobCenter = JobCenter.getJobCenterByName(name);
				event.setCancelled(true);
				jobCenter.openInv(event.getPlayer());
			} catch (JobSystemException e) {
				//is not a jobcenter villager
			}
			
			if(name.contains("For Sale!")) {
				event.setCancelled(true);
				try {
					TownWorld townWorld = TownWorld.getTownWorldByName(entity.getWorld().getName());
					Town town = townWorld.getTownByChunk(entity.getLocation().getChunk());
					Plot plot = town.getPlotByChunk(entity.getLocation().getChunk().getX(), entity.getLocation().getChunk().getZ());
					plot.openSaleVillagerInv(event.getPlayer());
				} catch (TownSystemException e) {}
			}
			else if(name.contains("TownManager")) {
				event.setCancelled(true);
				try {
					TownWorld townWorld = TownWorld.getTownWorldByName(entity.getWorld().getName());
					Town town = townWorld.getTownByChunk(entity.getLocation().getChunk());
					town.openTownManagerVillagerInv(event.getPlayer());
				} catch (TownSystemException e) {}
				
			}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@EventHandler
	public void HitVillagerEvent(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Villager) {
			String entityname = event.getEntity().getCustomName();
			Player damager = (Player) event.getDamager();
			if(JobCenter.getJobCenterNameList().contains(entityname)) {
				event.setCancelled(true);
				damager.sendMessage(ChatColor.RED + "You are not allowed to hit a jobCenterVillager!");
			}
			else if(adminShopNames.contains(entityname)) {
				event.setCancelled(true);
				damager.sendMessage(ChatColor.RED + "You are not allowed to hit a shopVillager!");
			}
			else if(playershopNames.contains(entityname)) {
				event.setCancelled(true);
				damager.sendMessage(ChatColor.RED + "You are not allowed to hit a shopVillager!");
			}
			else if(entityname.contains("For Sale!")) {
				event.setCancelled(true);
				damager.sendMessage(ChatColor.RED + "You are not allowed to hit a saleVillager!");
			}
			else if(entityname.contains("TownManager")) {
				event.setCancelled(true);
				damager.sendMessage(ChatColor.RED + "You are not allowed to hit a townManagerVillager!");
			}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if(entity.getKiller() instanceof Player) {
			EconomyPlayer ecoPlayer;
			try {
				ecoPlayer = EconomyPlayer.getEconomyPlayerByName(entity.getKiller().getName());
				if(!ecoPlayer.getJobList().isEmpty() && player.getGameMode() == GameMode.SURVIVAL) {
					for(Job job: Job.getJobList()) {
						try {
							double d = job.getKillPrice(entity.getType().toString());
							ecoPlayer.increasePlayerAmount(d);
						} catch (PlayerException | JobSystemException e) {}
						break;
					}
				}
			} catch (PlayerException e1) {}		
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@EventHandler
	public void InvClickEvent(InventoryClickEvent event) {
		Player playe = (Player) event.getWhoClicked();
		try {
			EconomyPlayer ecoPlayer = EconomyPlayer.getEconomyPlayerByName(playe.getName());
			if(event.getCurrentItem() != null && event.getInventory().getType() == InventoryType.ANVIL && event.getCurrentItem().getType() == Material.SPAWNER) {
				event.setCancelled(true);
			}
			event.getView().getTitle();
			//1.13
			//String inventoryname = event.getInventory().get;
			//1.14
			String inventoryname = event.getView().getTitle();
			if(event.getInventory().getType() == InventoryType.CHEST && event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {
				ItemMeta meta = event.getCurrentItem().getItemMeta();
				////////////////////////////////////////////////////////////////////////////editor
				if(inventoryname.contains("Editor")) {
					event.setCancelled(true);
					if(meta.getDisplayName() != null) {
						for(AdminShop adminShop: adminShopList) {
							handleEditor(playe, adminShop, event);
						}
						for(PlayerShop playerShop: playerShopList) {
							handleEditor(playe, playerShop, event);
						}
					}
				}
				////////////////////////////////////////////////////////////////////////////saleVillager
				else if(inventoryname.contains("Plot") || inventoryname.contains("TownManager")) {
					event.setCancelled(true);
					TownWorld townWorld = TownWorld.getTownWorldByName(playe.getWorld().getName());
					townWorld.handleTownVillagerInvClick(event);
					playe.closeInventory();
				}
				////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////
				ClickType clickType = event.getClick();
				if(event.getCurrentItem().getItemMeta() != null) {
					////////////////////////////////////////////////////////////////////////////Job
					JobCenter jobCenter = JobCenter.getJobCenterByName(inventoryname);
					if(jobCenter.getName().equals(inventoryname)) {
						event.setCancelled(true);
						String displayname = meta.getDisplayName();
						if(clickType == ClickType.RIGHT && displayname != null) {
							if(!ecoPlayer.getJobList().isEmpty()) {
								ecoPlayer.removeJob(displayname);
								playe.sendMessage(ChatColor.GOLD + "You have left the job " + ChatColor.GREEN + displayname);	
							}
							else if(!displayname.equals("Info")){
								playe.sendMessage(ChatColor.RED + "You didn't joined a job yet!");
							}
						}
						else if(clickType == ClickType.LEFT && displayname != null) {
							if(!ecoPlayer.getJobList().isEmpty()) {
								ecoPlayer.addJob(displayname);
								playe.sendMessage(ChatColor.GOLD + "You have joined the job " + ChatColor.GREEN + displayname);
							}
						}
					}
					////////////////////////////////////////////////////////////////////////////playershop
					for(PlayerShop playerShop:playerShopList) {
						handleBuySell(playerShop, event, playe);
					}
					//////////////////////////////////////////////////////////////////////////// adminshop
					for(AdminShop adminShop:adminShopList) {
						handleBuySell(adminShop, event, playe);
					}
				}
			}
		} catch (JobSystemException | TownSystemException e) {
		} catch (PlayerException e) {
			playe.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@EventHandler
	public void setBlockEvent(BlockPlaceEvent event) {
		if(event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
			if(event.getBlock().getBlockData().getMaterial() == Material.SPAWNER) {
				if(event.getItemInHand().getItemMeta().getDisplayName().contains("-")) {
					String spawnerowner = event.getItemInHand().getItemMeta().getDisplayName().substring(event.getItemInHand().getItemMeta().getDisplayName().lastIndexOf("-") + 1);
					if(spawnerowner.equals(event.getPlayer().getName())) {
							String string = event.getItemInHand().getItemMeta().getDisplayName();
							Spawner.setSpawner(EntityType.valueOf(string.substring(0,string.lastIndexOf("-"))), event.getBlock());
							event.getBlock().setMetadata("name", new FixedMetadataValue(this, string.substring(string.lastIndexOf("-") + 1)));
							event.getBlock().setMetadata("entity", new FixedMetadataValue(this, string.substring(0,string.lastIndexOf("-"))));
							config = YamlConfiguration.loadConfiguration(spawner);
							double x = event.getBlock().getX();
							double y = event.getBlock().getY();
							double z = event.getBlock().getZ();
							String spawnername = String.valueOf(x) + String.valueOf(y) + String.valueOf(z);
							spawnername = spawnername.replace(".", "-");
							spawnerlist.add(spawnername);
							getConfig().set("Spawnerlist", spawnerlist);
							saveConfig();
							config.set(spawnername + ".X", x);
							config.set(spawnername + ".Y", y);
							config.set(spawnername + ".Z", z);
							config.set(spawnername + ".World", event.getBlock().getWorld().getName());
							config.set(spawnername + ".player",string.substring(string.lastIndexOf("-") + 1));
							config.set(spawnername + ".EntityType", string.substring(0,string.lastIndexOf("-")));
							saveFile(spawner);
						}
					else {
						event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to place this spawner!");
						event.setCancelled(true);
					}
				}
			}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@EventHandler
	public void breakBlockEvent(BlockBreakEvent event) {
		if(event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
			try {
				EconomyPlayer ecoPlayer = EconomyPlayer.getEconomyPlayerByName(event.getPlayer().getName());
				List<String> jobList = ecoPlayer.getJobList();
				if(!jobList.isEmpty()) {
					Material blockMaterial = event.getBlock().getBlockData().getMaterial();
					for(String jobName:jobList) {
						try {
							Job job = Job.getJobByName(jobName);
							if(blockMaterial == Material.POTATOES || blockMaterial == Material.CARROTS ||blockMaterial == Material.WHEAT || blockMaterial == Material.NETHER_WART_BLOCK || blockMaterial == Material.BEETROOTS) {
								Ageable ageable = (Ageable) event.getBlock().getBlockData();
								if(ageable.getAge() == ageable.getMaximumAge()) {
									double d = job.getItemPrice(blockMaterial.toString());
									ecoPlayer.increasePlayerAmount(d);
								}	
							}
							else {
								double d = job.getItemPrice(blockMaterial.toString());
								ecoPlayer.increasePlayerAmount(d);
							}
						} catch (JobSystemException e) {
							break;
						} catch (PlayerException e) {}
					}
				}
			} catch (PlayerException e1) {}
			if(event.getBlock().getBlockData().getMaterial() == Material.SPAWNER) {
				List<MetadataValue> blockmeta = event.getBlock().getMetadata("name");
				if(!blockmeta.isEmpty()) {
					MetadataValue s = blockmeta.get(0);
					String blockname = s.asString();
					if(event.getPlayer().getName().equals(blockname)) {
						if(!event.getBlock().getMetadata("entity").isEmpty()) {
							config = YamlConfiguration.loadConfiguration(spawner);
							double x = event.getBlock().getX();
							double y = event.getBlock().getY();
							double z = event.getBlock().getZ();
							String spawnername = String.valueOf(x) + String.valueOf(y) + String.valueOf(z);
							spawnername = spawnername.replace(".", "-");
							spawnerlist.remove(spawnername);
							getConfig().set("Spawnerlist", spawnerlist);
							saveConfig();
							config.set(spawnername, null);
							saveFile(spawner);
							ItemStack stack = new ItemStack(Material.SPAWNER, 1);
							ItemMeta meta = stack.getItemMeta();
							meta.setDisplayName(event.getBlock().getMetadata("entity").get(0).asString() + "-" + event.getPlayer().getName());     
							stack.setItemMeta(meta);
							event.getPlayer().getInventory().addItem(stack);
						}
					}
					else {
						event.setCancelled(true);
						event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to break this spawner!");
					}
				}
			}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@EventHandler
	public void JoinEvent(PlayerJoinEvent event) {
		String playername = event.getPlayer().getName();
		try {
			if(playerlist.contains(playername)) {
				playerlist.add(playername);
				EconomyPlayer.createEconomyPlayer(playername);
			}
		} catch (PlayerException e) {
			Bukkit.getLogger().log(Level.WARNING,e.getMessage(),e);
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@EventHandler
	public void FishingEvent(PlayerFishEvent event) {
		if(event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) { 
			config = YamlConfiguration.loadConfiguration(playerFile);
			EconomyPlayer ecoPlayer = EconomyPlayer.getEconomyPlayerByName(event.getPlayer().getName());
			List<String> list = config.getStringList(event.getPlayer().getName() + ".Jobs");
			if(!list.isEmpty()) {
				String lootType = "";
				try {
					Item caught =(Item) event.getCaught();
					if(caught != null) {
						switch(caught.getItemStack().getType().toString()) {
						case "COD": 
						case "SALMON": 
						case "TROPICAL_FISH":
						case "PUFFERFISH": lootType = "fish"; break;
						case "BOW": 
						case "ENCHANTED_BOOK": 
						case "FISHING_ROD":
						case "NAME_TAG": 
						case "NAUTILUS_SHELL": 
						case "SADDLE":
						case "LILY_PAD": lootType = "treasure"; break;
						default: lootType = "junk";
						}
						for(String jobName:list) {
							Job job = Job.getJobByName(jobName);
							Double price = job.getFisherPrice(lootType);
							playerFile = PaymentUtils.increasePlayerAmount(playerFile, event.getPlayer().getName(), price);
						}
					}
				} catch(ClassCastException | JobSystemException | PlayerDoesNotExistException e) {}
			}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Methoden
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private List<String> getMaterialList(String arg) {
		Material[] materials = Material.values();
		List<String> list = new ArrayList<>();
		if(arg.equals("")) {
			for(Material material: materials) {
				list.add(material.name().toLowerCase());
			}
		}
		else {
			for(Material material: materials) {
				if(material.name().toLowerCase().contains(arg)) {
					list.add(material.name().toLowerCase());
				}
			}
		}
		return list;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private List<String> getEntityList(String arg) {
		List<String> list = new ArrayList<>();
		EntityType[] entityTypes = EntityType.values();
		if(arg.equals("")) {
			for(EntityType entityname: entityTypes) {
				list.add(entityname.name().toLowerCase());
			}
		}
		else {
			for(EntityType entityname: entityTypes) {
				if(entityname.name().toLowerCase().contains(arg)) {
					list.add(entityname.name().toLowerCase());
				}
			}
		}
		return list;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private List<String> getJobList(String arg) {
		List<String> temp = getConfig().getStringList("JobList");
		List<String> list = new ArrayList<>();
		if(arg.equals("")) {
			list = temp;
		}
		else {
			for(String jobname:temp) {
				if(jobname.contains(arg)) {
					list.add(jobname);
				}
			}
		}
		return list;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private List<String> getAdminShopList(String arg) {
		List<String> temp = getConfig().getStringList("ShopNames");
		List<String> list = new ArrayList<>();
		if(arg.equals("")) {
			list = temp;
		}
		else {
			for(String shopName:temp) {
				if(shopName.contains(arg)) {
					list.add(shopName);
				}
			}
		}
		return list;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private List<String> getPlayerShopList(String arg,String playerName) {
		List<String> temp = getConfig().getStringList("PlayerShopNames");
		List<String> list = new ArrayList<>();
		if(arg.equals("")) {
			for(String shopName:temp) {
				if(shopName.substring(shopName.indexOf("_")+1).equals(playerName)) {
					list.add(shopName.substring(0,shopName.indexOf("_")));
				}
			}
		}
		else {
			for(String shopName:temp) {
				if(shopName.substring(0,shopName.indexOf("_")).contains(arg) && shopName.substring(shopName.indexOf("_")+1).equals(playerName)) {
					list.add(shopName.substring(0,shopName.indexOf("_")));
				}
			}
		}
		return list;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private List<String> getHomeList(String arg,String playerName) {
		config = YamlConfiguration.loadConfiguration(playerFile);
		List<String> temp = config.getStringList(playerName + ".Home.Homelist");
		List<String> list = new ArrayList<>();
		if(arg.equals("")) {
			list = temp;
		}
		else {
			for(String homeName:temp) {
				if(homeName.contains(arg)) {
					list.add(homeName);
				}
			}
		}
		return list;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void saveFile(File file) {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private boolean checkValidation(Player player, Shop s,String material, String slot, String amount, String sellPrice, String buyPrice) {
		boolean correct = false;
		if(Integer.valueOf(slot) <= 0) {
			player.sendMessage(ChatColor.RED + "The slot should be higher then 0!");
		}
		else if(!s.slotIsEmpty(Integer.valueOf(slot))) {
			player.sendMessage(ChatColor.RED + "This slot is occupied!");
		}
		else if(Integer.valueOf(amount) <= 0) {
			player.sendMessage(ChatColor.RED + "The amount should be higher then 0!");
		}
		else if(Double.valueOf(sellPrice) < 0) {
			player.sendMessage(ChatColor.RED + "The sellPrice should be 0 or highter!");
		}
		else if(Double.valueOf(buyPrice) < 0) {
			player.sendMessage(ChatColor.RED + "The buyPrice should be 0 or highter!");
		}
		else if(!material.toUpperCase().equals("HAND") && Material.matchMaterial(material.toUpperCase()) == null) {
			player.sendMessage(ChatColor.RED + "Invalid material!");
		}
		else {
			correct = true;
		}
		return correct;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void handleAddPotion(Shop s, String[] args) {
		if(!args[2].equalsIgnoreCase("potion") && !args[2].equalsIgnoreCase("splash_potion") && !args[2].equalsIgnoreCase("lingering_potion")) {
			player.sendMessage(ChatColor.RED + "PotionType is not correct! Use potion/splash_potion/lingering_potion");
		}
		else if(!args[4].equalsIgnoreCase("extended") && !args[4].equalsIgnoreCase("upgraded") && !args[4].equalsIgnoreCase("none")) {
			player.sendMessage(ChatColor.RED + "Use extended/upgraded/none!");
		}
		else if(checkValidation(player, s,args[2], args[5], args[6], args[7], args[8])) {
			ItemStack itemStack = new ItemStack(Material.valueOf(args[2].toUpperCase()),Integer.valueOf(args[6]));
			PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
			boolean extended = false;
			boolean upgraded = false;
			if(args[4].equalsIgnoreCase("extended")) {
				extended = true;
			}
			else if(args[4].equalsIgnoreCase("upgraded")) {
				upgraded = true;
			}
			meta.setBasePotionData(new PotionData(PotionType.valueOf(args[3].toUpperCase()),extended,upgraded));
			itemStack.setItemMeta(meta);
			s.addItem(player, Integer.valueOf(args[5]) - 1, Double.valueOf(args[7]), Double.valueOf(args[8]), itemStack);
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void handleAddEnchantedItem(Player p,String[] args,Shop s) {
		if(checkValidation(player, s,args[2], args[3], args[4], args[5], args[6])) {
			Integer length = args.length - 7;
			if(length % 2 == 0) {
				ArrayList<String> enchantmentList = new ArrayList<>();
				for(Integer i=1; i<length; i = i + 2) {
					enchantmentList.add(args[i+6].toLowerCase() + "-" + args[i+7]);
				}
				ItemStack iStack = new ItemStack(Material.valueOf(args[2].toUpperCase()),Integer.valueOf(args[4]));
				ArrayList<String> newEnchantmentList = Shop.addEnchantments(iStack, enchantmentList);
				if(newEnchantmentList.size() < enchantmentList.size()) {
					p.sendMessage(ChatColor.RED + "Not all enchantments could be used!");
				}
				s.addItem(p, Integer.valueOf(args[3]) - 1, Double.valueOf(args[5]), Double.valueOf(args[6]), iStack);
			}
			else {
				player.sendMessage(ChatColor.RED + "Your list [<enchantment> <lvl>] is incomplete!");
			}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void handleEditor(Player playe,Shop shop,InventoryClickEvent event) {
		ItemMeta meta = event.getCurrentItem().getItemMeta();
		String command = meta.getDisplayName();
		//1.13
		//String inventoryname = event.getInventory().get;
		//1.14
		String inventoryname = event.getView().getTitle();
		if(inventoryname.equals(shop.getName() + "-Editor") && meta.getDisplayName().contains("Slot")) {
			int slot = Integer.valueOf(meta.getDisplayName().substring(5));
			shop.openSlotEditor(playe, slot);
		}
		else if(inventoryname.equals(shop.getName() + "-SlotEditor")) {
			shop.handleSlotEditor(event);
			if(command.equals(ChatColor.RED + "remove item") || command.equals(ChatColor.RED + "exit without save") || command.equals(ChatColor.YELLOW + "save changes")) {
				shop.openEditor(playe);
			}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private boolean inventoryContainsItems(Inventory inventory,ItemStack item, int amount) {
		boolean bool = false;
		int realAmount = 0;
		int amountStack = 0;
		for(ItemStack s: inventory.getContents()) {
			if(s != null) {
				ItemStack stack = new ItemStack(s);
				Repairable repairable = (Repairable) stack.getItemMeta();
				if(repairable != null) {
					repairable.setRepairCost(0);
					stack.setItemMeta((ItemMeta) repairable);
				}
				item.setAmount(1);
				amountStack = stack.getAmount();
				stack.setAmount(1);
				if(item.equals(stack)) {
					realAmount += amountStack;
				}
			}
		}
		if(realAmount >= amount) {
			bool = true;
		}
		return bool;
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void removeItemFromInventory(Inventory inventory,ItemStack item,int amount) {
		int amount2 = amount;
		int amountStack = 0;
		int repairCosts = 0;
		boolean isRepairable = false;
		for(ItemStack s: inventory.getContents()) {
			if(s != null) {
				ItemStack stack = new ItemStack(s);
				Repairable repairable = (Repairable) stack.getItemMeta();
				if(repairable != null) {
					repairCosts = repairable.getRepairCost();
					repairable.setRepairCost(0);
					stack.setItemMeta((ItemMeta) repairable);
					isRepairable = true;
				}
				item.setAmount(1);
				amountStack = stack.getAmount();
				stack.setAmount(1);
				if(item.equals(stack) && amount2 != 0) {
					if(isRepairable) {
						repairable.setRepairCost(repairCosts);
						stack.setItemMeta((ItemMeta) repairable);
					}
					if(amount2 >= amountStack) {
						stack.setAmount(amountStack);
						inventory.removeItem(stack);
						amount2 -= amountStack;
					}
					else {
						stack.setAmount(amount2);
						inventory.removeItem(stack);
						amount2-= amount2;
					}
				}
			}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void handleBuySell(Shop shop,InventoryClickEvent event,Player playe) {
		double sellprice;
		double buyprice;
		//double playermoney;
		int amount;
		boolean isPlayershop = false;
		boolean alreadysay = false;
		List<String> lore = new ArrayList<>();
		int damage = 0;
		String shopOwner = "";
		ClickType clickType = event.getClick();
		//1.13
		//String inventoryname = event.getInventory().get;
		//1.14
		String inventoryname = event.getView().getTitle();
		Inventory inventoryplayer = event.getWhoClicked().getInventory();
		PlayerShop playerShop = null;
		if(shop instanceof PlayerShop) {
			isPlayershop = true;
			playerShop = (PlayerShop) shop;
			shopOwner = playerShop.getOwner();
		}
		if(shop.getName().equals(inventoryname)) {
			event.setCancelled(true);
			config = YamlConfiguration.loadConfiguration(new File(getDataFolder() , shop.getName() + ".yml"));
			List<String> itemlist = shop.getItemList();
			String item3;
			//Playershop
			if(isPlayershop && clickType == ClickType.MIDDLE && shopOwner.equals(playe.getName())) {
				playerShop.switchStockpile();
			} //
			else {
				for(String item2: itemlist) {
					String s = event.getCurrentItem().getType().toString();
					//only relevant for adminshop
					boolean isSpawner = false;
					if(item2.contains("SPAWNER")) {
						item3 = "SPAWNER";
						isSpawner = true;
					}
					else {
						item3 = item2;
					}
					EnchantmentStorageMeta metaEnchanted = null;
					PotionMeta metaPotion = null;
					if(event.getCurrentItem().getType().toString().equals("ENCHANTED_BOOK")) {
						metaEnchanted = (EnchantmentStorageMeta) event.getCurrentItem().getItemMeta();
						if(!metaEnchanted.getStoredEnchants().isEmpty()) {
							List<String> list = new ArrayList<>();
							for(Entry<Enchantment, Integer> map: metaEnchanted.getStoredEnchants().entrySet()) {
								list.add(map.getKey().getKey().toString().substring(map.getKey().getKey().toString().indexOf(":") + 1) + "-" + map.getValue().intValue());
								list.sort(String.CASE_INSENSITIVE_ORDER);
							}
							s = event.getCurrentItem().getType().toString().toLowerCase() + "#Enchanted_" + list.toString();
						}
					}
					else if(item3.contains("#Enchanted_")) {
						if(!event.getCurrentItem().getEnchantments().isEmpty()) {
							List<String> list = new ArrayList<>();
							for(Entry<Enchantment, Integer> map: event.getCurrentItem().getEnchantments().entrySet()) {
								list.add(map.getKey().getKey().toString().substring(map.getKey().getKey().toString().indexOf(":") + 1) + "-" + map.getValue().intValue());
								list.sort(String.CASE_INSENSITIVE_ORDER);
							}
							s = event.getCurrentItem().getType().toString().toLowerCase() + "#Enchanted_" + list.toString();
						}
					}
					else if(item3.contains("potion:")) {
						if(event.getCurrentItem().getType().toString().contains("POTION")) {
							metaPotion = (PotionMeta) event.getCurrentItem().getItemMeta();
							String type = metaPotion.getBasePotionData().getType().toString().toLowerCase();
							String property;
							if(metaPotion.getBasePotionData().isExtended()) {
								property = "extended";
							}
							else if(metaPotion.getBasePotionData().isUpgraded()) {
								property = "upgraded";
							}
							else {
								property = "none";
							}
							s = event.getCurrentItem().getType().toString().toLowerCase() + ":" + type + "#" + property;
						}
					}
					else if(!item3.contains("SPAWNER")){
						s = event.getCurrentItem().getType().name();
					}
					ItemMeta meta2 = event.getCurrentItem().getItemMeta();
					ItemStack testStack = new ItemStack(event.getCurrentItem().getType());
					ItemMeta testMeta = testStack.getItemMeta();
					if(!isSpawner && !event.getCurrentItem().getItemMeta().getDisplayName().equals(testMeta.getDisplayName())) {
						s = meta2.getDisplayName() + "|" + s;
					}
					if(item3.equals(s)) {
						config = YamlConfiguration.loadConfiguration(new File(getDataFolder() , shop.getName() + ".yml"));
						String materialName = null;
						//only relevant for adminshop
						if(isSpawner) {
							materialName = "SPAWNER";
						}
						else {
							materialName = item2;
						}
						if(materialName.equals(item3)) {
							sellprice = config.getDouble("ShopItems." + item2 +  ".sellPrice");	
							buyprice = config.getDouble("ShopItems." + item2 +  ".buyPrice");
							amount = config.getInt("ShopItems." + item2 +  ".Amount");	
							lore = config.getStringList("ShopItems." + item2 + ".lore");
							damage = config.getInt("ShopItems." + item2 + ".damage");
							List<String> loreRealItem = event.getCurrentItem().getItemMeta().getLore();
							if(loreRealItem != null) {
								Iterator<String> iterator = loreRealItem.iterator();
								while(iterator.hasNext()) {
									String string = iterator.next();
									if(string.contains("buy for") || string.contains("sell for")) {
										iterator.remove();
									}
								}	
							}
							else {
								loreRealItem = new ArrayList<>();
							}
							Damageable damageMeta = (Damageable) event.getCurrentItem().getItemMeta();
							int realDamage = 0;
							if(damageMeta != null) {
								realDamage = damageMeta.getDamage();
							}
							if(lore.equals(loreRealItem) && damage == realDamage) {
								try {
									if(clickType == ClickType.LEFT) {
										if(buyprice != 0.0 && PaymentUtils.playerHasEnoughtMoney(playerFile, playe.getName(), buyprice) || playe.getName().equals(shopOwner)) {
											if(!isPlayershop || playerShop.available(materialName)) {  
												if(inventoryplayer.firstEmpty() != -1) {
													//only adminshop
													if(isSpawner && event.getCurrentItem().getItemMeta().getDisplayName().equals(item2.substring(8))) {
														ItemStack stack = new ItemStack(Material.getMaterial(materialName), amount);
														ItemMeta meta = stack.getItemMeta();
														meta.setDisplayName(item2.substring(8) + "-" + playe.getName());   
														stack.setItemMeta(meta);
														inventoryplayer.addItem(stack);
														playerFile = PaymentUtils.decreasePlayerAmount(playerFile, playe.getName(), buyprice, true);
														if(amount > 1) {
															playe.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " " + ChatColor.GOLD + "Items" + ChatColor.GOLD + " were bought for " + ChatColor.GREEN + buyprice + ChatColor.GREEN + "$"+ ChatColor.GOLD + ".");
														}
														else {
															playe.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " " + ChatColor.GOLD + "Item" + ChatColor.GOLD + " was bought for " + ChatColor.GREEN + buyprice + ChatColor.GREEN + "$"+ ChatColor.GOLD + ".");
														}
													} //
													else if(!isSpawner){
														String displayName = "default";
														
														if(materialName.contains("|")) {
															displayName = materialName.substring(0,materialName.indexOf("|"));
															materialName = materialName.substring(materialName.indexOf("|")+1);
														}
														boolean isEnchanted = false;
														boolean isPotion = false;
														if(materialName.contains("#Enchanted_")) {
															isEnchanted = true;
															materialName = materialName.substring(0,materialName.indexOf("#")).toUpperCase();
														}
														else if(materialName.contains("potion:")) {
															isPotion = true;
															materialName = materialName.substring(0,materialName.indexOf(":")).toUpperCase();
														}
														ItemStack stack = new ItemStack(Material.getMaterial(materialName), amount);
														if(isEnchanted) {
															if(metaEnchanted != null) {
																metaEnchanted.setLore(lore);
																stack.setItemMeta(metaEnchanted);
															}
															else {
																stack.addEnchantments(event.getCurrentItem().getEnchantments());
															}
															if(isPlayershop) {
																playerShop.decreaseStock(s, amount);
															}
														}
														else if(isPotion) {
															if(metaPotion != null) {
																metaPotion.setLore(lore);
																stack.setItemMeta(metaPotion);
															}
															//only playershop
															if(isPlayershop) {
																playerShop.decreaseStock(s, amount);
															}
														}
														//only playershop
														else if(isPlayershop){
															playerShop.decreaseStock(s, amount);
														} //
														ItemMeta meta = stack.getItemMeta();
														meta.setLore(lore);
														if(!displayName.equals("default")) {
															meta.setDisplayName(displayName);
														}
														if(damage > 0) {
															Damageable damageMeta2 = (Damageable) meta;
															damageMeta2.setDamage(damage);
															meta = (ItemMeta) damageMeta2;
														}
														stack.setItemMeta(meta);
														inventoryplayer.addItem(stack);
														if(!isPlayershop || !shopOwner.equals(playe.getName())) {
															playerFile = PaymentUtils.decreasePlayerAmount(playerFile, playe.getName(), buyprice, true);
															//only playershop
															if(isPlayershop) {
																playerFile = PaymentUtils.increasePlayerAmount(playerFile, Bukkit.getPlayer(shopOwner).getName(), buyprice);
															}
															if(amount > 1) {
																playe.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " " + ChatColor.GOLD + "Items" + ChatColor.GOLD + " were bought for " + ChatColor.GREEN + buyprice + ChatColor.GREEN + "$"+ ChatColor.GOLD + ".");
															}
															else {
																playe.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " " + ChatColor.GOLD + "Item" + ChatColor.GOLD + " was bought for " + ChatColor.GREEN + buyprice + ChatColor.GREEN + "$"+ ChatColor.GOLD + ".");
															}
														}
														//only playershop
														else if(isPlayershop && shopOwner.equals(playe.getName())) {
															if(amount > 1) {
																playe.sendMessage(ChatColor.GOLD + "You got " + ChatColor.GREEN + String.valueOf(amount) + ChatColor.GOLD + " Items from the shop.");
															}
															else {
																playe.sendMessage(ChatColor.GOLD + "You got " + ChatColor.GREEN + String.valueOf(amount) + ChatColor.GOLD + " Item from the shop.");
															}
														}
														//only playershop
														if(isPlayershop) {
															playerShop.refreshStockpile();
														}
														break;
													}
												}
												else {
													playe.sendMessage(ChatColor.RED + "There is no free slot in your inventory!");
												}
											}
											//only playershop
											else if(isPlayershop){
												playe.sendMessage(ChatColor.GOLD + "This item is unavailable!");
											}
										} 
										else if(!PaymentUtils.playerHasEnoughtMoney(playerFile, playe.getName(), buyprice) && !alreadysay) {
											playe.sendMessage(ChatColor.RED + "You don't have enough money!");
											alreadysay = true;
										}
									}
									else if(clickType == ClickType.RIGHT && !materialName.contains("ANVIL_0") && !materialName.contains("CRAFTING_TABLE_0")&& sellprice != 0.0 || clickType == ClickType.RIGHT && playe.getName().equals(shopOwner) && inventoryplayer.containsAtLeast(new ItemStack(Material.getMaterial(materialName), 1), amount)) {
										String displayName = "default";
										if(materialName.contains("|")) {
											displayName = materialName.substring(0,materialName.indexOf("|"));
											materialName = materialName.substring(materialName.indexOf("|")+1);
										}
										boolean isEnchanted = false;
										boolean isPotion = false;
										if(materialName.contains("#Enchanted_")) {
											isEnchanted = true;
											materialName = materialName.substring(0,materialName.indexOf("#")).toUpperCase();
										}
										if(materialName.contains("potion:")) {
											isPotion = true;
											materialName = materialName.substring(0,materialName.indexOf(":")).toUpperCase();
										}
										ItemStack iStack = new ItemStack(Material.getMaterial(materialName),amount);
										if(isEnchanted) {
											if(metaEnchanted != null) {
												iStack.setItemMeta(metaEnchanted);
											}
											else {
												iStack.addEnchantments(event.getCurrentItem().getEnchantments());
											}
										}
										else if(isPotion) {
											if(metaPotion != null) {
												iStack.setItemMeta(metaPotion);
											}
										}
										ItemMeta meta = iStack.getItemMeta();
										meta.setLore(lore);
										if(!displayName.equals("default")) {
											meta.setDisplayName(displayName);
										}
										Damageable damageMeta2 = (Damageable) meta;
										if(damage > 0) {
											damageMeta2.setDamage(damage);
											meta = (ItemMeta) damageMeta2;
										}
										iStack.setItemMeta(meta);
										if(inventoryContainsItems(inventoryplayer,iStack,amount) ) {
											if(!shopOwner.equals(playe.getName()) || !isPlayershop) {
												if(!isPlayershop || (isPlayershop && PaymentUtils.playerHasEnoughtMoney(playerFile, shopOwner, sellprice))) {
													playerFile = PaymentUtils.increasePlayerAmount(playerFile, playe.getName(), sellprice);
													//only playershop
													if(isPlayershop) {
														playerFile = PaymentUtils.decreasePlayerAmount(playerFile, shopOwner, sellprice, false);
													}
													if(amount > 1) {
														playe.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " " + ChatColor.GOLD + "Items" + ChatColor.GOLD + " were sold for " + ChatColor.GREEN + sellprice + ChatColor.GREEN + "$"+ ChatColor.GOLD + ".");
													}
													else {
														playe.sendMessage(ChatColor.GREEN + String.valueOf(amount) + " " + ChatColor.GOLD + "Item" + ChatColor.GOLD + " was sold for " + ChatColor.GREEN + sellprice + ChatColor.GREEN + "$"+ ChatColor.GOLD + ".");
													}
												}
												//only playershop
												else if(isPlayershop){
													playe.sendMessage(ChatColor.RED + "The owner has not enough money to buy your items!");
												}
											}
											//only playershop
											else if(isPlayershop) {
												if(amount > 1) {
													playe.sendMessage(ChatColor.GOLD + "You added " + ChatColor.GREEN + String.valueOf(amount) + ChatColor.GOLD + " Items to your shop.");
												}
												else {
													playe.sendMessage(ChatColor.GOLD + "You added " + ChatColor.GREEN + String.valueOf(amount) + ChatColor.GOLD + " Item to your shop.");
												}
											}
											removeItemFromInventory(inventoryplayer,iStack,amount);
											//only playershop
											if(isPlayershop) {
												playerShop.increaseStock(s, amount);
												playerShop.refreshStockpile();
											}
											break;
										}
									}
									else if(clickType == ClickType.SHIFT_RIGHT && sellprice != 0.0 || clickType == ClickType.SHIFT_RIGHT && playe.getName().equals(shopOwner) && inventoryplayer.containsAtLeast(new ItemStack(Material.getMaterial(materialName), 1), amount)) {
										String displayName = "default";
										if(materialName.contains("|")) {
											displayName = materialName.substring(0,materialName.indexOf("|"));
											materialName = materialName.substring(materialName.indexOf("|")+1);
										}
										boolean isEnchanted = false;
										boolean isPotion = false;
										if(materialName.contains("#Enchanted_")) {
											isEnchanted = true;
											materialName = materialName.substring(0,materialName.indexOf("#")).toUpperCase();
										}
										if(materialName.contains("potion:")) {
											isPotion = true;
											materialName = materialName.substring(0,materialName.indexOf(":")).toUpperCase();
										}
										ItemStack iStack = new ItemStack(Material.getMaterial(materialName));
										if(isEnchanted) {
											if(metaEnchanted != null) {
												iStack.setItemMeta(metaEnchanted);
											}
											else {
												iStack.addEnchantments(event.getCurrentItem().getEnchantments());
											}
										}
										else if(isPotion) {
											if(metaPotion != null) {
												iStack.setItemMeta(metaPotion);
											}
										}
										ItemMeta meta = iStack.getItemMeta();
										meta.setLore(lore);
										if(!displayName.equals("default")) {
											meta.setDisplayName(displayName);
										}
										if(damage > 0) {
											Damageable damageMeta2 = (Damageable) meta;
											damageMeta2.setDamage(damage);
											meta = (ItemMeta) damageMeta2;
										}
										iStack.setItemMeta(meta);
										if(inventoryContainsItems(inventoryplayer,iStack,1) ) {
											ItemStack[] i = inventoryplayer.getContents();
											int itemAmount = 0;
											double iA = 0.0;
											double newprice = 0;
											Repairable repairable = (Repairable) iStack.getItemMeta();
											if(repairable != null) {
												repairable.setRepairCost(0);
												iStack.setItemMeta((ItemMeta) repairable);
											}
											for(ItemStack is1 : i){
												if(is1 != null) {
													ItemStack is = new ItemStack(is1);
													Repairable repairable2 = (Repairable) is.getItemMeta();
													if(repairable2 != null) {
														repairable2.setRepairCost(0);
														is.setItemMeta((ItemMeta) repairable2);
													}
													iStack.setAmount(is.getAmount());
													if(is.toString().equals(iStack.toString())) {
														itemAmount = itemAmount + is.getAmount();
													}
												}
											}
											iA = Double.valueOf(String.valueOf(itemAmount));
											newprice = sellprice/amount * iA;
											if(!shopOwner.equals(playe.getName()) || !isPlayershop) {
												if(config.getDouble(shopOwner + ".account amount") >= newprice && isPlayershop || !isPlayershop) {
													if(itemAmount > 1) {
														playe.sendMessage(ChatColor.GREEN + String.valueOf(itemAmount) + " " + ChatColor.GOLD + "Items" + ChatColor.GOLD + " were sold for " + ChatColor.GREEN + newprice + ChatColor.GREEN + "$"+ ChatColor.GOLD + ".");
													}
													else {
														playe.sendMessage(ChatColor.GREEN + String.valueOf(itemAmount) + " " + ChatColor.GOLD + "Item" + ChatColor.GOLD + " was sold for " + ChatColor.GREEN + newprice + ChatColor.GREEN + "$"+ ChatColor.GOLD + ".");
													}
													playerFile = PaymentUtils.increasePlayerAmount(playerFile, playe.getName(), newprice);
													//only playershop
													if(isPlayershop) {
														playerFile = PaymentUtils.decreasePlayerAmount(playerFile, shopOwner, newprice, false);
													}
												}
												//only playershop
												else if(isPlayershop) {
												playe.sendMessage(ChatColor.RED + "The owner has not enough money to buy your items!");
												}
											}
											//only playershop
											else if(isPlayershop) {
												if(itemAmount > 1) {
													playe.sendMessage(ChatColor.GOLD + "You added " + ChatColor.GREEN + String.valueOf(itemAmount) + ChatColor.GOLD + " Items to your shop.");
												}
												else {
													playe.sendMessage(ChatColor.GOLD + "You added " + ChatColor.GREEN + String.valueOf(itemAmount) + ChatColor.GOLD + " Item to your shop.");
												}
											}
											iStack.setAmount(itemAmount);
											removeItemFromInventory(inventoryplayer,iStack,itemAmount);
											//only playershop
											if(isPlayershop) {
												playerShop.increaseStock(s, itemAmount);
												playerShop.refreshStockpile();
											}
											break;
										}
									}
								} catch (PlayerDoesNotExistException
										| PlayerHasNotEnoughtMoneyException e) {
									Bukkit.getLogger().log(Level.WARNING,e.getMessage(),e);
								}
							}
						}										
					}
				}
			}
		}
	}
}