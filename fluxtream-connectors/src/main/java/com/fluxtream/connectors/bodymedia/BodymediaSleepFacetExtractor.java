package com.fluxtream.connectors.bodymedia;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fluxtream.ApiData;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.facets.extractors.AbstractFacetExtractor;
import com.fluxtream.services.ConnectorUpdateService;
import com.fluxtream.services.MetadataService;
import com.fluxtream.utils.TimeUtils;

/**
 * Extracts information from the apicall and creates a facet
 */
@Component
public class BodymediaSleepFacetExtractor extends AbstractFacetExtractor {

	// Logs various transactions
	private static final Logger LOG = LoggerFactory.getLogger(BodymediaSleepFacetExtractor.class);

	@Qualifier("connectorUpdateServiceImpl")
	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@Qualifier("metadataServiceImpl")
	@Autowired
	MetadataService metadataService;

	@Override
	public List<AbstractFacet> extractFacets(ApiData apiData, ObjectType objectType) throws Exception {

		LOG.debug("guestId=" + apiData.updateInfo.getGuestId()
				+ " connector=bodymedia action=extractFacets objectType=" + objectType.getName());

		String name = objectType.getName();
		if (!name.equals("sleep")) {
			throw new RuntimeException("Sleep extractor called with illegal ObjectType");
		}

		return extractSleepFacets(apiData);
	}

	/**
	 * Extracts Data from the Sleep api.
	 * 
	 * @param apiData
	 *            The data returned by bodymedia
	 * @return a list containing a single BodymediaSleepFacet for the current
	 *         day
	 */
	private ArrayList<AbstractFacet> extractSleepFacets(final ApiData apiData) {
		ArrayList<AbstractFacet> facets = new ArrayList<AbstractFacet>();
		/*
		 * burnJson is a JSONArray that contains a seperate JSONArray and
		 * calorie counts for each day
		 */
		JSONObject bodymediaResponse = JSONObject.fromObject(apiData.json);

		if (bodymediaResponse.has("Failed")) {
			LOG.warn("Got failed bodymedia sleep response. Will ignore it.");
			return facets;
		}

		if (bodymediaResponse.has("days")) {
			JSONArray daysArray = bodymediaResponse.getJSONArray("days");
			DateTime d = bodymediaResponse.has("lastSync") ? DATETIME_FORMATTER.parseDateTime(bodymediaResponse
					.getJSONObject("lastSync").getString("dateTime")) : null;
			for (Object o : daysArray) {
				if (o instanceof JSONObject) {
					JSONObject day = (JSONObject) o;
					BodymediaSleepFacet sleep = buildSleepFacet(apiData, d, day);
					facets.add(sleep);
				} else
					throw new JSONException("Days array is not a proper JSONObject");
			}
		}
		return facets;
	}

	private BodymediaSleepFacet buildSleepFacet(final ApiData apiData, DateTime d, JSONObject day) {
		BodymediaSleepFacet sleep = new BodymediaSleepFacet();
		super.extractCommonFacetData(sleep, apiData);
		sleep.date = day.getString("date");
		sleep.efficiency = day.getDouble("efficiency");
		sleep.totalLying = day.getInt("totalLying");
		sleep.totalSleeping = day.getInt("totalSleep");
		sleep.json = day.getString("sleepPeriods");
		sleep.lastSync = d == null ? null : d.getMillis();

		// https://developer.bodymedia.com/docs/read/api_reference_v2/Sleep_Service
		// sleep data is from noon the previous day to noon the
		// current day,
		// so subtract MILLIS_IN_DAY/2 from midnight
		long MILLIS_IN_DAY = 86400000l;
		DateTime date = DATE_FORMATTER.parseDateTime(day.getString("date"));
		sleep.date = DASH_DATE_FORMATTER.print(date.getMillis());
		TimeZone timeZone = metadataService.getTimeZone(apiData.updateInfo.getGuestId(), date.getMillis());
		long fromNoon = TimeUtils.fromMidnight(date.getMillis(), timeZone) - MILLIS_IN_DAY / 2;
		long toNoon = TimeUtils.toMidnight(date.getMillis(), timeZone) - MILLIS_IN_DAY / 2;
		sleep.start = fromNoon;
		sleep.end = toNoon;
		return sleep;
	}

}
