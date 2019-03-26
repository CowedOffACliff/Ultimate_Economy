package com.ue.exceptions.Town;

public class ChunkNotClaimedByThisTownException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public ChunkNotClaimedByThisTownException(String chunk) {
		super("The chunk " + chunk + " is not owned by this town!");
	}

}