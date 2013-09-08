package com.quantimodo.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public final class UnitConversionPK implements Serializable {
	@Column(name = "unit", nullable = false)
	protected Long unitId;

	@Column(name = "`step-number`", nullable = false)
	protected Integer stepNumber;

	protected UnitConversionPK() {
	}

	public UnitConversionPK(Long unitId, Integer stepNumber) {
		this.unitId = unitId;
		this.stepNumber = stepNumber;
	}
	// TODO: equals, hashCode
}
