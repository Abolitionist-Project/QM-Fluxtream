package com.fluxtream.connectors.withings;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fluxtream.connectors.annotations.JsonFacetCollection;
import com.fluxtream.connectors.annotations.Updater;
import com.fluxtream.connectors.updaters.AbstractUpdater;
import com.fluxtream.connectors.updaters.UpdateInfo;
import com.fluxtream.domain.ApiUpdate;
import com.fluxtream.utils.Utils;

@Component
@Updater(prettyName = "Withings", value = 4, objectTypes = { WithingsBpmMeasureFacet.class,
		WithingsBodyScaleMeasureFacet.class }, extractor = WithingsFacetExtractor.class, defaultChannels = {
		"Withings.weight", "Withings.systolic", "Withings.diastolic", "Withings.heartPulse" })
@JsonFacetCollection(WithingsFacetVOCollection.class)
public class WithingsUpdater extends AbstractUpdater {

	private static final String WITHING_URL = "http://wbsapi.withings.net/measure?action=getmeas";

	@Autowired
	WithingsOAuthController withingsOAuthController;

	public WithingsUpdater() {
		super();
	}

	@Override
	protected void updateConnectorDataHistory(UpdateInfo updateInfo) throws Exception {
		fetchWithingsData(updateInfo, 0);
	}

	@Override
	public void updateConnectorData(UpdateInfo updateInfo) throws Exception {
		ApiUpdate lastSuccessfulUpdate = connectorUpdateService.getLastSuccessfulUpdate(updateInfo.apiKey.getGuestId(),
				getConnector());
		fetchWithingsData(updateInfo, lastSuccessfulUpdate.ts / 1000);
	}

	private void fetchWithingsData(final UpdateInfo updateInfo, long since) throws Exception {
		long then = System.currentTimeMillis();

		OAuthRequest request = new OAuthRequest(Verb.GET, WITHING_URL);
		request.addQuerystringParameter("userid", updateInfo.apiKey.getAttributeValue("userid", env));
		request.addQuerystringParameter("startdate", String.valueOf(since));
		long end = System.currentTimeMillis();

		Token accessToken = new Token(updateInfo.apiKey.getAttributeValue("accessToken", env),
				updateInfo.apiKey.getAttributeValue("tokenSecret", env));

		long guestId = updateInfo.apiKey.getGuestId();
		try {
			withingsOAuthController.getOAuthService().signRequest(accessToken, request);
			System.out.println("Withings: " + request.getCompleteUrl());

			Response response = request.send();
			String body = response.getBody();
			if (response.getCode() != 200) {
				throw new RuntimeException("An error occured while retrieving data for withings: " + body);
			}
			countSuccessfulApiCall(guestId, updateInfo.objectTypes, then, WITHING_URL, body);
			apiDataService.cacheApiDataJSON(updateInfo, body, since, end);
		} catch (Exception e) {
			countFailedApiCall(guestId, updateInfo.objectTypes, then, WITHING_URL, Utils.stackTrace(e));
			throw e;
		}

	}

}
