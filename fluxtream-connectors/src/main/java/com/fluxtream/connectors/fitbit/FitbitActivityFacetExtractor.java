package com.fluxtream.connectors.fitbit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fluxtream.ApiData;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.facets.extractors.AbstractFacetExtractor;

@Component
public class FitbitActivityFacetExtractor extends AbstractFacetExtractor {

	Logger logger = Logger.getLogger(FitbitActivityFacetExtractor.class);

	public List<AbstractFacet> extractFacets(ApiData apiData, ObjectType objectType) {
		List<AbstractFacet> facets = new ArrayList<AbstractFacet>();
		JSONObject fitbitResponse = JSONObject.fromObject(apiData.json);

		logger.info("guestId=" + apiData.updateInfo.getGuestId() + " connector=fitbit action=extractFacets objectType="
				+ objectType.getName());

		if (objectType.getName().equals("activity_summary"))
			extractSummaryActivityInfo(apiData, fitbitResponse, facets);
		else if (objectType.getName().equals("logged_activity"))
			extractLoggedActivities(apiData, fitbitResponse, facets);
		else
			logger.info("guestId=" + apiData.updateInfo.getGuestId()
					+ " connector=fitbit action=extractFacets error=no such objectType");

		return facets;
	}

	private void extractSummaryActivityInfo(ApiData apiData, JSONObject fitbitResponse, List<AbstractFacet> facets) {
		long guestId = apiData.updateInfo.getGuestId();
		logger.info("guestId=" + guestId + " connector=fitbit action=extractSummaryActivityInfo");

		FitbitTrackerActivityFacet facet = new FitbitTrackerActivityFacet();

		JSONObject fitbitSummary = fitbitResponse.getJSONObject("summary");

		super.extractCommonFacetData(facet, apiData);
		logger.info("guestId=" + guestId + " connector=fitbit action=extractSummaryActivityInfo");
		facet.date = (String) apiData.updateInfo.getContext("date");
		facet.startTimeStorage = facet.endTimeStorage = noon(facet.date);

		if (fitbitSummary.containsKey("activeScore"))
			facet.activeScore = fitbitSummary.getInt("activeScore");
		if (fitbitSummary.containsKey("floors"))
			facet.floors = fitbitSummary.getInt("floors");
		if (fitbitSummary.containsKey("elevation"))
			facet.elevation = fitbitSummary.getInt("elevation");
		if (fitbitSummary.containsKey("caloriesOut"))
			facet.caloriesOut = fitbitSummary.getInt("caloriesOut");
		if (fitbitSummary.containsKey("fairlyActiveMinutes"))
			facet.fairlyActiveMinutes = fitbitSummary.getInt("fairlyActiveMinutes");
		if (fitbitSummary.containsKey("lightlyActiveMinutes"))
			facet.lightlyActiveMinutes = fitbitSummary.getInt("lightlyActiveMinutes");
		if (fitbitSummary.containsKey("sedentaryMinutes"))
			facet.sedentaryMinutes = fitbitSummary.getInt("sedentaryMinutes");
		if (fitbitSummary.containsKey("veryActiveMinutes"))
			facet.veryActiveMinutes = fitbitSummary.getInt("veryActiveMinutes");
		if (fitbitSummary.containsKey("steps"))
			facet.steps = fitbitSummary.getInt("steps");

		if (fitbitSummary.has("distances")) {
			JSONArray distancesArray = fitbitSummary.getJSONArray("distances");
			for (int i = 0; i < distancesArray.size(); i++) {
				JSONObject distanceObject = distancesArray.getJSONObject(i);
				final String activityType = distanceObject.getString("activity");
				final double distance = distanceObject.getDouble("distance");
				if (activityType.equals("tracker"))
					facet.trackerDistance = distance;
				else if (activityType.equals("loggedActivities"))
					facet.loggedActivitiesDistance = distance;
				else if (activityType.equals("veryActive"))
					facet.veryActiveDistance = distance;
				else if (activityType.equals("total"))
					facet.totalDistance = distance;
				else if (activityType.equals("moderatelyActive"))
					facet.moderatelyActiveDistance = distance;
				else if (activityType.equals("lightlyActive"))
					facet.lightlyActiveDistance = distance;
				else if (activityType.equals("sedentary"))
					facet.sedentaryActiveDistance = distance;
			}
		}

		facets.add(facet);
	}

	private void extractLoggedActivities(ApiData apiData, JSONObject fitbitResponse, List<AbstractFacet> facets) {
		logger.info("guestId=" + apiData.updateInfo.getGuestId() + " connector=fitbit action=extractLoggedActivities");

		JSONArray loggedActivities = fitbitResponse.getJSONArray("activities");

		if (loggedActivities == null || loggedActivities.size() == 0)
			return;

		@SuppressWarnings("rawtypes")
		Iterator iterator = loggedActivities.iterator();
		while (iterator.hasNext()) {
			JSONObject loggedActivity = (JSONObject) iterator.next();

			FitbitLoggedActivityFacet facet = new FitbitLoggedActivityFacet();
			super.extractCommonFacetData(facet, apiData);

			facet.date = (String) apiData.updateInfo.getContext("date");

			final String startTime = loggedActivity.getString("startTime");
			facet.startTimeStorage = facet.endTimeStorage = facet.date + "T" + startTime + ":00.000";
			if (loggedActivity.containsKey("duration")) {
				final int duration = loggedActivity.getInt("duration");
				facet.duration = duration;
			}
			if (loggedActivity.containsKey("activityId"))
				facet.activityId = loggedActivity.getLong("activityId");
			if (loggedActivity.containsKey("activityParentId"))
				facet.activityParentId = loggedActivity.getLong("activityParentId");
			if (loggedActivity.containsKey("calories"))
				facet.calories = loggedActivity.getInt("calories");
			if (loggedActivity.containsKey("description"))
				facet.fullTextDescription = loggedActivity.getString("description");
			if (loggedActivity.containsKey("distance"))
				facet.distance = loggedActivity.getInt("distance");
			if (loggedActivity.containsKey("isFavorite"))
				facet.isFavorite = loggedActivity.getBoolean("isFavorite");
			if (loggedActivity.containsKey("logId"))
				facet.logId = loggedActivity.getLong("logId");
			if (loggedActivity.containsKey("name"))
				facet.name = loggedActivity.getString("name");
			if (loggedActivity.containsKey("steps"))
				facet.steps = loggedActivity.getInt("steps");

			facets.add(facet);
		}

	}

}
