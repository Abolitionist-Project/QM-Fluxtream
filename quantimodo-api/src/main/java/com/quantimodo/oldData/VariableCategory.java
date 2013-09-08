package com.quantimodo.oldData;

/**
 * A <code>VariableCategory</code> contains an entire category of <code>{@link Variable}</code>s.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

// Class should be final to prevent subclasses with access-controlled instances.
public final class VariableCategory {
	private int id;
	private String name;
	
	private static final String WRITE_DENIED  = "Cannot change variable categories that aren't yours.";
	private static final String CREATE_DENIED = "Cannot create variable categories that won't be yours."; 
	
	/**
	 * Do not use the default constructor.
	 * 
	 * @throws UnsupportedOperationException  Whenever called
	 * @deprecated Do not use the default constructor.
	 * @exclude
	 */
	private VariableCategory() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	
	/**
	 * Constructs a possibly-modified copy of a <code>VariableCategory</code> from the database.
	 * 
	 * @param id    ID of this <code>VariableCategory</code>
	 * @param name  name of this <code>VariableCategory</code>
	 */
	/*package*/ VariableCategory(final int id, final String name) {
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Constructs a <code>VariableCategory</code> from scratch (that is, it is not loaded from the database).
	 * 
	 * @param name  name of this <code>VariableCategory</code>
	 * 
	 * @throws AuthorizationException  if the current user would not be able to alter this <code>VariableCategory</code>
	 */
	public VariableCategory(final String name) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(CREATE_DENIED);
		this.id = 0;
		this.name = name;
	}
	
	/**
	 * Sets the ID of the <code>VariableCategory</code> when first writing it to the database.
	 * 
	 * @param id  the ID to use for this <code>VariableCategory</code>
	 */
	/*package*/ void setId(final int id) {
		if (this.id == 0) this.id = id;
	}
	
	/**
	 * Changes the name of this <code>VariableCategory</code>.
	 * 
	 * @param name  the name to use for this <code>VariableCategory</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>VariableCategory</code>
	 */
	public void setName(final String name) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(CREATE_DENIED);
		this.name = name;
	}
	
	/**
	 * Returns the ID of this <code>VariableCategory</code> or <tt>0</tt> if it has not been written to the database yet.
	 * 
	 * @return the ID of this <code>VariableCategory</code> or <tt>0</tt> if it has not been written to the database yet 
	 */
	/*package*/ int getID() {
		return id;
	}
	
	/**
	 * Returns the name of this <code>VariableCategory</code>.
	 * 
	 * @return the name of this <code>VariableCategory</code>
	 */
	public String getName() {
		return name;
	}
}
