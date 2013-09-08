package com.fluxtream.dto;

import com.google.gson.annotations.Expose;

public class FillingDto {

	public enum FillingType {
		IS_MISSING(0), CONSTANT(1), INTERPOLATE(2), AVERAGE(3);

		public int value;

		private FillingType(int value) {
			this.value = value;
		}

		public static FillingType resolveByValue(int value) {
			for (FillingType fillingType : FillingType.values()) {
				if (fillingType.value == value) {
					return fillingType;
				}
			}
			return null;
		}
	}

	@Expose
	public FillingType type;

	@Expose
	public Double value;

}
