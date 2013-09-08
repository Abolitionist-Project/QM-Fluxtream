package com.quantimodo.oldData;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * A <code>Unit</code> is a unit of measurement, such as a kilometer.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

// Class should be final to prevent subclasses with access-controlled instances.
public final class Unit {
	public static enum Operation { ADD, MULTIPLY };
	
	/*package*/ static final Operation toOperation(final byte value) {
		switch (value) {
			case 0:
				return Operation.ADD;
			case 1:
				return Operation.MULTIPLY;
			default:
				return null;
		}
	}
	
	/*package*/ static final byte fromOperation(final Operation value) {
		switch (value) {
			case ADD:
				return 0;
			case MULTIPLY:
				return 1;
			default:
				return -1;
		}
	}
	
	private int id;
	private UnitCategory category;
	private String name;
	private String abbreviatedName;
	
	private Operation[] operations;
	private double[] operands;
	
	private static final String WRITE_DENIED  = "Cannot change units of measurement.";
	private static final String CREATE_DENIED = "Cannot create units of measurement."; 
	
	/**
	 * Do not use the default constructor.
	 * 
	 * @throws UnsupportedOperationException  Whenever called
	 * @deprecated Do not use the default constructor.
	 * @exclude
	 */
	private Unit() { throw new UnsupportedOperationException("Default constructor is invalid."); }
	
	/**
	 * Constructs a possibly-modified copy of a <code>Unit</code> from the database.
	 * 
	 * @param id               the ID of this <code>Unit</code>, or <tt>0</tt> if it hasn't yet been inserted into the database
	 * @param category         the <code>UnitCategory</code> this <code>Unit</code> is within (such as distance)
	 * @param name             the name of this <code>Unit</code> (such as kilometer)
	 * @param abbreviatedName  the abbreviated name of this <code>Unit</code> (such as km)
	 */
	/*package*/ Unit(final int id, final UnitCategory category, final String name, final String abbreviatedName,
	                 final Operation[] operations, final double[] operands) {
		this.id = id;
		this.category = category;
		this.name = name;
		this.abbreviatedName = abbreviatedName;
		this.operations = operations;
		this.operands = operands;
	}
	
	/**
	 * Constructs a <code>Unit</code> from scratch (that is, it is not loaded from the database).
	 * 
	 * @param category         the <code>UnitCategory</code> this <code>Unit</code> is within (such as distance)
	 * @param name             the name of this <code>Unit</code> (such as kilometer)
	 * @param abbreviatedName  the abbreviated name of this <code>Unit</code> (such as km)
	 * 
	 * @throws AuthorizationException  if the current user would not be able to use the given <code>Variable</code>
	 */
	public Unit(final UnitCategory category, final String name, final String abbreviatedName,
	            final Operation[] operations, final double[] operands) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(CREATE_DENIED);
		this.id = 0;
		this.category = category;
		this.name = name;
		this.abbreviatedName = abbreviatedName;
		this.operations = operations;
		this.operands = operands;
	}
	
	/**
	 * Converts a value from this <code>Unit</code> to the <code>UnitCategory</code>'s canonical <code>Unit</code>.
	 * 
	 * @param valueInThisUnit  the value when measured in this <code>Unit</code>
	 * 
	 * @return the value when measured in the canonical <code>Unit</code>.
	 */
	public double convertFrom(double valueInThisUnit) {
		for (int i = operations.length - 1; i >= 0; i--) {
			final double operand = operands[i];
			switch (operations[i]) {
				case ADD:      valueInThisUnit -= operand; break;
				case MULTIPLY: valueInThisUnit /= operand; break;
			}
		}
		return valueInThisUnit;
	}
	
	/**
	 * Converts a value from the <code>UnitCategory</code>'s canonical <code>Unit</code> to this <code>Unit</code>.
	 * 
	 * @param valueInCanonicalUnit  the value when measured in the canonical <code>Unit</code>
	 * 
	 * @return the value when measured in this <code>Unit</code>.
	 */
	public double convertTo(double valueInCanonicalUnit) {
		for (int i = 0; i < operations.length; i++) {
			final double operand = operands[i];
			switch (operations[i]) {
				case ADD:      valueInCanonicalUnit += operand; break;
				case MULTIPLY: valueInCanonicalUnit *= operand; break;
			}
		}
		return valueInCanonicalUnit;
	}
	
	/**
	 * Moves this <code>Unit</code> into a different <code>UnitCategory</code>.
	 * 
	 * @param category  the <code>UnitCategory</code> to move this <code>Unit</code> into
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Unit</code>
	 */
	public void setCategory(final UnitCategory category) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(WRITE_DENIED);
		this.category = category;
	}
	
	/**
	 * Changes the name of this <code>Unit</code>.
	 * 
	 * @param name  the new name of this <code>Unit</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Unit</code>
	 */
	public void setName(final String name) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(WRITE_DENIED);
		this.name = name;
	}
	
	/**
	 * Changes the abbreviated name of this <code>Unit</code>.
	 * 
	 * @param abbreviatedName  the new abbreviated name of this <code>Unit</code>
	 * 
	 * @throws AuthorizationException  if the current user is not authorized to change this <code>Unit</code>
	 */
	public void setAbbreviatedName(final String abbreviatedName) {
		if (!User.currentUserCanWrite(null)) throw new AuthorizationException(WRITE_DENIED);
		this.abbreviatedName = abbreviatedName;
	}
	
	/**  
	 * Returns the ID of this <code>Unit</code>
	 * 
	 * @return the ID of this <code>Unit</code>
	 */
	/*package*/ int getId() {
		return this.id;
	}
	
	/**
	 * Returns the <code>UnitCategory</code> that this <code>Unit</code> is within.
	 * 
	 * @return the <code>UnitCategory</code> (such as distance) that this <code>Unit</code> is within
	 */
	public UnitCategory getCategory() {
		return this.category;
	}
	
	/**
	 * Returns the name of this <code>Unit</code>.
	 * 
	 * @return the name of this <code>Unit</code> (such as kilometer)
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns the abbreviated name of this <code>Unit</code>.
	 * 
	 * @return the abbreviated name (such as km) of this <code>Unit</code>
	 */
	public String getAbbreviatedName() {
		return this.abbreviatedName;
	}
	
	/**
	 * Returns the count of conversion steps.
	 * 
	 * @return the count of conversion steps.
	 */
	public int getStepCount() {
		return this.operations.length;
	}
	
	/**
	 * Returns the <code>Operation</code> used during a conversion step.
	 * 
	 * @return the <code>Operation</code> used during a conversion step.
	 */
	public Operation getOperation(final int stepNumber) {
		if ((stepNumber < 0) && (stepNumber >= this.operations.length)) return null;
		return this.operations[stepNumber];
	}
	
	/**
	 * Returns the operand used during a conversion step.
	 * 
	 * @return the operand used during a conversion step.
	 */
	public double getOperand(final int stepNumber) {
		if ((stepNumber < 0) && (stepNumber >= this.operations.length)) return Double.NaN;
		return this.operands[stepNumber];
	}
}
