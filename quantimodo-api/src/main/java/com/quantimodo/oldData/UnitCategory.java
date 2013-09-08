package com.quantimodo.oldData;

/**
 * A <code>UnitCategory</code> is a category (like distance) of some units of measurement.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

// Class should be final to prevent subclasses with access-controlled instances.
public final class UnitCategory {
	private int id;
	private String name;
	private Unit canonicalUnit;
	
	private static final String WRITE_DENIED  = "Cannot change units of measurement.";
	private static final String CREATE_DENIED = "Cannot create units of measurement."; 
	
	/**
	 * Do not use the default constructor.
	 * 
	 * @throws UnsupportedOperationException  Whenever called
	 * @deprecated Do not use the default constructor.
	 * @exclude
	 */
	private UnitCategory() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	
	/**
	 * Constructs a possibly-modified copy of a <code>UnitCategory</code> from the database.
	 * 
	 * @param id             the ID of this <code>UnitCategory</code>, or <tt>0</tt> if it hasn't yet been inserted into the database
	 * @param name           the name of this <code>UnitCategory</code> (such as distance)
	 * @param canonicalUnit  the canonical <code>Unit</code> for conversions (conversions between units go from original unit &rarr; canonical unit &rarr; destination unit)
	 */
	/*package*/ UnitCategory(final int id, final String name, final Unit canonicalUnit) {
		this.id = id;
		this.name = name;
		this.canonicalUnit = canonicalUnit;
	}
	
	/**
	 * Constructs a <code>UnitCategory</code> from scratch (that is, it is not loaded from the database).
	 * 
	 * @param name           the name of this <code>UnitCategory</code> (such as distance)
	 * @param canonicalUnit  the canonical <code>Unit</code> for conversions (conversions between units go from original unit &rarr; canonical unit &rarr; destination unit)
	 * 
	 * @throws AuthorizationException  if the current user would not be able to use the given <code>Variable</code>
	 */
	public UnitCategory(final String name, final Unit canonicalUnit) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(CREATE_DENIED);
		this.id = 0;
		this.name = name;
		this.canonicalUnit = canonicalUnit;
	}
	
	/**
	 * Changes the name of this <code>UnitCategory</code>.
	 * 
	 * @param name  the new name of this <code>UnitCategory</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>UnitCategory</code>
	 */
	public void setName(final String name) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(WRITE_DENIED);
		this.name = name;
	}
	
	/**
	 * Changes the canonical <code>Unit</code> for conversions within this <code>UnitCategory</code>.
	 * WARNING: This doesn't change the unit conversions table.
	 * TODO: Make this change the unit conversions table.
	 * 
	 * @param canonicalUnit  the new canonical <code>Unit</code> for conversions within this <code>UnitCategory</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>UnitCategory</code>
	 */
	public void setCanonicalUnit(final Unit canonicalUnit) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(WRITE_DENIED);
		this.canonicalUnit = canonicalUnit;
	}
	
	/**  
	 * Returns the ID of this <code>UnitCategory</code>
	 * 
	 * @return the ID of this <code>UnitCategory</code>
	 */
	/*package*/ int getId() {
		return this.id;
	}
	
	/**
	 * Returns the name of this <code>UnitCategory</code>.
	 * 
	 * @return the name of this <code>UnitCategory</code> (such as kilometer)
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the canonical <code>Unit</code> for conversions within this <code>UnitCategory</code>.
	 * 
	 * @return the canonical <code>Unit</code> for conversions within this <code>UnitCategory</code>
	 */
	public Unit getCanonicalUnit() {
		return this.canonicalUnit;
	}
}
