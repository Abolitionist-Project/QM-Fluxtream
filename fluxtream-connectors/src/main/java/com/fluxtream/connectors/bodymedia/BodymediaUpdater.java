package com.fluxtream.connectors.bodymedia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fluxtream.connectors.ObjectType;
import com.fluxtream.connectors.SignpostOAuthHelper;
import com.fluxtream.connectors.annotations.Updater;
import com.fluxtream.connectors.updaters.AbstractUpdater;
import com.fluxtream.connectors.updaters.UpdateInfo;
import com.fluxtream.domain.ApiKey;
import com.fluxtream.services.MetadataService;
import com.fluxtream.utils.TimeUtils;

@Component
@Updater(prettyName = "BodyMedia", value = 88, objectTypes = { BodymediaBurnFacet.class, BodymediaSleepFacet.class,
		BodymediaStepsFacet.class }, hasFacets = true, additionalParameters = { "api_key" }, defaultChannels = {
		"BodyMedia.mets", "BodyMedia.lying" })
public class BodymediaUpdater extends AbstractUpdater {

	private static final Logger LOG = LoggerFactory.getLogger(BodymediaUpdater.class);

	private static final String BODYMEDIA_REST_V2 = "http://api.bodymedia.com/v2/json/";

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
	private static final DateTimeFormatter DASH_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ");

	@Autowired
	SignpostOAuthHelper signpostHelper;

	@Qualifier("metadataServiceImpl")
	@Autowired
	MetadataService metadataService;

	@Autowired
	BodymediaOAuthController bodymediaController;

	private final HashMap<ObjectType, String> url = new HashMap<ObjectType, String>();
	private final HashMap<ObjectType, Integer> maxIncrement = new HashMap<ObjectType, Integer>();

	public BodymediaUpdater() {
		super();
		ObjectType burn = ObjectType.getObjectType(getConnector(), "burn");
		ObjectType sleep = ObjectType.getObjectType(getConnector(), "sleep");
		ObjectType steps = ObjectType.getObjectType(getConnector(), "steps");
		url.put(burn, "burn/day/minute/intensity/");
		url.put(sleep, "sleep/day/period/");
		url.put(steps, "step/day/hour/");
		maxIncrement.put(burn, 1);
		maxIncrement.put(sleep, 1);
		maxIncrement.put(steps, 31);
	}

	@Override
	public void updateConnectorDataHistory(UpdateInfo updateInfo) throws Exception {

		// NOTE: commented it out because it's not used and when do request to
		// get user data or time zone it returns 403 Forbidden
		// Get timezone map for this user
		// List<TimezoneMapElt> tzMap = getTimezoneMap(updateInfo);

		// Prasanth removed this and said it doesn't work in checkin
		// https://github.com/fluxtream/fluxtream-app/commit/eb10e1a3bb38170657f81621d26e1775644aa18f
		// TODO: figure out how to handle exchanging the token in a way that
		// does work

		checkAndReplaceOauthToken(updateInfo);
		for (ObjectType ot : updateInfo.objectTypes()) {
			String date = jpaDaoService.findOne("bodymedia." + ot.getName() + ".getFailedUpdate", String.class,
					updateInfo.getGuestId());
			DateTime end = (date == null ? new DateTime() : DATE_FORMATTER.parseDateTime(date));
			DateTime start = calculateStartDateTime(updateInfo);

			retrieveHistory(updateInfo, ot, url.get(ot), maxIncrement.get(ot), start, end);
		}
	}

	@Override
	public void updateConnectorData(UpdateInfo updateInfo) throws Exception {
		// NOTE: commented it out because it's not used and when do request to
		// get user data or time zone it returns 403 Forbidden
		// Get timezone map for this user
		// List<TimezoneMapElt> tzMap = getTimezoneMap(updateInfo);

		checkAndReplaceOauthToken(updateInfo);
		for (ObjectType ot : updateInfo.objectTypes()) {
			BodymediaAbstractFacet endDate = jpaDaoService.findOne("bodymedia." + ot.getName() + ".getFailedUpdate",
					BodymediaAbstractFacet.class, updateInfo.getGuestId());
			DateTime start, end;
			if (endDate != null) {
				end = DATE_FORMATTER.parseDateTime(endDate.date);
				TimeZone timeZone = metadataService.getTimeZone(updateInfo.getGuestId(), end.getMillis());
				BodymediaAbstractFacet startDate = jpaDaoService.findOne("bodymedia." + ot.getName() + ".getDaysPrior",
						BodymediaAbstractFacet.class, updateInfo.getGuestId(),
						TimeUtils.fromMidnight(end.getMillis(), timeZone));
				start = DATE_FORMATTER.parseDateTime(startDate.date);
			} else {
				end = new DateTime();
				start = getStartDate(updateInfo, ot);
			}
			retrieveHistory(updateInfo, ot, url.get(ot), maxIncrement.get(ot), start, end);
		}
	}

	private DateTime calculateStartDateTime(UpdateInfo updateInfo) throws Exception {
		OAuthConsumer consumer = setupConsumer(updateInfo.apiKey);
		String api_key = env.get("bodymediaConsumerKey");
		String startDate = getUserRegistrationDate(updateInfo, api_key, consumer);
		DateTime start = DATE_FORMATTER.parseDateTime(startDate);
		return start;
	}

	/**
	 * Retrieves that history for the given facet from the start date to the end
	 * date. It peforms the api calls in reverse order starting from the end
	 * date. This is so that the most recent information is retrieved first.
	 * 
	 * @param updateInfo
	 *            The api's info
	 * @param ot
	 *            The ObjectType that represents the facet to be updated
	 * @param urlExtension
	 *            the request uri used for the api
	 * @param increment
	 *            the number of days to retrieve at once from bodymedia
	 * @param start
	 *            The earliest date for which the burn history is retrieved.
	 *            This date is included in the update.
	 * @param end
	 *            The latest date for which the burn history is retrieved. This
	 *            date is also included in the update.
	 * @throws Exception
	 *             If either storing the data fails or if the rate limit is
	 *             reached on Bodymedia's api
	 */
	private void retrieveHistory(UpdateInfo updateInfo, ObjectType ot, String urlExtension, int increment,
			DateTime start, DateTime end) throws Exception {
		DateTimeComparator comparator = DateTimeComparator.getDateOnlyInstance();
		DateTime current = end;
		String apiKey = updateInfo.apiKey.getAttributeValue("api_key", env);
		String apiUrl = "url is not specified yet";
		try {
			while (comparator.compare(current, start) > 0) {
				String startPeriod = current.minusDays(increment - 1).toString(DATE_FORMATTER);
				String endPeriod = current.toString(DATE_FORMATTER);
				apiUrl = BODYMEDIA_REST_V2 + urlExtension + startPeriod + "/" + endPeriod + "?api_key=" + apiKey;
				// bodymedia key for testing has restrictions
				if (StringUtils.hasText(env.get("bodymediaTestSleepTimeMs"))) {
					Thread.sleep(Integer.parseInt(env.get("bodymediaTestSleepTimeMs")));
				}
				// The following call may fail due to bodymedia's api. That is
				// expected behavior
				String jsonResponse = signpostHelper
						.makeRestCall(getConnector(), updateInfo.apiKey, ot.value(), apiUrl);
				apiDataService.cacheApiDataJSON(updateInfo, jsonResponse, -1, -1);
				current = current.minusDays(increment);
			}
			String startPeriod = start.toString(DATE_FORMATTER);
			String endPeriod = current.plusDays(increment - 1).toString(DATE_FORMATTER);
			apiUrl = BODYMEDIA_REST_V2 + urlExtension + startPeriod + "/" + endPeriod + "?api_key=" + apiKey;

			// bodymedia key for testing has restrictions
			if (StringUtils.hasText(env.get("bodymediaTestSleepTimeMs"))) {
				Thread.sleep(Integer.parseInt(env.get("bodymediaTestSleepTimeMs")));
			}
			// The following call may fail due to bodymedia's api. That is
			// expected behavior
			String jsonResponse = signpostHelper.makeRestCall(getConnector(), updateInfo.apiKey, ot.value(), apiUrl);
			apiDataService.cacheApiDataJSON(updateInfo, jsonResponse, -1, -1);
			countSuccessfulApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, System.currentTimeMillis(),
					apiUrl, jsonResponse);
		} catch (Exception e) {
			LOG.warn("Request to bodymedia with api_key=" + apiKey + " for guest_id=" + updateInfo.getGuestId()
					+ " failed", e);
			JSONObject json = new JSONObject();
			json.put("Failed", "");
			json.put("Date", current.toString(DATE_FORMATTER));
			apiDataService.cacheApiDataJSON(updateInfo, json.toString(), -1, -1);
			countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, System.currentTimeMillis(),
					apiUrl, e.getMessage());
		}
	}

	private OAuthConsumer setupConsumer(ApiKey apiKey) {
		String api_key = env.get("bodymediaConsumerKey");
		String bodymediaConsumerSecret = env.get("bodymediaConsumerSecret");

		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(api_key, bodymediaConsumerSecret);

		String accessToken = apiKey.getAttributeValue("accessToken", env);
		String tokenSecret = apiKey.getAttributeValue("tokenSecret", env);

		consumer.setTokenWithSecret(accessToken, tokenSecret);
		return consumer;
	}

	public DateTime getStartDate(UpdateInfo updateInfo, ObjectType ot) throws Exception {
		BodymediaAbstractFacet facet = jpaDaoService.findOne("bodymedia." + ot.getName() + ".getByLastSync",
				BodymediaAbstractFacet.class, updateInfo.getGuestId());
		String startDate;
		if (facet == null) {
			OAuthConsumer consumer = setupConsumer(updateInfo.apiKey);
			String api_key = env.get("bodymediaConsumerKey");
			startDate = getUserRegistrationDate(updateInfo, api_key, consumer);
		} else {
			startDate = facet.date;
		}
		try {
			return DATE_FORMATTER.parseDateTime(startDate);
		} catch (IllegalArgumentException e) {
			return DASH_DATE_FORMATTER.parseDateTime(startDate);
		}
	}

	public String getUserRegistrationDate(UpdateInfo updateInfo, String api_key, OAuthConsumer consumer)
			throws Exception {
		long then = System.currentTimeMillis();
		String requestUrl = BODYMEDIA_REST_V2 + "user/info?api_key=" + api_key;

		HttpGet request = new HttpGet(requestUrl);
		consumer.sign(request);
		HttpClient client = env.getHttpClient();
		// bodymedia key for testing has restrictions
		if (StringUtils.hasText(env.get("bodymediaTestSleepTimeMs"))) {
			Thread.sleep(Integer.parseInt(env.get("bodymediaTestSleepTimeMs")));
		}
		HttpResponse response = client.execute(request);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseText = responseHandler.handleResponse(response);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			final String reasonPhrase = response.getStatusLine().getReasonPhrase();
			countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl, reasonPhrase);
			throw new Exception("Unexpected error occured when trying to get statuses. Error code " + statusCode
					+ ", error text " + responseText);
		}
		countSuccessfulApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl);

		JSONObject userInfo = JSONObject.fromObject(responseText);
		return userInfo.getString("registrationDate");

	}

	public List<TimezoneMapElt> getTimezoneMap(UpdateInfo updateInfo) throws Exception {
		OAuthConsumer consumer = setupConsumer(updateInfo.apiKey);
		String api_key = env.get("bodymediaConsumerKey");

		List<TimezoneMapElt> ret = new ArrayList<TimezoneMapElt>();

		try {
			JSONArray timezoneMapJson = getUserTimezoneHistory(updateInfo, api_key, consumer);
			for (int i = 0; i < timezoneMapJson.size(); i++) {
				JSONObject jsonRecord = timezoneMapJson.getJSONObject(i);
				final String tzName = jsonRecord.getString("value");
				final String startDateStr = jsonRecord.getString("startDate");
				final String endDateStr = jsonRecord.optString("endDate");
				DateTime startDate;
				DateTime endDate;
				DateTimeZone tz;
				TimezoneMapElt tzElt;

				tz = DateTimeZone.forID(tzName);
				startDate = DATETIME_FORMATTER.parseDateTime(startDateStr);
				if (endDateStr.equals("")) {
					// Last entry in table has no endDate, set it to be now in
					// the target timezone
					// TODO: this should perhaps be in the future instead
					endDate = new DateTime(tz);
				} else {
					endDate = DATETIME_FORMATTER.parseDateTime(endDateStr);
				}
				tzElt = new TimezoneMapElt(startDate, endDate, tz);

				ret.add(tzElt);
			}

		} catch (Throwable e) {
			LOG.warn("An error occured during getting user timezone history", e);
		}
		return ret;
	}

	public JSONArray getUserTimezoneHistory(UpdateInfo updateInfo, String api_key, OAuthConsumer consumer)
			throws Exception {
		long then = System.currentTimeMillis();
		String requestUrl = BODYMEDIA_REST_V2 + "timezone?api_key=" + api_key;

		HttpGet request = new HttpGet(requestUrl);
		consumer.sign(request);
		HttpClient client = env.getHttpClient();

		// bodymedia key for testing has restrictions
		if (StringUtils.hasText(env.get("bodymediaTestSleepTimeMs"))) {
			Thread.sleep(Integer.parseInt(env.get("bodymediaTestSleepTimeMs")));
		}

		HttpResponse response = client.execute(request);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String json = responseHandler.handleResponse(response);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl, "");
			throw new Exception("Error: " + statusCode
					+ " Unexpected error occured when trying to bodymedia timezone for user. Status code " + statusCode
					+ ", response " + json + updateInfo.apiKey.getGuestId());
		}
		countSuccessfulApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl);

		JSONObject userInfo = JSONObject.fromObject(json);
		return userInfo.getJSONArray("timezones");

	}

	private void checkAndReplaceOauthToken(UpdateInfo updateInfo) throws OAuthExpectationFailedException,
			OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthCommunicationException {
		String time = guestService.getApiKeyAttribute(updateInfo.getGuestId(), getConnector(), "tokenExpiration");
		if (Long.parseLong(time) < System.currentTimeMillis() / 1000) {
			bodymediaController.replaceToken(updateInfo);
		}
	}
}
