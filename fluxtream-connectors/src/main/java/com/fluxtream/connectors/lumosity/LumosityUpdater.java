package com.fluxtream.connectors.lumosity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.Connector.UpdateStrategyType;
import com.fluxtream.connectors.ConnectorNames;
import com.fluxtream.connectors.annotations.Updater;
import com.fluxtream.connectors.lumosity.LumosityFacet.Type;
import com.fluxtream.connectors.updaters.AbstractUpdater;
import com.fluxtream.connectors.updaters.RateLimitReachedException;
import com.fluxtream.connectors.updaters.UpdateInfo;
import com.fluxtream.domain.ApiUpdate;
import com.fluxtream.services.ApiDataService;
import com.fluxtream.services.ApiDataService.ScrappingConnectorCredentials;
import com.fluxtream.services.ConnectorUpdateService;
import com.fluxtream.utils.HttpUtils;
import com.fluxtream.utils.Utils;

/**
 * @author alucab
 * 
 */

@Component
@Updater(prettyName = "Lumosity", value = 101, objectTypes = { LumosityFacet.class }, updateStrategyType = UpdateStrategyType.ALWAYS_UPDATE)
// @JsonFacetCollection(LumosityFacetVOCollection.class)
public class LumosityUpdater extends AbstractUpdater {

	private static final String LOGIN_ERROR_MESSAGE = "Invalid username/password combination";
	private static final String LOGIN_URL = "https://www.lumosity.com/login";
	private static final String BPI_ALL_GET = "http://www.lumosity.com/app/v4/training_history/bpis.json?series_type=overall";
	// is not used now
	// private static final String GAMES_ALL_GET =
	// "http://www.lumosity.com/app/v4/training_history/game_plays.json?series_type=overall";
	private static final Pattern EXTRACT_DATA_PATTERN = Pattern
			.compile("authenticity_token\\\" type=\\\"hidden\\\" value=\\\"(.+)\\\"");

	private final static Logger LOGGER = Logger.getLogger(LumosityUpdater.class);

	@Qualifier("connectorUpdateServiceImpl")
	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@Qualifier("apiDataServiceImpl")
	@Autowired
	ApiDataService apiDataService;

	public LumosityUpdater() {
		super();
	}

	@Override
	protected void updateConnectorDataHistory(UpdateInfo updateInfo) throws RateLimitReachedException, Exception {

		LOGGER.debug("lumosity-updateConnectorDataHistory");
		apiDataService.eraseApiData(updateInfo.getGuestId(), Connector.getConnector(ConnectorNames.LUMOSITY), 1);
		sync(updateInfo, 0, System.currentTimeMillis());
	}

	@Override
	public void updateConnectorData(UpdateInfo updateInfo) throws Exception {
		ApiUpdate lastSuccessfulUpdate = connectorUpdateService.getLastSuccessfulUpdate(updateInfo.apiKey.getGuestId(),
				getConnector());
		LOGGER.debug("lumosity-updateConnectorData");

		if (lastSuccessfulUpdate == null) {
			sync(updateInfo, 0, System.currentTimeMillis());
			return;
		}

		sync(updateInfo, lastSuccessfulUpdate.ts, System.currentTimeMillis());
	}

	private void sync(UpdateInfo updateInfo, long from, long to) throws Exception {
		try {
			LOGGER.debug("sync");

			ScrappingConnectorCredentials credentials = apiDataService.getScrapingConnectorCredentials(
					updateInfo.getGuestId(), updateInfo.apiKey.getConnector());

			String jsonString = retrieveScores(updateInfo, credentials, from, to);

			countSuccessfulApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, to, BPI_ALL_GET, jsonString);
		} catch (Exception e) {
			countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, to, BPI_ALL_GET,
					Utils.stackTrace(e));
			throw e;
		}
	}

	private String retrieveScores(UpdateInfo updateInfo, ScrappingConnectorCredentials credentials, long from, long to)
			throws RateLimitReachedException, Exception {
		HttpClient httpclient = getConnectedClient(updateInfo, credentials);
		try {
			HttpGet httpGetBpi = new HttpGet(BPI_ALL_GET);
			String jsonString = request(httpclient, httpGetBpi);
			JSONArray json = JSONArray.fromObject(jsonString);
			parseScores(updateInfo, json, from, to);
			return jsonString;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

	}

	private void parseScores(UpdateInfo updateInfo, JSONArray json, long from, long to)
			throws RateLimitReachedException, Exception {
		long guestId = updateInfo.getGuestId();
		JSONArray scoresArray = json;
		for (int i = 0; i < scoresArray.size(); i++) {
			JSONObject scoreObject = scoresArray.getJSONObject(i);
			String id = scoreObject.getString("id");
			Type type = Type.resolveTypeByValue(id);
			JSONArray scoreSeries = scoreObject.getJSONArray("data");
			for (int j = 0; j < scoreSeries.size(); j++) {
				JSONObject goal = scoreSeries.getJSONObject(j);
				long actualkey = goal.getLong("x");
				if (actualkey > from) {
					LumosityFacet lumosityFacet = buildFacet(guestId, type, goal, actualkey);
					apiDataService.cacheApiDataObject(updateInfo, actualkey, actualkey, lumosityFacet);
				}
			}
		}
	}

	private LumosityFacet buildFacet(long guestId, Type type, JSONObject goal, long actualkey) {
		LumosityFacet goalFacet = new LumosityFacet();
		goalFacet.lumosity_id = actualkey;
		goalFacet.type = type;
		goalFacet.score = goal.getInt("y");
		goalFacet.guestId = guestId;
		goalFacet.api = getConnector().value();
		goalFacet.objectType = 1;
		long now = System.currentTimeMillis();
		goalFacet.timeUpdated = now;
		return goalFacet;
	}

	public void testConnection(ScrappingConnectorCredentials credentials) throws Exception {
		HttpClient connectedClient = getConnectedClient(null, credentials);
		connectedClient.getConnectionManager().shutdown();
	}

	private HttpClient getConnectedClient(UpdateInfo updateInfo, ScrappingConnectorCredentials credentials)
			throws Exception {
		HttpClient httpclient = null;
		long then = System.currentTimeMillis();
		String requestUrl = "request url not set yet";
		try {
			httpclient = HttpUtils.wrapClient(new DefaultHttpClient());

			requestUrl = LOGIN_URL;
			HttpGet httpget1 = new HttpGet(requestUrl);

			String formPage = request(httpclient, httpget1);

			Matcher matcher = EXTRACT_DATA_PATTERN.matcher(formPage);

			if (!matcher.find()) {
				throw new RuntimeException("Invalid Form Received from Login Page: " + formPage);
			}

			String myToken = matcher.group(1);

			requestUrl = LOGIN_URL;
			HttpPost httpost = new HttpPost(requestUrl);

			List<NameValuePair> nvps = fillData(credentials.username, credentials.password, myToken);

			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			String answerPage = request(httpclient, httpost);

			// Invalid username/password combination
			if (answerPage.contains(LOGIN_ERROR_MESSAGE)) {
				throw new RuntimeException("Lumosity login error: " + LOGIN_ERROR_MESSAGE);
			}
			return httpclient;
		} catch (Exception e) {
			LOGGER.error("An error occured during retrieving data for lumosity", e);
			if (updateInfo != null) {
				countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl,
						Utils.stackTrace(e));
			}

			if (httpclient != null) {
				httpclient.getConnectionManager().shutdown();
			}
			throw e;
		}
	}

	private String request(HttpClient httpClient, HttpRequestBase httpReq) throws Exception {
		HttpResponse response = httpClient.execute(httpReq);
		HttpEntity entity = response.getEntity();
		StatusLine statusLine = response.getStatusLine();

		if (entity == null) {
			throw new RuntimeException("Invalid response from " + httpReq.getURI() + ": "
					+ statusLine.getReasonPhrase() + " with code " + statusLine.getStatusCode());
		}
		return EntityUtils.toString(entity);
	}

	private static List<NameValuePair> fillData(String username, String password, String myToken) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair("utf8", "&#x2713;"));
		nvps.add(new BasicNameValuePair("authenticity_token", myToken));
		nvps.add(new BasicNameValuePair("redirect_url", ""));
		nvps.add(new BasicNameValuePair("failure_redirect_url", ""));
		nvps.add(new BasicNameValuePair("buy_now", ""));
		nvps.add(new BasicNameValuePair("screen_resolution", "1366x768"));
		nvps.add(new BasicNameValuePair("user[login]", username));
		nvps.add(new BasicNameValuePair("user[password]", password));
		nvps.add(new BasicNameValuePair("commit", "Login"));
		return nvps;
	}
}
