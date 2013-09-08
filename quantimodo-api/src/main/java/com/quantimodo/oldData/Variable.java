package com.quantimodo.oldData;

/**
 * A <code>Variable</code> is a measurable attribute of a person, such as the amount of time they slept or the amount of aspirin they ingested.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

// Class should be final to prevent subclasses with access-controlled instances.
public final class Variable {
	public static enum CombinationOperation { SUM, MEAN };
	
	/*package*/ static final CombinationOperation toOperation(final byte value) {
		switch (value) {
			case 0:
				return CombinationOperation.SUM;
			case 1:
				return CombinationOperation.MEAN;
			default:
				return null;
		}
	}
	
	/*package*/ static final byte fromOperation(final CombinationOperation value) {
		switch (value) {
			case SUM:
				return 0;
			case MEAN:
				return 1;
			default:
				return -1;
		}
	}
	
	private int id;
	private final User user;
	
	private String name;
	private VariableCategory category;
	private Unit defaultUnit;
	
	private CombinationOperation combinationOperation;
	
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
	private Variable() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	
	/**
	 * Constructs a possibly-modified copy of a <code>Variable</code> from the database.
	 * 
	 * @param id                    ID of this <code>Variable</code>
	 * @param user                  owner of this <code>Variable</code>
	 * @param name                  name of this <code>Variable</code>
	 * @param category              <code>VariableCategory</code> this <code>Variable</code> is in
	 * @param defaultUnit           default <code>Unit</code> to display this <code>Variable</code>'s data in
	 * @param combinationOperation  operation to use when combining neighboring measurements
	 */
	/*package*/ Variable(final int id, final User user,
	                     final String name, final VariableCategory category, final Unit defaultUnit,
	                     final CombinationOperation combinationOperation) {
		this.id = id;
		this.user = user;
		this.name = name;
		this.category = category;
		this.defaultUnit = defaultUnit;
		this.combinationOperation = combinationOperation;
	}
	
	/**
	 * Constructs a <code>Variable</code> from scratch (that is, it is not loaded from the database).
	 * 
	 * @param user                  owner of this <code>Variable</code>
	 * @param name                  name of this <code>Variable</code>
	 * @param category              <code>VariableCategory</code> this <code>Variable</code> is in
	 * @param defaultUnit           default <code>Unit</code> to display this <code>Variable</code>'s data in
	 * @param combinationOperation  operation to use when combining neighboring measurements
	 * 
	 * @throws AuthorizationException  if the current user would not be able to alter this <code>Variable</code> (no creating <code>Variable</code>s for others)
	 */
	public Variable(final boolean isGlobal,
	                final String name, final VariableCategory category, final Unit defaultUnit,
			final CombinationOperation combinationOperation) {
		final User currentUser = User.getCurrentUser();
		final User user = isGlobal ? null : currentUser;
		if (currentUser.canWrite(user)) throw new AuthorizationException(CREATE_DENIED);
		this.id = 0;
		this.user = user;
		this.name = name;
		this.category = category;
		this.defaultUnit = defaultUnit;
		this.combinationOperation = combinationOperation;
	}
	
	/**
	 * Sets the ID of the <code>Variable</code> when first writing it to the database.
	 * 
	 * @param id  the ID to use for this <code>Variable</code>
	 */
	/*package*/ void setId(final int id) {
		if (this.id == 0) this.id = id;
	}
	
	/**
	 * Changes the name of this <code>Variable</code>.
	 * 
	 * @param name  the name to use for this <code>Variable</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Variable</code>
	 */
	public void setName(final String name) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.name = name;
	}
	
	/**
	 * Moves this <code>Variable</code> into a different <code>VariableCategory</code>.
	 * 
	 * @param category  the <code>VariableCategory</code> to put this <code>Variable</code> into
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Variable</code>
	 */
	public void setCategory(final VariableCategory category) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.category = category;
	}
	
	/**
	 * Chooses the default (but user-overrideable) <code>Unit</code> in which to display the measurements in this <code>Variable</code>.
	 * 
	 * @param defaultUnit  the default <code>Unit</code> in which to display the data of this <code>Variable</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Variable</code>
	 */
	public void setDefaultUnit(final Unit defaultUnit) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.defaultUnit = defaultUnit;
	}
	
	/**
	 * Chooses how neighboring measurements in this <code>Variable</code> should be combined when 'zooming out'.
	 * 
	 * @param combinationOperation  the method to use to combine neighboring measurements in this <code>Variable</code> when 'zooming out'
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Variable</code>
	 */
	public void setCombinationOperation(final CombinationOperation combinationOperation) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.combinationOperation = combinationOperation;
	}
	
	/**
	 * Returns the ID of this <code>Variable</code> or <tt>0</tt> if it has not been written to the database yet.
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Variable</code>
	 */
	/*package*/ int getID() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return id;
	}
	
	/**  
	 * Returns the <code>User</code> who owns this <code>Variable</code>.
	 * 
	 * @return the <code>User</code> who owns this <code>Variable</code>
	 */
	/*package*/ User getUser() {
		return this.user;
	}
	
	/**
	 * Returns the name of this <code>Variable</code>.
	 * 
	 * @return the name of this <code>Variable</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Variable</code>
	 */
	public String getName() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return name;
	}
	
	/**
	 * Returns which <code>VariableCategory</code> this <code>Variable</code> is within.
	 * 
	 * @return which <code>VariableCategory</code> this <code>Variable</code> is within
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Variable</code>
	 */
	// Gets the variable category.
	// Can only be done if the current user is allowed to see the variable.
	public VariableCategory getCategory() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return category;
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
		final User currentUser = User.getCurrentUser();
		if (currentUser != null) {
			final VariableUserSettings settings = DAO.instance.getVariableUserSettings(currentUser, this);
			if (settings != null) {
				final Unit unit = settings.getUnit();
				if (unit != null) return unit;
			}
		}
		return this.defaultUnit;
	}
	
	/**
	 * Returns how neighboring measurements in this <code>Variable</code> should be combined when 'zooming out'.
	 * 
	 * @return the method to use to combine neighboring measurements in this <code>Variable</code> when 'zooming out'
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Variable</code>
	 */
	public CombinationOperation getCombinationOperation() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return combinationOperation;
	}
}
