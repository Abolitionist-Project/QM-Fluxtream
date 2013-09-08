package com.fluxtream.services;

import com.quantimodo.etl.QuantimodoRecord;
import com.fluxtream.domain.Guest;
import com.fluxtream.dto.DataDto;
import org.joda.time.DateTime;

import java.util.List;

public interface DataService {
	// Create/update
	boolean insert(Guest guest, int variableId, double value, DateTime startTime, int duration);
	boolean insert(Guest guest, QuantimodoRecord record);
	int insert(Guest guest, QuantimodoRecord[] records);

	// Read
	DataDto get(long id);
	List<DataDto> getAll(Guest guest);
	List<DataDto> getByVariable(Guest guest, int variableId);

	// Delete
	boolean delete(long id);
	int deleteAll(Guest guest);
	int deleteByVariable(Guest guest, int variableId);
}
