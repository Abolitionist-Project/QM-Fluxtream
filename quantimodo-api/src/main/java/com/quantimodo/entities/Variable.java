package com.quantimodo.entities;

import javax.persistence.*;

@Entity
@Table(name = "`qm-variables`")
public final class Variable {
	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "user", nullable = false)
	private Long userId;

	@Column(name = "name", nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "`variable-category`")
	private VariableCategory category;

	@ManyToOne
	@JoinColumn(name = "`default-unit`", nullable = false)
	private Unit defaultUnit;

	@Column(name = "`combination-operation`", nullable = false)
	private CombinationOperation combinationOperation;

	protected Variable() {
	}

	public Variable(Long userId, String name, Unit defaultUnit, CombinationOperation combinationOperation) {
		this.userId = userId;
		this.name = name;
		this.defaultUnit = defaultUnit;
		this.combinationOperation = combinationOperation;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public VariableCategory getCategory() {
		return category;
	}

	public Unit getDefaultUnit() {
		return defaultUnit;
	}

	public CombinationOperation getCombinationOperation() {
		return combinationOperation;
	}
}
