package com.fluxtream.facets.extractors;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fluxtream.ApiData;
import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.connectors.updaters.UpdateInfo;
import com.fluxtream.domain.AbstractFacet;

public abstract class AbstractFacetExtractor {

	protected final static DateTimeFormatter DASH_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

	protected static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ");
	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");

	protected UpdateInfo updateInfo;

	public void setUpdateInfo(UpdateInfo updateInfo) {
		this.updateInfo = updateInfo;
	}

	protected Connector connector() {
		return updateInfo.apiKey.getConnector();
	}

	protected void extractCommonFacetData(AbstractFacet facet, ApiData apiData) {
		facet.guestId = apiData.updateInfo.apiKey.getGuestId();
		facet.api = apiData.updateInfo.apiKey.getConnector().value();
		facet.timeUpdated = System.currentTimeMillis();
		// may be overridden by subclasses, this is just a "first approximation"
		// that may of may not be provided by the specialized extractor
		if (apiData.start != -1)
			facet.start = apiData.start;
		if (apiData.end != -1)
			facet.end = apiData.end;
	}

	protected String noon(String date) {
		return date + "T12:00:00.000";
	}

	protected String toTimeStorage(int year, int month, int day, int hours, int minutes, int seconds) {
		// yyyy-MM-dd'T'HH:mm:ss.SSS
		return (new StringBuilder()).append(year).append("-").append(pad(month)).append("-").append(pad(day))
				.append("T").append(pad(hours)).append(":").append(pad(minutes)).append(":").append(pad(seconds))
				.append(".000").toString();
	}

	protected static String pad(int i) {
		return i < 10 ? (new StringBuilder("0").append(i)).toString() : String.valueOf(i);
	}

	public abstract List<AbstractFacet> extractFacets(ApiData apiData, ObjectType objectType) throws Exception;
}
