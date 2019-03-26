package com.ue.exceptions.Town;

public class TownDoesNotExistException extends Exception{

	private static final long serialVersionUID = 1L;

	public TownDoesNotExistException(String town) {
		super("The town " + town + " does not exist in this townworld!");
	}
}