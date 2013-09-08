package com.fluxtream.test.services.variables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fluxtream.dao.UnitsDao;
import com.fluxtream.dto.UnitDto;

public class UnitsDaoImplTest implements UnitsDao {

	private static final Map<Integer, UnitDto> UNIT_DTOS = new HashMap<Integer, UnitDto>();
	static {
		UnitDto unitDto4 = new UnitDto();
		unitDto4.siUnit = 1;
		// one hour. siUnit == second
		unitDto4.siUnitsPerThisUnit = 3600;
		unitDto4.id = 4;
		unitDto4.useAverage = false;

		UNIT_DTOS.put(unitDto4.id, unitDto4);
	}

	@Override
	public UnitDto findUnitById(Integer unitId) {
		return UNIT_DTOS.get(unitId);
	}

	@Override
	public List<UnitDto> getUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UnitDto> getUnits(int siUnit) {
		// TODO Auto-generated method stub
		return null;
	}

}
