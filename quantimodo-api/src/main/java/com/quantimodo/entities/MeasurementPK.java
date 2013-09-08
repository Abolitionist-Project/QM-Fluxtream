package com.quantimodo.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public final class MeasurementPK implements Serializable {
	@Column(name = "user", nullable = false)
	private Long userId;

	@Column(name = "variable", nullable = false)
	private Long variableId;

	@Column(name = "source", nullable = false)
	private Long sourceId;

	@Column(name = "timestamp", nullable = false)
	private Integer timestamp;

	public MeasurementPK(Long userId, Long variableId, Long sourceId, Integer timestamp) {
		this.userId = userId;
		this.variableId = variableId;
		this.sourceId = sourceId;
		this.timestamp = timestamp;
	}
}
