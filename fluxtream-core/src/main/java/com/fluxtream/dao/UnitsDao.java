package com.fluxtream.dao;

import java.util.List;

import com.fluxtream.dto.UnitDto;

public interface UnitsDao {

	UnitDto findUnitById(Integer unitId);

	List<UnitDto> getUnits();

	List<UnitDto> getUnits(int siUnit);
}
