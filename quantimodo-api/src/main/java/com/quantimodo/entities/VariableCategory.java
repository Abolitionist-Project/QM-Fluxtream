package com.quantimodo.entities;

import javax.persistence.*;

@Entity
@Table(name = "`qm-variable-categories`")
public class VariableCategory {
	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	private VariableCategory() {
	}

	public VariableCategory(final String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
