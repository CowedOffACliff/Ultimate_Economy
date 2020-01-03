package com.ue.jobsystem.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

import com.ue.exceptions.JobSystemException;
import com.ue.exceptions.PlayerException;
import com.ue.jobsystem.impl.JobcenterImpl;
import com.ue.player.EconomyPlayer;

public class JobcenterController {
	
	private static List<Jobcenter> jobCenterList = new ArrayList<>();
	
	/**
	 * This method returns a jobcenter by it's name.
	 * 
	 * @param name
	 * @return JobCenter
	 * @throws JobSystemException
	 */
	public static Jobcenter getJobCenterByName(String name) throws JobSystemException {
		for (Jobcenter jobcenter : jobCenterList) {
			if (jobcenter.getName().equals(name)) {
				return jobcenter;
			}
		}
		throw new JobSystemException(JobSystemException.JOBCENTER_DOES_NOT_EXIST);
	}

	/**
	 * This method returns a namelist of all jobcenters.
	 * 
	 * @return List of Strings
	 */
	public static List<String> getJobCenterNameList() {
		List<String> jobCenterNames = new ArrayList<>();
		for (Jobcenter jobcenter : jobCenterList) {
			jobCenterNames.add(jobcenter.getName());
		}
		return jobCenterNames;
	}

	/**
	 * This method returns a list of all existing jobcenters.
	 * 
	 * @return List of JobCenters
	 */
	public static List<Jobcenter> getJobCenterList() {
		return jobCenterList;
	}

	/**
	 * This method should me used to delete a jobcenter.
	 * 
	 * @param name
	 * @throws JobSystemException
	 */
	public static void deleteJobCenter(String name) throws JobSystemException {
		Jobcenter jobcenter = getJobCenterByName(name);
		jobcenter.deleteJobCenter();
		List<String> jobList = jobcenter.getJobNameList();
		jobCenterList.remove(jobcenter);
		int i = 0;
		for (String jobName : jobList) {
			for (Jobcenter jobCenter2 : jobCenterList) {
				if (jobCenter2.hasJob(jobName)) {
					i++;
				}
			}
			if (i == 0) {
				for (EconomyPlayer ecoPlayer : EconomyPlayer.getAllEconomyPlayers()) {
					if (ecoPlayer.hasJob(jobName)) {
						try {
							ecoPlayer.removeJob(jobName);
						} catch (PlayerException e) {
						}
					}
				}
			}
		}
	}

	/**
	 * This method should be used to create a new jobcenter.
	 * 
	 * @param server
	 * @param dataFolder
	 * @param name
	 * @param spawnLocation
	 * @param size
	 * @throws JobSystemException
	 */
	public static void createJobCenter(Server server, File dataFolder, String name, Location spawnLocation, int size)
			throws JobSystemException {
		if (getJobCenterNameList().contains(name)) {
			throw new JobSystemException(JobSystemException.JOBCENTER_ALREADY_EXIST);
		} else if (size % 9 != 0) {
			throw new JobSystemException(JobSystemException.INVALID_INVENTORY_SIZE);
		} else {
			jobCenterList.add(new JobcenterImpl(server, dataFolder, name, spawnLocation, size));
		}
	}

	/**
	 * This method loads all jobcenters from the save files.
	 * 
	 * @param server
	 * @param fileConfig
	 * @param dataFolder
	 */
	public static void loadAllJobCenters(Server server, FileConfiguration fileConfig, File dataFolder) {
		for (String jobCenterName : fileConfig.getStringList("JobCenterNames")) {
			File file = new File(dataFolder, jobCenterName + "-JobCenter.yml");
			if (file.exists()) {
				jobCenterList.add(new JobcenterImpl(server, dataFolder, jobCenterName));
			} else {
				Bukkit.getLogger().log(Level.WARNING, JobSystemException.CANNOT_LOAD_JOBCENTER,
						new JobSystemException(JobSystemException.CANNOT_LOAD_JOBCENTER));
			}
		}
	}

	/**
	 * This method despawns all jobcenter villager.
	 */
	public static void despawnAllVillagers() {
		for (Jobcenter jobcenter : jobCenterList) {
			jobcenter.despawnVillager();
		}
	}
}
