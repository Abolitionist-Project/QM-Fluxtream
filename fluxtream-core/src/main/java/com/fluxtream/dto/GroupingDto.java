package com.fluxtream.dto;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import com.google.gson.annotations.Expose;

public class GroupingDto {

	public static GroupingDto MINUTELY = new GroupingDto("minute", 0);
	public static GroupingDto HOURLY = new GroupingDto("hour", 1);
	public static GroupingDto DAILY = new GroupingDto("day", 2);
	public static GroupingDto WEEKLY = new GroupingDto("week", 3);
	public static GroupingDto MONTHLY = new GroupingDto("month", 4);

	public static List<GroupingDto> VALUES = Arrays.asList(MINUTELY, HOURLY, DAILY, WEEKLY, MONTHLY);

	@Expose
	public String name;

	@Expose
	public Integer value;

	public GroupingDto() {
		// TODO Auto-generated constructor stub
	}

	public GroupingDto(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public static GroupingDto resolveByName(String name) {
		if (name == null) {
			return null;
		}
		for (GroupingDto grouping : VALUES) {
			if (grouping.name.equals(name.toLowerCase())) {
				return grouping;
			}
		}
		return null;
	}

	public static GroupingDto resolveByValue(Integer value) {
		for (GroupingDto grouping : VALUES) {
			if (grouping.value.equals(value)) {
				return grouping;
			}
		}
		return null;
	}

	public long getMillis(long dateTime) {
		if (this.equals(MINUTELY)) {
			return 60000;
		}
		if (this.equals(HOURLY)) {
			return 3600000;
		}
		if (this.equals(DAILY)) {
			return 86400000L;
		}
		if (this.equals(WEEKLY)) {
			return 604800000L;
		}
		if (this.equals(MONTHLY)) {
			return (new DateTime(dateTime).dayOfMonth().getMaximumValue()) * 604800000L;
		}
		throw new RuntimeException("Method is declared only for MINUTELY, HOURLY, DAILY, WEEKLY, MONTHLY");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupingDto other = (GroupingDto) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
