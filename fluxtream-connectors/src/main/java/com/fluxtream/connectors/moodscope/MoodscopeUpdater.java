package com.fluxtream.connectors.moodscope;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.Connector.UpdateStrategyType;
import com.fluxtream.connectors.ConnectorNames;
import com.fluxtream.connectors.annotations.Updater;
import com.fluxtream.connectors.updaters.AbstractUpdater;
import com.fluxtream.connectors.updaters.RateLimitReachedException;
import com.fluxtream.connectors.updaters.UpdateInfo;
import com.fluxtream.domain.ApiUpdate;
import com.fluxtream.services.ApiDataService;
import com.fluxtream.services.ApiDataService.ScrappingConnectorCredentials;
import com.fluxtream.services.ConnectorUpdateService;
import com.fluxtream.utils.HttpUtils;
import com.fluxtream.utils.Utils;

@Component
@Updater(prettyName = "Moodscope", value = 102, objectTypes = { MoodscopeFacet.class }, updateStrategyType = UpdateStrategyType.ALWAYS_UPDATE)
// @JsonFacetCollection(MoodscopeFacetVOCollection.class)
public class MoodscopeUpdater extends AbstractUpdater {

	private static final String MOODSCOPE_MAIN_URL = "https://www.moodscope.com";
	private static final String LOGIN_ERROR_MESSAGE = "Sorry, your username/password weren&#039;t recognised.";
	private static final String LOGIN_URL = "https://www.moodscope.com/login-check";
	private static final String WELCOME_URL = "https://www.moodscope.com/welcome";

	private static final String MOODSCOPE_ALL_GET = "https://www.moodscope.com/chart?range=2000";

	/*
	 * Row to be captured {name: 'Click to add explanation',fillColor:
	 * '#FF0033', x: Date.UTC(2013, 2, 25), y: 89, lineWidth: 1, lineColor:
	 * '#FF0033', url: '/chart/annotate/2013-03-25'}, {name: '(.*)',fillColor:
	 * '#FF0033',\s+x:\s+Date.UTC\((\d+),\s+(\d+),\s+(\d+)\),\s+y:\s+(\d+),\s+lineWidth'
	 */
	private static final Pattern EXTRACT_DATA_PATTERN = Pattern
			.compile("name: '(.*)',fillColor: '#FF0033',\\s+x:\\s+Date.UTC\\((\\d+),\\s+(\\d+),\\s+(\\d+)\\),\\s+y:\\s+(\\d+),\\s+lineWidth");

	private static final Logger LOGGER = Logger.getLogger(MoodscopeUpdater.class);

	@Qualifier("connectorUpdateServiceImpl")
	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@Qualifier("apiDataServiceImpl")
	@Autowired
	ApiDataService apiDataService;

	public MoodscopeUpdater() {
		super();
	}

	@Override
	protected void updateConnectorDataHistory(UpdateInfo updateInfo) throws RateLimitReachedException, Exception {
		LOGGER.debug("moodscope-updateConnectorDataHistory");
		apiDataService.eraseApiData(updateInfo.getGuestId(), Connector.getConnector(ConnectorNames.MOODSCOPE), 1);
		sync(updateInfo, 0, System.currentTimeMillis());
	}

	@Override
	public void updateConnectorData(UpdateInfo updateInfo) throws Exception {
		ApiUpdate lastSuccessfulUpdate = connectorUpdateService.getLastSuccessfulUpdate(updateInfo.apiKey.getGuestId(),
				getConnector());

		LOGGER.debug("moodscope-updateConnectorData");
		if (lastSuccessfulUpdate == null) {
			sync(updateInfo, 0, System.currentTimeMillis());
			return;
		}
		loadData(updateInfo, lastSuccessfulUpdate.ts, System.currentTimeMillis());

	}

	/**
	 * Test connection using credentials
	 * 
	 * @param userName
	 * @param password
	 * @throws Exception
	 */
	public void testConnectionAndCredentials(ScrappingConnectorCredentials credentials) throws Exception {
		getConnectedClient(null, credentials);
	}

	private void sync(UpdateInfo updateInfo, long from, long to) throws Exception {
		LOGGER.debug("sync");

		loadData(updateInfo, from, to);
	}

	private void loadData(UpdateInfo updateInfo, long from, long to) throws Exception {

		ScrappingConnectorCredentials credentials = apiDataService.getScrapingConnectorCredentials(
				updateInfo.getGuestId(), updateInfo.apiKey.getConnector());

		HttpClient httpclient = getConnectedClient(updateInfo, credentials);

		long then = System.currentTimeMillis();

		long lastUpdate = from;
		try {
			HttpGet httpGet = new HttpGet(MOODSCOPE_ALL_GET);
			HttpResponse response = httpclient.execute(httpGet);
			HttpEntity responseEntity = response.getEntity();

			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != 200 || responseEntity == null) {
				LOGGER.error("Cannot get information from " + MOODSCOPE_ALL_GET + ". Response is "
						+ statusLine.getReasonPhrase() + " with code " + statusLine.getStatusCode());
				throw new Exception("No answer from Moodscope");
			}

			String rawHtmlData = EntityUtils.toString(responseEntity);

			countSuccessfulApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, MOODSCOPE_ALL_GET,
					rawHtmlData);

			Matcher matcher = EXTRACT_DATA_PATTERN.matcher(rawHtmlData);
			Calendar cal = Calendar.getInstance();
			// if (!matcher.find()) new
			// Exception("No useful info received from Moodscope");
			while (matcher.find()) {
				String desc = matcher.group(1);
				int year = Integer.parseInt(matcher.group(2));
				int month = Integer.parseInt(matcher.group(3));
				int day = Integer.parseInt(matcher.group(4));
				int score = Integer.parseInt(matcher.group(5));

				cal.set(year, month, day);
				Date date = cal.getTime();

				long actualkey = date.getTime();

				if (actualkey > lastUpdate)
					lastUpdate = actualkey;

				if (actualkey <= from) {
					continue;
				}
				long now = System.currentTimeMillis();
				MoodscopeFacet buildMoodscopeFacet = buildMoodscopeFacet(desc, score, actualkey, now);
				apiDataService.cacheApiDataObject(updateInfo, actualkey, actualkey, buildMoodscopeFacet);
			}

		} finally {
			httpclient.getConnectionManager().shutdown();
		}

	}

	private MoodscopeFacet buildMoodscopeFacet(String desc, int score, long actualkey, long timeUpdated) {
		MoodscopeFacet goalFacet = new MoodscopeFacet();
		goalFacet.moodscope_id = actualkey;
		goalFacet.score = score;
		goalFacet.api = getConnector().value();
		goalFacet.objectType = 1;
		goalFacet.fullTextDescription = desc;
		goalFacet.timeUpdated = timeUpdated;
		return goalFacet;
	}

	// TODO: refactor later
	private HttpClient getConnectedClient(UpdateInfo updateInfo, ScrappingConnectorCredentials scrappingCredentials)
			throws Exception {

		long then = System.currentTimeMillis();

		HttpClient httpClient = HttpUtils.wrapClient(new DefaultHttpClient());
		String requestUrl = "request url not set yet";

		try {
			requestUrl = MOODSCOPE_MAIN_URL;
			HttpGet httpGet = new HttpGet(requestUrl);
			HttpResponse firstResponse = httpClient.execute(httpGet);

			StatusLine firstStatusLine = firstResponse.getStatusLine();
			LOGGER.debug("Moodscope home get: " + firstStatusLine);

			HttpEntity firstResponseEntity = firstResponse.getEntity();
			if (firstStatusLine.getStatusCode() != 200 || firstResponseEntity == null) {
				throw new RuntimeException("Invalid response from " + MOODSCOPE_MAIN_URL + ": "
						+ firstStatusLine.getReasonPhrase() + " with code " + firstStatusLine.getStatusCode());
			}

			// it's needed to consume response
			String firstResponseRawContent = EntityUtils.toString(firstResponseEntity);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(MOODSCOPE_MAIN_URL + " response " + firstResponseRawContent);
			}

			List<NameValuePair> credentials = fillUpCredentials(scrappingCredentials);
			requestUrl = LOGIN_URL;
			HttpPost httPost = new HttpPost(requestUrl);
			httPost.setEntity(new UrlEncodedFormEntity(credentials));
			HttpResponse secondResponse = httpClient.execute(httPost);

			StatusLine secondStatusLine = secondResponse.getStatusLine();
			LOGGER.debug("Moodscope home get: " + secondStatusLine);

			// it's needed to consume response
			HttpEntity secondResponseEntity = secondResponse.getEntity();
			if ((secondStatusLine.getStatusCode() != 200 && secondStatusLine.getStatusCode() != 302)
					|| secondResponseEntity == null) {
				throw new RuntimeException("Invalid response from " + LOGIN_URL + ": "
						+ secondStatusLine.getReasonPhrase() + " with code " + secondStatusLine.getStatusCode());
			}

			String secondResponseRawContent = EntityUtils.toString(secondResponseEntity);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(LOGIN_URL + " response " + secondResponseRawContent);
			}

			requestUrl = WELCOME_URL;
			HttpGet welcomeHttpGet = new HttpGet(requestUrl);
			HttpResponse welcomeResponse = httpClient.execute(welcomeHttpGet);

			StatusLine welcomeStatusLine = welcomeResponse.getStatusLine();
			LOGGER.debug("Check form get: " + welcomeStatusLine);

			HttpEntity welcomeResponseEntity = welcomeResponse.getEntity();
			if (welcomeStatusLine.getStatusCode() != 200 || welcomeResponseEntity == null) {
				throw new RuntimeException("Invalid response from " + WELCOME_URL + ": "
						+ welcomeStatusLine.getReasonPhrase() + " with code " + welcomeStatusLine.getStatusCode());
			}

			String welcomePageRawResponse = EntityUtils.toString(welcomeResponseEntity);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("First response " + welcomePageRawResponse);
			}
			// Invalid username/password combination
			if (welcomePageRawResponse.contains(LOGIN_ERROR_MESSAGE)) {
				throw new RuntimeException("Moodscope login error: " + LOGIN_ERROR_MESSAGE);
			}

			return httpClient;
		} catch (Exception e) {
			LOGGER.error("An error occured during retrieving data for moodscope", e);
			if (updateInfo != null) {
				countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl,
						Utils.stackTrace(e));
			}

			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
			throw e;
		}
	}

	private static List<NameValuePair> fillUpCredentials(ScrappingConnectorCredentials credentials) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("_username", credentials.username));
		nvps.add(new BasicNameValuePair("_password", credentials.password));
		nvps.add(new BasicNameValuePair("login", "Login!"));
		nvps.add(new BasicNameValuePair("login.x", "38"));
		nvps.add(new BasicNameValuePair("login.y", "18"));
		return nvps;
	}
}
