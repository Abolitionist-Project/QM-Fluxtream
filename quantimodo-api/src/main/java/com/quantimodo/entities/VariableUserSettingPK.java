package com.quantimodo.entities;

import javax.persistence.Column;
import java.io.Serializable;

public final class VariableUserSettingPK implements Serializable {
	@Column(name = "user", nullable = false)
	protected Long userId;

	@Column(name = "variable", nullable = false)
	protected Long variableId;

	protected VariableUserSettingPK() {
	}

	public VariableUserSettingPK(Long userId, Long variableId) {
		this.userId = userId;
		this.variableId = variableId;
	}
	// TODO: equals, hashCode
}

