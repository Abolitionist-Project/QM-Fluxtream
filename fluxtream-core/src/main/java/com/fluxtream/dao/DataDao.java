package com.fluxtream.dao;

import com.fluxtream.dto.DataDto;
import com.fluxtream.domain.Guest;
import com.fluxtream.dto.VariableDto;

import java.util.List;
import java.util.Date;

public interface DataDao {
	// Create/update
	boolean insert(Guest guest, int variableId, double value, Date startTime, int duration);

	// Read
	DataDto get(long id);
	List<DataDto> getAll(Guest guest);
	List<DataDto> getByVariable(Guest guest, int variableId);

	// Delete
	boolean delete(long id);
	int deleteAll(Guest guest);
	int deleteByVariable(Guest guest, int variableId);
}

