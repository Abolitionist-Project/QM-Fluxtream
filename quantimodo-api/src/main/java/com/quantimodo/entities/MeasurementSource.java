package com.quantimodo.entities;

import javax.persistence.*;

@Entity
@Table(name = "`qm-measurement-sources`")
public final class MeasurementSource {
	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	public MeasurementSource(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
