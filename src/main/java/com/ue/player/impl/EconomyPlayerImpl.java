package com.ue.player.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.ue.exceptions.JobSystemException;
import com.ue.exceptions.PlayerException;
import com.ue.exceptions.PlayerExceptionMessageEnum;
import com.ue.jobsystem.api.Job;
import com.ue.jobsystem.api.JobController;
import com.ue.language.MessageWrapper;
import com.ue.player.api.EconomyPlayer;
import com.ue.player.api.EconomyPlayerController;
import com.ue.ultimate_economy.Ultimate_Economy;

public class EconomyPlayerImpl implements EconomyPlayer {

	private Map<String, Location> homes;
	private double account;
	private Player player;
	private String name;
	private List<Job> jobs;
	private List<String> joinedTowns;
	private boolean scoreBoardDisabled;
	private BossBar bossBar;

	/**
	 * Constructor for creating a new economyPlayer/loading an existing player.
	 * 
	 * @param name
	 * @param isNew
	 */
	public EconomyPlayerImpl(String name,boolean isNew) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
		jobs = new ArrayList<>();
		homes = new HashMap<>();
		joinedTowns = new ArrayList<>();
		this.player = Bukkit.getPlayer(name);
		this.name = name;
		if (isNew) {
			scoreBoardDisabled = true;
			account = 0.0;
			config.set(player + ".bank", scoreBoardDisabled);
			config.set(player + ".account amount", account);
			save(config);
		} else {
			scoreBoardDisabled = config.getBoolean(name + ".bank");
			account = config.getDouble(name + ".account amount");
			List<String> jobNames = config.getStringList(name + ".Jobs");
			for(String jobName:jobNames) {
				try {
					jobs.add(JobController.getJobByName(jobName));
				} catch (JobSystemException e) {
					Bukkit.getLogger().warning("[Ultimate_Economy] " + MessageWrapper.getErrorString("job_does_not_exist") + ":" + jobName);
				}
			}
			joinedTowns = config.getStringList(name + ".joinedTowns");
			List<String> homeNameList = config.getStringList(name + ".Home.Homelist");
			for (String homeName : homeNameList) {
				Location homeLocation = new Location(
						Bukkit.getWorld(config.getString(name + ".Home." + homeName + ".World")),
						config.getDouble(name + ".Home." + homeName + ".X"),
						config.getDouble(name + ".Home." + homeName + ".Y"),
						config.getDouble(name + ".Home." + homeName + ".Z"));
				homes.put(homeName, homeLocation);
			}
		}
		bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
		bossBar.setVisible(false);
	}
	
	public boolean isOnline() {
		if(player == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public BossBar getBossBar() {
		return bossBar;
	}

	public String getName() {
		return name;
	}

	public void joinJob(Job job,boolean sendMessage) throws PlayerException, JobSystemException {
		if (reachedMaxJoinedJobs()) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.MAX_REACHED);
		} else if (jobs.contains(job)) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.JOB_ALREADY_JOINED);
		} else {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
			jobs.add(job);
			List<String> jobList = config.getStringList(name + ".Jobs");
			jobList.add(job.getName());
			config.set(name + ".Jobs", jobList);
			save(config);
			if(sendMessage && isOnline()) {
				player.sendMessage(MessageWrapper.getString("job_join", job.getName()));
			}
		}
	}

	public void leaveJob(Job job,boolean sendMessage) throws PlayerException, JobSystemException {
		if (!jobs.contains(job)) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.JOB_NOT_JOINED);
		} else {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
			jobs.remove(job);
			List<String> jobList = config.getStringList(name + ".Jobs");
			jobList.remove(job.getName());
			config.set(name + ".Jobs", jobs);
			save(config);
			if(isOnline() && sendMessage) {
				player.sendMessage(MessageWrapper.getString("job_left", job.getName()));
			}
		}
	}

	public boolean hasJob(Job job) throws JobSystemException {
		if (jobs.contains(job)) {
			return true;
		} else {
			return false;
		}
	}

	public List<Job> getJobList() {
		return jobs;
	}

	public Location getHome(String homeName) throws PlayerException {
		if (homes.containsKey(homeName)) {
			return homes.get(homeName);
		} else {
			throw PlayerException.getException(PlayerExceptionMessageEnum.HOME_DOES_NOT_EXIST);
		}
	}

	public Map<String, Location> getHomeList() {
		return homes;
	}

	public void addJoinedTown(String townName) throws PlayerException {
		if (joinedTowns.contains(townName)) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.TOWN_ALREADY_JOINED);
		} else if (reachedMaxJoinedTowns()) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.MAX_REACHED);
		} else {
			FileConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
			joinedTowns.add(townName);
			config.set(name + ".joinedTowns", joinedTowns);
			save(config);

		}
	}

	public void removeJoinedTown(String townName) throws PlayerException {
		if (joinedTowns.contains(townName)) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
			joinedTowns.remove(townName);
			config.set(name + ".joinedTowns", joinedTowns);
			save(config);
		} else {
			throw PlayerException.getException(PlayerExceptionMessageEnum.TOWN_NOT_JOINED);
		}
	}

	public List<String> getJoinedTownList() {
		return joinedTowns;
	}

	public boolean reachedMaxJoinedTowns() {
		if (EconomyPlayerController.getMaxJoinedTowns() <= joinedTowns.size()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method return true if this player reached the max number of homes.
	 * 
	 * @return boolean
	 */
	public boolean reachedMaxHomes() {
		if (EconomyPlayerController.getMaxHomes() <= homes.size()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method return true if this player reached the max number of joined jobs.
	 * 
	 * @return boolean
	 */
	public boolean reachedMaxJoinedJobs() {
		if (EconomyPlayerController.getMaxJobs() <= jobs.size()) {
			return true;
		} else {
			return false;
		}
	}

	public void addHome(String homeName, Location location, boolean sendMessage) throws PlayerException {
		if (reachedMaxHomes()) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.MAX_REACHED);
		} else if (homes.containsKey(homeName)) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.HOME_ALREADY_EXIST);
		} else {
			homes.put(homeName, location);
			YamlConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
			List<String> homeNameList = config.getStringList(name + ".Home.Homelist");
			homeNameList.add(homeName);
			config.set(name + ".Home.Homelist", homeNameList);
			config.set(name + ".Home." + homeName + ".Name", homeName);
			config.set(name + ".Home." + homeName + ".World", location.getWorld().getName());
			config.set(name + ".Home." + homeName + ".X", location.getX());
			config.set(name + ".Home." + homeName + ".Y", location.getY());
			config.set(name + ".Home." + homeName + ".Z", location.getZ());
			save(config);
			if(isOnline() && sendMessage) {
				player.sendMessage(MessageWrapper.getString("sethome", homeName));
			}
		}
	}

	public void removeHome(String homeName, boolean sendMessage) throws PlayerException {
		if (homes.containsKey(homeName)) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
			List<String> homeNameList = config.getStringList(name + ".Home.Homelist");
			homes.remove(homeName);
			homeNameList.remove(homeName);
			config.set(name + ".Home." + homeName, null);
			config.set(name + ".Home.Homelist", homeNameList);
			save(config);
			if(isOnline() && sendMessage) {
				player.sendMessage(MessageWrapper.getString("delhome", homeName));
			}
		} else {
			throw PlayerException.getException(PlayerExceptionMessageEnum.HOME_DOES_NOT_EXIST);
		}
	}

	/**
	 * This method returns true if the bank scoreboard is disabled.
	 * 
	 * @return
	 */
	public boolean isScoreBoardDisabled() {
		return scoreBoardDisabled;
	}

	public void setScoreBoardDisabled(boolean scoreBoardDisabled) {
		this.scoreBoardDisabled = scoreBoardDisabled;
		YamlConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
		config.set(name + ".bank", scoreBoardDisabled);
		if(scoreBoardDisabled) {
			Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
			if(isOnline()) {
				player.setScoreboard(board);
			}
		} else {
			new UpdateScoreboardRunnable().runTask(Ultimate_Economy.getInstance);
		}
	}

	public boolean hasEnoughtMoney(double amount) {
		if (account >= amount) {
			return true;
		} else {
			return false;
		}
	}

	public void payToOtherPlayer(EconomyPlayer reciever, double amount,boolean sendMessage) throws PlayerException {
		if (amount < 0) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.INVALID_PARAMETER, amount);
		} else if (hasEnoughtMoney(amount)) {
			reciever.increasePlayerAmount(amount, false);
			
			decreasePlayerAmount(amount, true);
			if (reciever.isOnline() && sendMessage) {
				reciever.getPlayer().sendMessage(MessageWrapper.getString("got_money_with_sender", amount,player.getName()));
			}
			if(isOnline() && sendMessage) {
				player.sendMessage(MessageWrapper.getString("gave_money", reciever.getName(),amount));
			}
		}
	}

	public void increasePlayerAmount(double amount,boolean sendMessage) throws PlayerException {
		if (amount < 0) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.INVALID_PARAMETER, amount);
		} else {
			FileConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
			account += amount;
			config.set(name + ".account amount", account);
			save(config);
			if (isOnline()) {
				new UpdateScoreboardRunnable().runTask(Ultimate_Economy.getInstance);
				if(sendMessage) {
					player.sendMessage(MessageWrapper.getString("got_money", amount));
				}
			}
		}
	}

	public void decreasePlayerAmount(double amount, boolean personal) throws PlayerException {
		if (amount < 0) {
			throw PlayerException.getException(PlayerExceptionMessageEnum.INVALID_PARAMETER, amount);
		} else if (hasEnoughtMoney(amount)) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(EconomyPlayerController.getPlayerFile());
			account -= amount;
			config.set(name + ".account amount", account);
			save(config);
			if (isOnline()) {
				new UpdateScoreboardRunnable().runTask(Ultimate_Economy.getInstance);
			}
		} else {
			if (personal) {
				throw PlayerException.getException(PlayerExceptionMessageEnum.NOT_ENOUGH_MONEY_PERSONAL);
			} else {
				throw PlayerException.getException(PlayerExceptionMessageEnum.NOT_ENOUGH_MONEY_NON_PERSONAL);
			}
		}
	}

	public double getBankAmount() {
		return account;
	}

	/**
	 * Set the bank scoreboard of this player.
	 * 
	 * @param file
	 * @param p
	 * @param score
	 */
	private void setScoreboard(Player p, int score) {
		if (!scoreBoardDisabled) {
			Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
			Objective o = board.registerNewObjective("bank", "dummy", MessageWrapper.getString("bank"));
			o.setDisplaySlot(DisplaySlot.SIDEBAR);
			o.getScore(ChatColor.GOLD + MessageWrapper.getString("money_info", "")).setScore(score);
			p.setScoreboard(board);
		}
	}

	public void updateScoreBoard() {
		int score = (int) account;
		if(isOnline()) {
			setScoreboard(player, score);
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		bossBar.removeAll();
		this.player = player;
		bossBar.addPlayer(player);
	}

	private void save(FileConfiguration config) {
		try {
			config.save(EconomyPlayerController.getPlayerFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class UpdateScoreboardRunnable extends BukkitRunnable {

		@Override
		public void run() {
			updateScoreBoard();
		}
	}

	@Override
	public void addWildernessPermission() {
		player.addAttachment(Ultimate_Economy.getInstance).setPermission("ultimate_economy.wilderness", true);
	}

	@Override
	public void denyWildernessPermission() {
		player.addAttachment(Ultimate_Economy.getInstance).setPermission("ultimate_economy.wilderness", false);
	}
}


