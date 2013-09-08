package com.fluxtream.connectors.google_latitude;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.Connector.UpdateStrategyType;
import com.fluxtream.connectors.ConnectorNames;
import com.fluxtream.connectors.annotations.JsonFacetCollection;
import com.fluxtream.connectors.annotations.Updater;
import com.fluxtream.connectors.controllers.BaseGoogleOAuthController;
import com.fluxtream.connectors.updaters.AbstractGoogleOAuthUpdater;
import com.fluxtream.connectors.updaters.UpdateInfo;
import com.fluxtream.domain.ApiUpdate;
import com.fluxtream.domain.Guest;
import com.fluxtream.services.ApiDataService;
import com.fluxtream.services.GuestService;
import com.fluxtream.services.SystemService;
import com.fluxtream.utils.Utils;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.latitude.Latitude;
import com.google.api.services.latitude.LatitudeRequest;
import com.google.api.services.latitude.LatitudeRequestInitializer;
import com.google.api.services.latitude.model.Location;
import com.google.api.services.latitude.model.LocationFeed;

@Component
@Updater(prettyName = "Latitude", value = 2, objectTypes = { LocationFacet.class }, updateStrategyType = UpdateStrategyType.ALWAYS_UPDATE)
@JsonFacetCollection(LocationFacetVOCollection.class)
public class GoogleLatitudeUpdater extends AbstractGoogleOAuthUpdater {

	private static final Logger LOG = Logger.getLogger(GoogleLatitudeUpdater.class);

	@Autowired
	GuestService guestService;

	@Autowired
	ApiDataService apiDataService;

	@Autowired
	SystemService systemService;

	public GoogleLatitudeUpdater() {
		super();
	}

	@Override
	public void updateConnectorDataHistory(UpdateInfo updateInfo) throws Exception {
		LOG.debug("Calendar-updateConnectorDataHistory");
		loadHistory(updateInfo, 0, System.currentTimeMillis());
	}

	@Override
	public void updateConnectorData(UpdateInfo updateInfo) throws Exception {
		ApiUpdate lastSuccessfulUpdate = connectorUpdateService.getLastSuccessfulUpdate(updateInfo.apiKey.getGuestId(),
				getConnector());
		long currentTimeMillis = System.currentTimeMillis();
		if ((currentTimeMillis - lastSuccessfulUpdate.ts) / 1000 + TIME_SHIFT >= getExpiresInSeconds(updateInfo.apiKey)) {
			String refreshToken = getRefreshToken(updateInfo.apiKey);
			if (!StringUtils.hasText(refreshToken)) {
				systemService.deleteApiKey(updateInfo.apiKey);
				return;
			}
			refreshAcessToken(updateInfo, refreshToken, ConnectorNames.GOOGLE_LATITUDE);
		}
		loadHistory(updateInfo, lastSuccessfulUpdate.ts, currentTimeMillis);
	}

	protected void refreshAcessToken(UpdateInfo updateInfo, String refreshToken, String connectorName)
			throws IOException {
		Connector connector = Connector.getConnector(connectorName);

		Guest guest = guestService.getGuestById(updateInfo.getGuestId());

		BaseGoogleOAuthController.refreshToken(guestService, getAccessToken(updateInfo.apiKey), refreshToken,
				getConsumerKey(), getConsumerSecret(), connector, guest);
	}

	private void loadHistory(UpdateInfo updateInfo, long from, long to) throws Exception {
		List<LocationFacet> locationList = getLocationFacets(updateInfo, 1000, from, to);

		if (CollectionUtils.isEmpty(locationList)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No new locations were found for " + updateInfo.getGuestId() + " from " + from + " till "
						+ to);
			}
			return;
		}
		List<LocationFacet> storedLocations = new ArrayList<LocationFacet>();
		for (LocationFacet locationResource : locationList) {
			if (locationResource.timestampMs == 0) {
				continue;
			}
			locationResource.start = locationResource.timestampMs;
			locationResource.end = locationResource.timestampMs;
			locationResource.source = LocationFacet.Source.GOOGLE_LATITUDE;
			locationResource.guestId = updateInfo.getGuestId();

			apiDataService.addGuestLocation(updateInfo.getGuestId(), locationResource);

			storedLocations.add(locationResource);
		}
		Collections.sort(storedLocations);
		LocationFacet oldest = storedLocations.get(0);
		loadHistory(updateInfo, from, oldest.timestampMs - 1000);

	}

	private List<LocationFacet> getLocationFacets(UpdateInfo updateInfo, int maxResults, long minTime, long maxTime)
			throws Exception {
		long then = System.currentTimeMillis();
		String requestUrl = "request url not set yet";
		try {
			Latitude latitude = buildGoogleLatitude(updateInfo);
			Latitude.Location.List request = latitude.location().list();
			request.setGranularity("best");
			request.setMinTime(minTime + "");
			request.setMaxTime(maxTime + "");
			request.setMaxResults(maxResults + "");
			requestUrl = request.buildHttpRequestUrl().build();
			LocationFeed locationFeed = request.execute();

			List<LocationFacet> result = new ArrayList<LocationFacet>();
			List<Location> locationItems = locationFeed.getItems();
			countSuccessfulApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl,
					locationFeed.toPrettyString());
			if (CollectionUtils.isEmpty(locationItems)) {
				return result;
			}
			for (Location location : locationItems) {
				convertLocationToFacet(result, location);
			}

			return result;
		} catch (Exception e) {
			countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl,
					Utils.stackTrace(e));
			throw e;
		}
	}

	private void convertLocationToFacet(List<LocationFacet> result, Location location) {
		LocationFacet facet = new LocationFacet();
		facet.accuracy = Utils.safelyParseToInteger(location.getAccuracy());
		facet.altitude = Utils.safelyParseToInteger(location.getAltitude());
		facet.altitudeAccuracy = Utils.safelyParseToInteger(location.getAltitudeAccuracy());
		facet.latitude = Utils.safelyParseToFloat(location.getLatitude());
		facet.longitude = Utils.safelyParseToFloat(location.getLongitude());
		facet.timestampMs = Utils.safelyParseToLong(location.getTimestampMs());
		facet.speed = Utils.safelyParseToInteger(location.getSpeed());
		facet.heading = Utils.safelyParseToInteger(location.getHeading());
		result.add(facet);
	}

	private Latitude buildGoogleLatitude(UpdateInfo updateInfo) {
		String accessToken = getAccessToken(updateInfo.apiKey);
		Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
				.setAccessToken(accessToken);
		NetHttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		return new Latitude.Builder(httpTransport, jsonFactory, credential).setGoogleClientRequestInitializer(
				new LatitudeRequestInitializer() {
					@Override
					public void initializeLatitudeRequest(LatitudeRequest<?> request) {
						request.setPrettyPrint(true);
					}
				}).build();
	}

}
