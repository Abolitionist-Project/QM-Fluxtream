package com.fluxtream.connectors.fitbit;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fluxtream.ApiData;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.facets.extractors.AbstractFacetExtractor;

/**
 * 
 * @author Candide Kemmler (candide@fluxtream.com)
 */
@Component
public class FitbitWeightFacetExtractor extends AbstractFacetExtractor {

	Logger logger = Logger.getLogger(FitbitActivityFacetExtractor.class);

	@Override
	public List<AbstractFacet> extractFacets(final ApiData apiData, final ObjectType objectType) throws Exception {
		List<AbstractFacet> facets = new ArrayList<AbstractFacet>();
		JSONObject fitbitResponse = JSONObject.fromObject(apiData.json);

		logger.info("guestId=" + apiData.updateInfo.getGuestId() + " connector=fitbit action=extractFacets objectType="
				+ objectType.getName());

		extractWeightInfo(apiData, fitbitResponse, facets);

		return facets;
	}

	private void extractWeightInfo(final ApiData apiData, final JSONObject fitbitResponse,
			final List<AbstractFacet> facets) {
		long guestId = apiData.updateInfo.getGuestId();
		logger.info("guestId=" + guestId + " connector=fitbit action=extractSummaryActivityInfo");

		JSONArray fitbitWeightMeasurements = fitbitResponse.getJSONArray("weight");

		logger.info("guestId=" + guestId + " connector=fitbit action=extractWeightInfo");

		for (int i = 0; i < fitbitWeightMeasurements.size(); i++) {
			FitbitWeightFacet facet = new FitbitWeightFacet();
			super.extractCommonFacetData(facet, apiData);

			facet.date = (String) apiData.updateInfo.getContext("date");
			facet.startTimeStorage = facet.endTimeStorage = noon(facet.date);

			if (fitbitWeightMeasurements.getJSONObject(i).containsKey("bmi"))
				facet.bmi = fitbitWeightMeasurements.getJSONObject(i).getDouble("bmi");
			// if (fitbitWeightMeasurements.getJSONObject(i).containsKey("fat"))
			// facet.fat =
			// fitbitWeightMeasurements.getJSONObject(i).getDouble("fat");
			if (fitbitWeightMeasurements.getJSONObject(i).containsKey("weight"))
				facet.weight = fitbitWeightMeasurements.getJSONObject(i).getDouble("weight");
			if (fitbitWeightMeasurements.getJSONObject(i).containsKey("time")) {
				String time = fitbitWeightMeasurements.getJSONObject(i).getString("time");
				String[] timeParts = time.split(":");
				int hours = Integer.valueOf(timeParts[0]);
				int minutes = Integer.valueOf(timeParts[1]);
				int seconds = Integer.valueOf(timeParts[2]);
				String[] dateParts = facet.date.split("-");
				int year = Integer.valueOf(dateParts[0]);
				int month = Integer.valueOf(dateParts[1]);
				int day = Integer.valueOf(dateParts[2]);
				facet.startTimeStorage = facet.endTimeStorage = toTimeStorage(year, month, day, hours, minutes, seconds);
			}

			facets.add(facet);
		}
	}
}
