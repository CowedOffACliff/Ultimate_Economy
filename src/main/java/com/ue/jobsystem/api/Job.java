package com.ue.jobsystem.api;

import java.util.List;

import com.ue.exceptions.GeneralEconomyException;
import com.ue.exceptions.JobSystemException;

public interface Job {

    /**
     * This method removes a mob from a job.
     * 
     * @param entity
     * @throws JobSystemException
     * @throws GeneralEconomyException
     */
    public void deleteMob(String entity) throws JobSystemException, GeneralEconomyException;

    /**
     * This method removes a item from a job.
     * 
     * @param material
     * @throws JobSystemException
     * @throws GeneralEconomyException
     */
    public void deleteItem(String material) throws JobSystemException, GeneralEconomyException;

    /**
     * This method removes a loottype from this job. The loottype is for a
     * fisherJob. It should be "treasure", "junk" or "fish".
     * 
     * @param lootType
     * @throws JobSystemException
     * @throws GeneralEconomyException
     */
    public void delFisherLootType(String lootType) throws JobSystemException, GeneralEconomyException;

    /**
     * This method adds a loottype with a price to this job. The loottype is for a
     * fisherJob. It should be "treasure", "junk" or "fish".
     * 
     * @param lootType
     * @param price
     * @throws JobSystemException
     * @throws GeneralEconomyException
     */
    public void addFisherLootType(String lootType, double price) throws JobSystemException, GeneralEconomyException;

    /**
     * This method adds a mob to a job.
     * 
     * @param entity
     * @param price
     * @throws JobSystemException
     * @throws GeneralEconomyException
     */
    public void addMob(String entity, double price) throws JobSystemException, GeneralEconomyException;

    /**
     * This method adds a item to a job.
     * 
     * @param material
     * @param price
     * @throws JobSystemException
     * @throws GeneralEconomyException
     */
    public void addItem(String material, double price) throws JobSystemException, GeneralEconomyException;

    /**
     * This method deletes the job saveFile.
     */
    public void deleteJob();

    /**
     * This method returns the name of this job.
     * 
     * @return String
     */
    public String getName();

    /**
     * This method returns the price of a item in this job.
     * 
     * @param material
     * @return double
     * @throws JobSystemException
     * @throws GeneralEconomyException
     */
    public double getItemPrice(String material) throws JobSystemException, GeneralEconomyException;

    /**
     * This method returns the price of a fisher lootType.
     * 
     * @param lootType
     * @return double
     * @throws JobSystemException
     * @throws GeneralEconomyException
     */
    public double getFisherPrice(String lootType) throws JobSystemException, GeneralEconomyException;

    /**
     * This method returns the price for killing a entity.
     * 
     * @param entityName
     * @return double
     * @throws JobSystemException
     * @throws GeneralEconomyException
     */
    public double getKillPrice(String entityName) throws JobSystemException, GeneralEconomyException;

    /**
     * This method returns the fisherList.
     * 
     * @return List of Strings
     */
    public List<String> getFisherList();

    /**
     * This method returns the entityList.
     * 
     * @return List of Strings
     */
    public List<String> getEntityList();

    /**
     * This method returns the itemList.
     * 
     * @return List of Strings
     */
    public List<String> getItemList();

}
