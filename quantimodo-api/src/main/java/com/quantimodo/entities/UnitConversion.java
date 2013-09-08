package com.quantimodo.entities;


import javax.persistence.*;

@Entity
@Table(name = "`qm-unit-conversions`")

public final class UnitConversion {
	@EmbeddedId
	private UnitConversionPK id;

	@MapsId("unitId")
	@ManyToOne
	@JoinColumn(name = "unit")
	private Unit unit;

	@Column(name = "`step-number`", insertable = false, updatable = false)
	protected Integer stepNumber;


	@Column(name = "operation")
	private Integer operation;

	@Column(name = "value")
	private Long value;

	protected UnitConversion() {
	}

	public UnitConversion(Unit unit, Integer stepNumber, Integer operation, Long value) {
		this.id = new UnitConversionPK(unit.getId(), stepNumber);
		this.unit = unit;
		this.stepNumber = stepNumber;
		this.operation = operation;
		this.value = value;
	}

	public Integer getOperation() {
		return operation;
	}

	public Long getValue() {
		return value;
	}
}


