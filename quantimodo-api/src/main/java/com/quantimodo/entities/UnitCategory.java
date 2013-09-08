package com.quantimodo.entities;

import javax.persistence.*;

@Entity
@Table(name = "`qm-unit-categories`")
public final class UnitCategory {
	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	private UnitCategory() {
	}

	public UnitCategory(final String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
