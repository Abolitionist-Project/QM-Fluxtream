package com.fluxtream.dto;

import com.google.gson.annotations.Expose;

public class UnitDto {

	@Expose
	public int id;
	@Expose
	public int siUnit;
	@Expose
	public double siUnitsPerThisUnit;
	@Expose
	public String name;

	public boolean useAverage;
}
