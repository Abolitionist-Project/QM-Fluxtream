package com.quantimodo.oldData;

/**
 * A <code>User</code> is a user account.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

// Class should be final to prevent subclasses with access-controlled instances.
public final class User {
	private final long id;
	private boolean isAdmin;
	
	private static final String WRITE_DENIED  = "Cannot change user information.";
	
	/**
	 * Returns the logged-in <code>User</code> or <tt>null</tt> if not logged in.
	 * 
	 * @return the logged-in <code>User</code> or <tt>null</tt> if not logged in.
	 */
	public static User getCurrentUser() {
		return null;
	}
	
	/**
	 * Determines whether the logged-in <code>User</code> is permitted to read an item.
	 * 
	 * @param itemOwner  the <code>User</code> that owns the item or <tt>null</tt> if the item is for all users
	 * 
	 * @return <tt>true</tt> iff the logged-in <code>User</code> is permitted to read the item.
	 */
	public static boolean currentUserCanRead(final User itemOwner) {
		return canRead(getCurrentUser(), itemOwner);
	}
	
	/**
	 * Determines whether the logged-in <code>User</code> is permitted to write to an item.
	 * 
	 * @param itemOwner  the <code>User</code> that owns the item or <tt>null</tt> if the item is for all users
	 * 
	 * @return <tt>true</tt> iff the logged-in <code>User</code> is permitted to write to the item.
	 */
	public static boolean currentUserCanWrite(final User itemOwner) {
		return canWrite(getCurrentUser(), itemOwner);
	}
	
	/**
	 * Determines whether the accessor is permitted to read an item.
	 * 
	 * @param accessor   the <code>User</code> attempting to read the item
	 * @param itemOwner  the <code>User</code> that owns the item or <tt>null</tt> if the item is for all users
	 * 
	 * @return <tt>true</tt> iff the accessor is permitted to read the item.
	 */
	private static boolean canRead(final User accessor, final User itemOwner) {
		return (itemOwner == null) || ((accessor != null) && accessor.equals(itemOwner));
	}
	
	/**
	 * Determines whether the accessor is permitted to write to an item.
	 * 
	 * @param accessor   the <code>User</code> attempting to write to the item
	 * @param itemOwner  the <code>User</code> that owns the item or <tt>null</tt> if the item is for all users
	 * 
	 * @return <tt>true</tt> iff the accessor is permitted to write to the item.
	 */
	private static boolean canWrite(final User accessor, final User itemOwner) {
		return (accessor != null) && (itemOwner == null ? accessor.isAdmin : itemOwner.equals(accessor));
	}
	
	/**
	 * Do not use the default constructor.
	 * 
	 * @throws UnsupportedOperationException  Whenever called
	 * @deprecated Do not use the default constructor.
	 * @exclude
	 */
	private User() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	
	
	/**
	 * Constructs a possibly-modified copy of a <code>User</code> from the database.
	 * 
	 * @param id       the ID of this <code>User</code>
	 * @param isAdmin  whether this <code>User</code> is an administrator
	 */
	/*package*/ User(final long id, final boolean isAdmin) {
		this.id = id;
		this.isAdmin = isAdmin;
	}
	
	/**
	 * Returns the ID of this <code>User</code>.
	 * 
	 * @return the ID of this <code>User</code>
	 */
	/*package*/ long getID() {
		return id;
	}
	
	/**
	 * Sets whether this <code>User</code> is an administrator.
	 * 
	 * @param isAdmin  whether this <code>User</code> will be administrator afterwards
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>User</code>
	 */
	public void setAdmin(final boolean isAdmin) {
		if (!getCurrentUser().isAdmin) throw new AuthorizationException(WRITE_DENIED);
		this.isAdmin = isAdmin;
	}
	
	/**
	 * Determines whether this <code>User</code> is permitted to read an item.
	 * 
	 * @param itemOwner  the <code>User</code> that owns the item or <tt>null</tt> if the item is for all users
	 * 
	 * @return <tt>true</tt> iff this <code>User</code> is permitted to read the item.
	 */
	public boolean canRead(final User itemOwner) {
		return canRead(this, itemOwner);
	}
	
	/**
	 * Determines whether this <code>User</code> is permitted to write to an item.
	 * 
	 * @param itemOwner  the <code>User</code> that owns the item or <tt>null</tt> if the item is for all users
	 * 
	 * @return <tt>true</tt> iff this <code>User</code> is permitted to write to the item.
	 */
	public boolean canWrite(final User itemOwner) {
		return canWrite(this, itemOwner);
	}
}
