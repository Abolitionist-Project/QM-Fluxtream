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
public class BodymediaStepFacetExtractor extends AbstractFacetExtractor {

	// Logs various transactions
	private static final Logger LOG = LoggerFactory.getLogger(BodymediaStepFacetExtractor.class);

	@Qualifier("connectorUpdateServiceImpl")
	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@Qualifier("metadataServiceImpl")
	@Autowired
	MetadataService metadataService;

	@Override
	public List<AbstractFacet> extractFacets(ApiData apiData, ObjectType objectType) throws Exception {

		LOG.warn("guestId=" + apiData.updateInfo.getGuestId() + " connector=bodymedia action=extractFacets objectType="
				+ objectType.getName());

		String name = objectType.getName();
		if (!name.equals("steps")) {
			throw new RuntimeException("Step extractor called with illegal ObjectType");
		}
		return extractStepFacets(apiData);
	}

	private ArrayList<AbstractFacet> extractStepFacets(final ApiData apiData) {
		ArrayList<AbstractFacet> facets = new ArrayList<AbstractFacet>();
		/*
		 * burnJson is a JSONArray that contains a seperate JSONArray and
		 * calorie counts for each day
		 */
		JSONObject bodymediaResponse = JSONObject.fromObject(apiData.json);

		if (bodymediaResponse.has("Failed")) {
			LOG.warn("Got failed bodymedia steps response. Will ignore it.");
			return facets;
		}

		if (bodymediaResponse.has("days")) {
			JSONArray daysArray = bodymediaResponse.getJSONArray("days");
			DateTime d = bodymediaResponse.has("lastSync") ? DATETIME_FORMATTER.parseDateTime(bodymediaResponse
					.getJSONObject("lastSync").getString("dateTime")) : null;
			for (Object o : daysArray) {
				if (o instanceof JSONObject) {
					JSONObject day = (JSONObject) o;
					BodymediaStepsFacet steps = buildStepsFacet(apiData, d, day);

					facets.add(steps);
				} else
					throw new JSONException("Days array is not a proper JSONObject");
			}
		}
		return facets;
	}

	private BodymediaStepsFacet buildStepsFacet(final ApiData apiData, DateTime d, JSONObject day) {
		BodymediaStepsFacet steps = new BodymediaStepsFacet();
		super.extractCommonFacetData(steps, apiData);
		steps.totalSteps = day.getInt("totalSteps");
		steps.date = day.getString("date");
		steps.json = day.getString("hours");
		steps.lastSync = d == null ? null : d.getMillis();

		DateTime date = DATE_FORMATTER.parseDateTime(day.getString("date"));
		steps.date = DASH_DATE_FORMATTER.print(date.getMillis());
		TimeZone timeZone = metadataService.getTimeZone(apiData.updateInfo.getGuestId(), date.getMillis());
		long fromMidnight = TimeUtils.fromMidnight(date.getMillis(), timeZone);
		long toMidnight = TimeUtils.toMidnight(date.getMillis(), timeZone);
		steps.start = fromMidnight;
		steps.end = toMidnight;
		return steps;
	}
}