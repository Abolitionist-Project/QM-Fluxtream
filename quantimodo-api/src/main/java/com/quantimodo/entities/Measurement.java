package com.quantimodo.entities;

import javax.persistence.*;


@Entity
@Table(name = "`qm-measurements`")
public final class Measurement {
	@EmbeddedId
	private MeasurementPK id;

	@Column(name = "user", nullable = false, insertable = false, updatable = false)
	private Long userId;

	@MapsId("variableId")
	@ManyToOne
	@JoinColumn(name = "variable")
	private Variable variable;

	@MapsId("sourceId")
	@ManyToOne
	@JoinColumn(name = "source")
	private MeasurementSource source;

	@Column(name = "timestamp", nullable = false, insertable = false, updatable = false)
	private Integer timestamp;

	@Column(name = "value", nullable = false)
	private Long value;

	@ManyToOne
	@JoinColumn(name = "unit", nullable = false)
	private Unit unit;

	protected Measurement() {
	}

	public Measurement(Long userId, Variable variable, MeasurementSource source, Integer timestamp, Long value, Unit unit) {
		this.id = new MeasurementPK(userId, variable.getId(), source.getId(), timestamp);
		this.userId = userId;
		this.variable = variable;
		this.source = source;
		this.timestamp = timestamp;
		this.value = value;
		this.unit = unit;
	}
}
