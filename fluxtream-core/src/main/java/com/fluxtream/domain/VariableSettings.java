package com.fluxtream.domain;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.fluxtream.dto.VariableType;

@Entity(name = "VariableSettings")
@NamedQueries({ @NamedQuery(name = "variableSettings.byGuestId", query = "SELECT variableSettings FROM VariableSettings variableSettings WHERE variableSettings.guestId=?") })
public class VariableSettings extends AbstractEntity {

	public long guestId;

	public VariableType inputType;
	public Integer inputId;

	public VariableType outputType;
	public Integer outputId;

	public int groupingValue;

	// just use dateTime in long format. it will be values for user timezone
	//public long startTime;

	//public long endTime;

	public VariableSettings() {
		// TODO Auto-generated constructor stub
	}

	public VariableSettings(long guestId, VariableType inputType, Integer inputId, VariableType outputType,
			Integer outputId, int groupingValue/*, long startTime, long endTime*/) {
		super();
		this.guestId = guestId;
		this.inputType = inputType;
		this.inputId = inputId;
		this.outputType = outputType;
		this.outputId = outputId;
		this.groupingValue = groupingValue;
		//this.startTime = startTime;
		//this.endTime = endTime;
	}

}
