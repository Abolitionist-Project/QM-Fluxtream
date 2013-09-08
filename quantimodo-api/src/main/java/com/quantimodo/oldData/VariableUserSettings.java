package com.quantimodo.oldData;

/**
 * A <code>VariableUserSettings</code> contains custom settings for a <code>{@link Variable}</code> for one <code>{@link User}</code>.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

// Class should be final to prevent subclasses with access-controlled instances.
public final class VariableUserSettings {
	private final User user;
	private final Variable variable;
	
	private Unit unit;
	
	private static final String READ_DENIED   = "Cannot see variables that aren't yours.";
	private static final String WRITE_DENIED  = "Cannot change variables that aren't yours.";
	private static final String CREATE_DENIED = "Cannot create variables that won't be yours."; 
	
	/**
	 * Do not use the default constructor.
	 * 
	 * @throws UnsupportedOperationException  Whenever called
	 * @deprecated Do not use the default constructor.
	 * @exclude
	 */
	private VariableUserSettings() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	
	/**
	 * Constructs a possibly-modified copy of a <code>VariableUserSettings</code> from the database.
	 * 
	 * @param user      the <code>User</code> who wants these custom settings
	 * @param variable  the <code>Variable</code> these custom settings apply to
	 * @param unit      the <code>Unit</code> in which measurements of this <code>Variable</code> should be displayed or <tt>null</tt> if no customization is wanted
	 */
	/*package*/ VariableUserSettings(final User user, final Variable variable, final Unit unit) {
		this.user = user;
		this.variable = variable;
		this.unit = unit;
	}
	
	/**
	 * Constructs a <code>VariableUserSettings</code> from scratch (that is, it is not loaded from the database).
	 * 
	 * @param variable  the <code>Variable</code> these custom settings apply to
	 * @param unit      the <code>Unit</code> in which measurements of this <code>Variable</code> should be displayed or <tt>null</tt> if no customization is wanted
	 * 
	 * @throws AuthorizationException  if the current user would not be able to use the given <code>Variable</code>
	 */
	public VariableUserSettings(final Variable variable, final Unit unit) {
		final User currentUser = User.getCurrentUser();
		if ((variable == null) || currentUser.canWrite(variable.getUser())) throw new AuthorizationException(CREATE_DENIED);
		this.user = currentUser;
		this.variable = variable;
		this.unit = unit;
	}
	
	/**
	 * Changes the <code>Unit</code> that measurements from this <code>Variable</code> should be displayed in.
	 * 
	 * @param unit  the <code>Unit</code> that measurements from this <code>Variable</code> should be displayed in
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>VariableUserSettings</code>
	 */
	public void setUnit(final Unit unit) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.unit = unit;
	}
	
	/**  
	 * Returns the <code>User</code> who wants these custom settings.
	 * 
	 * @return the <code>User</code> who wants these custom settings
	 */
	/*package*/ User getUser() {
		return this.user;
	}
	
	/**  
	 * Returns the <code>Variable</code> these custom settings apply to.
	 * 
	 * @return the <code>Variable</code> these custom settings apply to
	 */
	/*package*/ Variable getVariable() {
		return this.variable;
	}
	
	/**
	 * Returns the <code>Unit</code> in which to display the measurements of this <code>Variable</code> to the current user.
	 * 
	 * @return the <code>Unit</code> in which to display the data of this <code>Variable</code> to the current user
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Variable</code>
	 */
	public Unit getUnit() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return this.unit;
	}
}
