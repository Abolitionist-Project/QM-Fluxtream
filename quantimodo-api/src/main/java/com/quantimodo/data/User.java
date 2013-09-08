package com.quantimodo.data;

public class User {
	private final long userID;
	private final boolean isAdmin; // storing this rather than querying the user rights table each time is a small security risk
	
	public User(final long userID, final boolean isAdmin) {
		this.userID = userID;
		this.isAdmin = isAdmin;
	}
	
	public long getUserID() { return userID; }
	public boolean isAdmin() { return isAdmin; }
}
