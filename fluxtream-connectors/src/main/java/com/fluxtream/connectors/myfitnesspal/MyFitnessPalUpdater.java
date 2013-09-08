package com.fluxtream.connectors.myfitnesspal;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

/*
 * Weight: http://www.myfitnesspal.com/reports/results/progress/1/7 Neck:
 * http://www.myfitnesspal.com/reports/results/progress/2/7 Waist:
 * http://www.myfitnesspal.com/reports/results/progress/3/7 Hips:
 * http://www.myfitnesspal.com/reports/results/progress/4/7
 * 
 * Fitness:
 * http://www.myfitnesspal.com/reports/results/fitness/Calories%20Burned/7
 * http://www.myfitnesspal.com/reports/results/fitness/Exercise%20Minutes/7
 * 
 * Nutrition: http://www.myfitnesspal.com/reports/results/nutrition/Calories/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Net%20Calories/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Carbs/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Protein/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Saturated%20Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Polyunsaturated%20Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Monounsaturated%20Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Trans%20Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Cholesterol/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Sodium/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Potassium/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Fiber/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Sugar/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Vitamin%20A/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Vitamin%20C/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Iron/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Calcium/7
 */

@Component
@Updater(prettyName = "MyFitnessPal", value = 103, objectTypes = { MyFitnessPalFacet.class }, updateStrategyType = UpdateStrategyType.ALWAYS_UPDATE)
public class MyFitnessPalUpdater extends AbstractUpdater {

	private static abstract class LoadFunction {

		private void preProcessData(Map<String, MyFitnessPalFacet> facets, String key, long timeMs) {
			if (!facets.containsKey(key)) {
				facets.put(key, new MyFitnessPalFacet());
			}

			facets.get(key).start = timeMs;
			facets.get(key).end = timeMs;
		}

		public void loadData(Map<String, MyFitnessPalFacet> facets, String key, String param, Long timeMs) {
			preProcessData(facets, key, timeMs);
			processData(facets, key, param);
		}

		protected abstract void processData(Map<String, MyFitnessPalFacet> facets, String key, String param);
	}

	private static final Map<String, LoadFunction> API_URLS_AND_FUNCTIONS = new HashMap<String, LoadFunction>() {

		private static final long serialVersionUID = 920201513644688747L;

		{
			put("http://www.myfitnesspal.com/reports/results/progress/1/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).weight = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/progress/2/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).neck = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/progress/3/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).waist = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/progress/4/30", new LoadFunction() {
				@Override
				public void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).hips = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/fitness/Calories%20Burned/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).caloriesBurned = Integer.parseInt(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/fitness/Exercise%20Minutes/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).exerciseMins = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Calories/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).calories = Integer.parseInt(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Net%20Calories/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).netCalories = Integer.parseInt(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Carbs/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).carbs = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Fat/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).protein = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Saturated%20Fat/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).saturatedFat = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Polyunsaturated%20Fat/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).polyunsaturatedFat = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Monounsaturated%20Fat/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).monounsaturatedFat = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Trans%20Fat/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).transFat = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Cholesterol/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).cholesterol = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Sodium/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).sodium = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Potassium/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).potassium = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Fiber/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).fiber = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Sugar/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).sugar = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Vitamin%20A/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).vitaminA = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Vitamin%20C/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).vitaminC = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Iron/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).iron = Float.parseFloat(param);
				}
			});

			put("http://www.myfitnesspal.com/reports/results/nutrition/Calcium/30", new LoadFunction() {
				@Override
				protected void processData(Map<String, MyFitnessPalFacet> facets, String key, String param) {
					facets.get(key).calcium = Float.parseFloat(param);
				}
			});
		}
	};

	private static final String BASE_URL = "http://www.myfitnesspal.com/";
	private static final String LOGIN_ERROR_MESSAGE = "Incorrect username or password";
	private static final String LOGIN_URL = "https://www.myfitnesspal.com/account/login";
	private static final Pattern EXTRACT_DATA_PATTERN = Pattern
			.compile("authenticity_token\\\" type=\\\"hidden\\\" value=\\\"(.+)\\\"");

	private static final Logger LOGGER = Logger.getLogger(MyFitnessPalUpdater.class);

	@Qualifier("connectorUpdateServiceImpl")
	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@Qualifier("apiDataServiceImpl")
	@Autowired
	ApiDataService apiDataService;

	@PersistenceContext
	EntityManager em;

	public MyFitnessPalUpdater() {
		super();
	}

	@Override
	protected void updateConnectorDataHistory(UpdateInfo updateInfo) throws RateLimitReachedException, Exception {
		LOGGER.debug("myfitnesspal-updateConnectorDataHistory");
		apiDataService.eraseApiData(updateInfo.getGuestId(), Connector.getConnector(ConnectorNames.MYFITNESSPAL), 1);
		sync(updateInfo, 0, System.currentTimeMillis());
	}

	@Override
	public void updateConnectorData(UpdateInfo updateInfo) throws Exception {
		ApiUpdate lastSuccessfulUpdate = connectorUpdateService.getLastSuccessfulUpdate(updateInfo.apiKey.getGuestId(),
				getConnector());
		LOGGER.debug("myfitnesspal-updateConnectorData");
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

			LOGGER.debug("myfitnesspal-sync-" + credentials.username);

			Map<String, MyFitnessPalFacet> facets = retrieveScores(updateInfo, credentials, from, to);

			LOGGER.debug("myfitnesspal-sync-" + facets);
			for (MyFitnessPalFacet facet : facets.values()) {
				apiDataService.cacheApiDataObject(updateInfo, facet.start, facet.end, facet);
			}

		} catch (Exception e) {
			countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, to, BASE_URL,
					Utils.stackTrace(e));
			throw e;
		}
	}

	private void parseXmlResponse(Map<String, MyFitnessPalFacet> facets, String xmlString, LoadFunction loadFunction)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		Document doc = buildDocument(xmlString);
		XPathFactory xFactory = XPathFactory.newInstance();
		XPath xpath = xFactory.newXPath();

		XPathExpression parseDatesExpr = xpath.compile("//chart/chart_data/row[1]/string/text()");
		NodeList datesNodes = (NodeList) parseDatesExpr.evaluate(doc, XPathConstants.NODESET);

		XPathExpression parseNodesExpr = xpath.compile("//chart/chart_data/row[2]/number/text()");
		NodeList valsNodes = (NodeList) parseNodesExpr.evaluate(doc, XPathConstants.NODESET);

		Calendar now = new GregorianCalendar();
		int nowDay = now.get(Calendar.DAY_OF_MONTH);
		int nowMonth = now.get(Calendar.MONTH);
		int year = now.get(Calendar.YEAR);
		/*
		 * NOTE: the myfitnesspal data have not the year inside, so what happens
		 * if i ask data which go back to the former year ? example 01/02,
		 * 01/01, 12/31, 12/30..... so i have to monitor in which position i
		 * find the 12/31 and decide if it is this year or the former one. The
		 * only case in which the year is not changing is when it is the 12/31
		 * and i'm asking for data which contains this day
		 */
		boolean isLastDay = (nowDay == 31 && nowMonth == 12);

		for (int i = datesNodes.getLength() - 1; i >= 0; i--) {

			String key = datesNodes.item(i).getNodeValue();
			String[] date = key.split("/");
			int day = Integer.parseInt(date[1]);
			int month = Integer.parseInt(date[0]);
			if (day == 31 && month == 12 && !isLastDay) {
				year--;
			}

			Calendar calendar = new GregorianCalendar(year, month, day);

			String nodeValue = valsNodes.item(i).getNodeValue();
			loadFunction.loadData(facets, key, nodeValue, calendar.getTimeInMillis());

		}

	}

	private Document buildDocument(String xmlString) throws ParserConfigurationException, SAXException, IOException {
		// Standard of reading a XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xmlString));
		Document doc = builder.parse(is);
		return doc;
	}

	private Map<String, MyFitnessPalFacet> retrieveScores(UpdateInfo updateInfo,
			ScrappingConnectorCredentials credentials, long from, long to) throws RateLimitReachedException, Exception {
		Map<String, MyFitnessPalFacet> facets = new HashMap<String, MyFitnessPalFacet>();
		HttpClient httpClient = getConnectedClient(updateInfo, credentials);
		long guestId = updateInfo.getGuestId();
		long then = System.currentTimeMillis();
		String requestUrl = "";
		try {
			for (Map.Entry<String, LoadFunction> entry : API_URLS_AND_FUNCTIONS.entrySet()) {
				requestUrl = entry.getKey();
				String xmlResponse = request(httpClient, new HttpGet(requestUrl));
				parseXmlResponse(facets, xmlResponse, entry.getValue());
				countSuccessfulApiCall(guestId, updateInfo.objectTypes, then, requestUrl, xmlResponse);
			}
			return facets;
		} catch (Exception e) {
			countFailedApiCall(guestId, updateInfo.objectTypes, then, Utils.stackTrace(e), requestUrl);
			throw e;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public void testConnection(ScrappingConnectorCredentials credentials, long guestId) throws Exception {

		HttpClient httpclient = null;
		try {
			httpclient = getConnectedClient(null, credentials);
		} catch (Exception e) {
			countFailedApiCall(guestId, 0, System.currentTimeMillis(), "MyFitnessPalUpdater::testConnection",
					Utils.stackTrace(e));
			throw e;
		} finally {
			if (httpclient != null) {
				httpclient.getConnectionManager().shutdown();
			}
		}
	}

	private HttpClient getConnectedClient(UpdateInfo updateInfo, ScrappingConnectorCredentials credentials)
			throws Exception {
		long then = System.currentTimeMillis();
		String requestUrl = "request url not set yet";
		HttpClient httpClient = HttpUtils.wrapClient(new DefaultHttpClient());
		try {
			requestUrl = BASE_URL;
			HttpGet httpGet = new HttpGet(requestUrl);

			String formPage = request(httpClient, httpGet);

			Matcher matcher = EXTRACT_DATA_PATTERN.matcher(formPage);

			if (!matcher.find()) {
				throw new RuntimeException("Invalid Form Received from Login Page: " + formPage);
			}

			String myToken = matcher.group(1);

			HttpPost httpost = new HttpPost(LOGIN_URL);

			List<NameValuePair> nvps = fillData(credentials.username, credentials.password, myToken);

			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			String answerPage = request(httpClient, httpost);

			// Invalid username/password combination
			if (answerPage.contains(LOGIN_ERROR_MESSAGE)) {
				throw new RuntimeException("MyFitnessPal login error: " + LOGIN_ERROR_MESSAGE);
			}
			return httpClient;
		} catch (Exception e) {
			LOGGER.error("An error occured during retrieving data for myfitnesspal", e);
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
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("remember_me", "1"));
		return nvps;
	}
}
