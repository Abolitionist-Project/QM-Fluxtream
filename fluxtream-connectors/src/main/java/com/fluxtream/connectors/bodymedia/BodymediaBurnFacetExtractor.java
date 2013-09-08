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
import com.fluxtream.connectors.updaters.UpdateInfo;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.facets.extractors.AbstractFacetExtractor;
import com.fluxtream.services.ConnectorUpdateService;
import com.fluxtream.services.MetadataService;
import com.fluxtream.utils.TimeUtils;

/**
 * Extracts information from the apicall and creates a facet
 */
@Component
public class BodymediaBurnFacetExtractor extends AbstractFacetExtractor {

	// Logs various transactions
	private static final Logger LOG = LoggerFactory.getLogger(BodymediaBurnFacetExtractor.class);

	@Qualifier("metadataServiceImpl")
	@Autowired
	MetadataService metadataService;

	@Qualifier("connectorUpdateServiceImpl")
	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@Override
	public List<AbstractFacet> extractFacets(ApiData apiData, ObjectType objectType) throws Exception {

		LOG.debug("guestId=" + apiData.updateInfo.getGuestId()
				+ " connector=bodymedia action=extractFacets objectType=" + objectType.getName());

		String name = objectType.getName();
		if (!name.equals("burn")) {
			throw new JSONException("Burn extractor called with illegal ObjectType");
		}
		return extractBurnFacets(apiData);
	}

	@Override
	public void setUpdateInfo(final UpdateInfo updateInfo) {
		super.setUpdateInfo(updateInfo);
	}

	/**
	 * Extracts facets for each day from the data returned by the api.
	 * 
	 * @param apiData
	 *            The data returned by the Burn api
	 * @return A list of facets for each day provided by the apiData
	 */
	private ArrayList<AbstractFacet> extractBurnFacets(ApiData apiData) {
		ArrayList<AbstractFacet> facets = new ArrayList<AbstractFacet>();
		/*
		 * burnJson is a JSONArray that contains a seperate JSONArray and
		 * calorie counts for each day
		 */
		JSONObject bodymediaResponse = JSONObject.fromObject(apiData.json);

		if (bodymediaResponse.has("Failed")) {
			LOG.warn("Got failed bodymedia burn response. Will ignore it.");
			return facets;
		}

		if (bodymediaResponse.has("days")) {
			DateTime d = bodymediaResponse.has("lastSync") ? DATETIME_FORMATTER.parseDateTime(bodymediaResponse
					.getJSONObject("lastSync").getString("dateTime")) : null;
			JSONArray daysArray = bodymediaResponse.getJSONArray("days");
			for (Object o : daysArray) {
				if (o instanceof JSONObject) {
					JSONObject day = (JSONObject) o;
					BodymediaBurnFacet burn = buildBurnFacet(apiData, d, day);

					facets.add(burn);
				} else
					throw new RuntimeException("days array is not a proper JSONObject");
			}
		}
		return facets;
	}

	private BodymediaBurnFacet buildBurnFacet(ApiData apiData, DateTime d, JSONObject day) {
		BodymediaBurnFacet burn = new BodymediaBurnFacet();
		// The following call must be made to load data about he
		// facets
		super.extractCommonFacetData(burn, apiData);
		burn.setTotalCalories(day.getInt("totalCalories"));
		burn.date = day.getString("date");
		burn.setEstimatedCalories(day.getInt("estimatedCalories"));
		burn.setPredictedCalories(day.getInt("predictedCalories"));
		burn.json = day.getString("minutes");
		burn.lastSync = d == null ? null : d.getMillis();
		DateTime date = DATE_FORMATTER.parseDateTime(day.getString("date"));
		burn.date = DASH_DATE_FORMATTER.print(date.getMillis());
		TimeZone timeZone = metadataService.getTimeZone(apiData.updateInfo.getGuestId(), date.getMillis());
		long fromMidnight = TimeUtils.fromMidnight(date.getMillis(), timeZone);
		long toMidnight = TimeUtils.toMidnight(date.getMillis(), timeZone);
		// Sets the start and end times for the facet so that it can
		// be uniquely defined
		burn.start = fromMidnight;
		burn.end = toMidnight;
		return burn;
	}
}
