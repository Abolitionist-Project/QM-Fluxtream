package com.quantimodo.oldData;

import java.util.Date;

/**
 * A <code>Measurement</code> is a measurable attribute of a person, such as the amount of time they slept or the amount of aspirin they ingested.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

// Class should be final to prevent subclasses with access-controlled instances.
public final class Measurement {
	private final User user;
	
	private Variable variable;
	private MeasurementSource measurementSource;
	
	private int epochMinuteTimestamp;
	private double value;
	private Unit unit;
	
	private static final String READ_DENIED   = "Cannot see measurements that aren't yours.";
	private static final String WRITE_DENIED  = "Cannot change measurements that aren't yours.";
	private static final String CREATE_DENIED = "Cannot create measurements that won't be yours."; 
	
	/**
	 * Do not use the default constructor.
	 * 
	 * @throws UnsupportedOperationException  Whenever called
	 * @deprecated Do not use the default constructor.
	 * @exclude
	 */
	private Measurement() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	
	/**
	 * Constructs a possibly-modified copy of a <code>Measurement</code> from the database.
	 * 
	 * @param user                  owner of this <code>Measurement</code>
	 * @param variable              variable being measured
	 * @param measurementSource     the source of the <code>Measurement</code> (such as the application name)
	 * @param epochMinuteTimestamp  the time of the event being measured, in epoch minutes (count of minutes since midnight, 1st of January, 1970)
	 * @param value                 the value measured
	 * @param defaultUnit           the <code>Unit</code> the value was measured in (such as kilometers)
	 */
	/*package*/ Measurement(final User user,
	                        final Variable variable, final MeasurementSource measurementSource,
	                        final int epochMinuteTimestamp, final double value, final Unit unit) {
		this.user = user;
		this.variable = variable;
		this.measurementSource = measurementSource;
		this.epochMinuteTimestamp = epochMinuteTimestamp;
		this.value = value;
		this.unit = unit;
	}
	
	/**
	 * Constructs a <code>Measurement</code> from scratch (that is, it is not loaded from the database).
	 * 
	 * @param variable            variable being measured
	 * @param measurementSource   the source of the <code>Measurement</code> (such as the application name)
	 * @param timestamp           the time of the event being measured
	 * @param value               the value measured
	 * @param defaultUnit         the <code>Unit</code> the value was measured in (such as kilometers)
	 */
	public Measurement(final Variable variable, final MeasurementSource measurementSource,
	                   final Date timestamp, final double value, final Unit unit) {
		this(variable, measurementSource, timestamp.getTime(), value, unit);
	}
	
	/**
	 * Constructs a <code>Measurement</code> from scratch (that is, it is not loaded from the database).
	 * 
	 * TODO: variable access restrictions, unit is in the same category as the variable's default unit (for set methods as well)
	 * @param variable            variable being measured
	 * @param measurementSource   the source of the <code>Measurement</code> (such as the application name)
	 * @param timestamp           the time of the event being measured (count of milliseconds since midnight, 1st of January, 1970)
	 * @param value               the value measured
	 * @param defaultUnit         the <code>Unit</code> the value was measured in (such as kilometers)
	 */
	public Measurement(final Variable variable, final MeasurementSource measurementSource,
	                   final long timestamp, final double value, final Unit unit) {
		this.user = User.getCurrentUser();
		this.variable = variable;
		this.measurementSource = measurementSource;
		this.epochMinuteTimestamp = (int) (timestamp / 60000L);
		this.value = value;
		this.unit = unit;
	}
	
	/**
	 * Changes which <code>Variable</code> was considered to have been measured.
	 * 
	 * @param variable  the <code>Variable</code> this <code>Measurement</code> measures
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Measurement</code>
	 */
	public void setVariable(final Variable variable) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.variable = variable;
	}
	
	/**
	 * Changes which <code>MeasurementSource</code> this <code>Measurement</code> is considered to have come from.
	 * 
	 * @param measurementSource  the <code>MeasurementSource</code> this <code>Measurement</code> came from
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Measurement</code>
	 */
	public void setMeasurementSource(final MeasurementSource measurementSource) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.measurementSource = measurementSource;
	}
	
	/**
	 * Changes the timestamp of this <code>Measurement</code>.
	 * 
	 * @param timestamp  the time of the event being measured
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Measurement</code>
	 */
	public void setTimestamp(final Date timestamp) {
		setTimestamp(timestamp.getTime());
	}
	
	/**
	 * Changes the timestamp of this <code>Measurement</code>.
	 * 
	 * @param timestamp  the time of the event being measured (count of milliseconds since midnight, 1st of January, 1970)
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Measurement</code>
	 */
	public void setTimestamp(final long timestamp) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.epochMinuteTimestamp = (int) (timestamp / 60000L);
	}
	
	/**
	 * Changes the value of this <code>Variable</code>.
	 * 
	 * @param value  the new value of this <code>Measurement</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Measurement</code>
	 */
	public void setValue(final double value) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.value = value;
	}
	
	/**
	 * Changes the unit of measurement of the value for this <code>Variable</code>.
	 * 
	 * @param unit  the new <code>Unit</code> for this <code>Measurement</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Measurement</code>
	 */
	public void setUnit(final Unit unit) {
		if (!User.currentUserCanWrite(this.user)) throw new AuthorizationException(WRITE_DENIED);
		this.unit = unit;
	}
	
	/**  
	 * Returns the <code>User</code> who owns this <code>Measurement</code>.
	 * 
	 * @return the <code>User</code> who owns this <code>Measurement</code>
	 */
	/*package*/ User getUser() {
		return this.user;
	}
	
	/**
	 * Returns the <code>Variable</code> that was measured.
	 * 
	 * @return the <code>Variable</code> that was measured
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Measurement</code>
	 */
	public Variable getVariable() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return this.variable;
	}
	
	/**
	 * Returns the source of this <code>Measurement</code>.
	 * 
	 * @return the source of this <code>Measurement</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Measurement</code>
	 */
	public MeasurementSource getMeasurementSource() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return this.measurementSource;
	}
	
	/**
	 * Returns the time the event was measured.
	 * 
	 * @return the time the event was measured (count of milliseconds since midnight, 1st of January, 1970)
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Measurement</code>
	 */
	public long getTimestamp() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return this.epochMinuteTimestamp * 60000L;
	}
	
	/**
	 * Returns the value that was measured.
	 * 
	 * @return the value that was measured
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Measurement</code>
	 */
	public double getValue() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return this.value;
	}
	
	/**
	 * Returns the <code>Unit</code> this <code>Measurement</code> was made in.
	 * 
	 * @return the <code>Unit</code> this <code>Measurement</code> was made in
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to see this <code>Measurement</code>
	 */
	public Unit getUnit() {
		if (!User.currentUserCanRead(this.user)) throw new AuthorizationException(READ_DENIED);
		return this.unit;
	}
}
