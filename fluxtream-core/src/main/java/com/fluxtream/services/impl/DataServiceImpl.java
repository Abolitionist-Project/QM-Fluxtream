package com.fluxtream.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quantimodo.etl.QuantimodoRecord;
import com.fluxtream.domain.Guest;
import com.fluxtream.services.VariablesService;
import com.fluxtream.services.DataService;
import com.fluxtream.dao.DataDao;
import com.fluxtream.dto.VariableDto;
import com.fluxtream.dto.DataDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import java.util.Date;
import java.util.List;

@Component
public class DataServiceImpl implements DataService {
	private static final Logger LOG = LoggerFactory.getLogger(DataServiceImpl.class);

	@Autowired
	DataDao dataDao;

	@Autowired
	VariablesService variablesService;

	// Create/update
	public boolean insert(final Guest guest, final int variableId, final double value, final DateTime startTime, final int duration) {
		return dataDao.insert(guest, variableId, value, startTime.toDate(), duration);
	}

	public boolean insert(final Guest guest, final QuantimodoRecord record) {
		final int variableId = variablesService.getVariable(record.isUserDefinedVariable() ? guest : null, record.getApplicationName(), record.getVariableName()).id;
		return insert(guest, variableId, record.getValue(), new DateTime(record.getStartTime()), record.getDuration());
	}

	public int insert(final Guest guest, final QuantimodoRecord[] records) {
		int result = 0;
		for (final QuantimodoRecord record : records) {
			if (insert(guest, record))
				result++;
		}
		return result;
	}

	// Read
	public DataDto get(final long id) {
		return dataDao.get(id);
	}

	public List<DataDto> getAll(final Guest guest) {
		return dataDao.getAll(guest);
	}

	public List<DataDto> getByVariable(final Guest guest, final int variableId) {
		return dataDao.getByVariable(guest, variableId);
	}

	// Delete
	public boolean delete(final long id) {
		return dataDao.delete(id);
	}

	public int deleteAll(final Guest guest) {
		return dataDao.deleteAll(guest);
	}

	public int deleteByVariable(final Guest guest, final int variableId) {
		return dataDao.deleteByVariable(guest, variableId);
	}
}

