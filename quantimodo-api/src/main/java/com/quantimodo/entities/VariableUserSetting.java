package com.quantimodo.entities;

import javax.persistence.*;

@Entity
@Table(name = "`qm-variable-user-settings`")
public final class VariableUserSetting {
	@EmbeddedId
	private VariableUserSettingPK id;

	@Column(name = "user", nullable = false, insertable = false, updatable = false)
	protected Long userId;

	@MapsId("variableId")
	@ManyToOne
	@JoinColumn(name = "variable", nullable = false)
	protected Variable variable;

	@ManyToOne
	@JoinColumn(name = "unit")
	private Unit unit;

	protected VariableUserSetting() {
	}

	public VariableUserSetting(Long userId, Variable variable, Unit unit) {
		this.id = new VariableUserSettingPK(userId, variable.getId());
		this.userId = userId;
		this.variable = variable;
		this.unit = unit;
	}

	public Variable getVariable() {
		return variable;
	}

	public Unit getUnit() {
		return unit;
	}
}

