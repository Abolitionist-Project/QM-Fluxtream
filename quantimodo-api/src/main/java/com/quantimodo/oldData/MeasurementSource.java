package com.quantimodo.oldData;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A <code>MeasurementSource</code> contains an entire category of <code>{@link Variable}</code>s.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

// Class should be final to prevent subclasses with access-controlled instances.
public final class MeasurementSource {
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
	private MeasurementSource() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	
	/**
	 * Constructs a possibly-modified copy of a <code>MeasurementSource</code> from the database.
	 * 
	 * @param id    ID of this <code>MeasurementSource</code>
	 * @param name  name of this <code>MeasurementSource</code>
	 */
	/*package*/ MeasurementSource(final int id, final String name) {
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Constructs a <code>MeasurementSource</code> from scratch (that is, it is not loaded from the database).
	 * 
	 * @param name  name of this <code>MeasurementSource</code>
	 * 
	 * @throws AuthorizationException  if the current user would not be able to alter this <code>MeasurementSource</code>
	 */
	public MeasurementSource(final String name) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(CREATE_DENIED);
		this.id = 0;
		this.name = name;
	}
	
	/**
	 * Sets the ID of the <code>MeasurementSource</code> when first writing it to the database.
	 * 
	 * @param id  the ID to use for this <code>MeasurementSource</code>
	 */
	/*package*/ void setId(final int id) {
		if (this.id == 0) this.id = id;
	}
	
	/**
	 * Changes the name of this <code>MeasurementSource</code>.
	 * 
	 * @param name  the name to use for this <code>MeasurementSource</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>MeasurementSource</code>
	 */
	public void setName(final String name) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(CREATE_DENIED);
		this.name = name;
	}
	
	/**
	 * Returns the ID of this <code>MeasurementSource</code> or <tt>0</tt> if it has not been written to the database yet.
	 * 
	 * @param id  the ID to use for this <code>MeasurementSource</code> or <tt>0</tt> if it has not been written to the database yet
	 */
	/*package*/ int getID() {
		return id;
	}
	
	/**
	 * Returns the name of this <code>MeasurementSource</code>.
	 * 
	 * @return the name of this <code>MeasurementSource</code>
	 */
	public String getName() {
		return name;
	}
}
