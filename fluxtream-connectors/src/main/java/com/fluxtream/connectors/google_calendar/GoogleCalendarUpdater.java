package com.fluxtream.connectors.google_calendar;

import java.io.IOException;
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
import com.fluxtream.services.GuestService;
import com.fluxtream.services.MetadataService;
import com.fluxtream.services.SystemService;
import com.fluxtream.utils.Utils;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

@Component
@Updater(prettyName = "Calendar", value = 0, updateStrategyType = UpdateStrategyType.ALWAYS_UPDATE, objectTypes = { GoogleCalendarEntryFacet.class })
@JsonFacetCollection(GoogleCalendarFacetVOCollection.class)
public class GoogleCalendarUpdater extends AbstractGoogleOAuthUpdater {

	private static final Logger LOG = Logger.getLogger(GoogleCalendarUpdater.class);

	@Autowired
	MetadataService metadataService;

	@Autowired
	GuestService guestService;

	@Autowired
	SystemService systemService;

	public GoogleCalendarUpdater() {
		super();
	}

	@Override
	protected void updateConnectorDataHistory(UpdateInfo updateInfo) throws Exception {
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
			refreshAcessToken(updateInfo, refreshToken, ConnectorNames.GOOGLE_CALENDAR);
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

		long then = System.currentTimeMillis();
		String requestUrl = "request url not set yet";

		try {
			Calendar calendar = buildGoogleCalendar(updateInfo);

			com.google.api.services.calendar.Calendar.CalendarList.List request = calendar.calendarList().list();
			requestUrl = request.buildHttpRequestUrl().build();
			CalendarList calendarFeed = request.execute();
			List<CalendarListEntry> entries = calendarFeed.getItems();

			if (CollectionUtils.isEmpty(entries)) {
				throw new Exception("null entries when loading google calendar history");
			}
			for (CalendarListEntry entry : entries) {
				DateTime timeMin = new DateTime(from);
				DateTime timeMax = new DateTime(to);
				Events events = calendar.events().list(entry.getId()).setTimeMin(timeMin).setTimeMax(timeMax).execute();
				List<Event> eventItems = events.getItems();
				if (eventItems == null) {
					continue;
				}
				processEventItems(updateInfo.getGuestId(), updateInfo, eventItems);
			}
			countSuccessfulApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl,
					calendarFeed.toPrettyString());
		} catch (Exception e) {
			countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, requestUrl,
					Utils.stackTrace(e));
			throw e;
		}
	}

	private void processEventItems(long guestId, UpdateInfo updateInfo, List<Event> eventItems) throws Exception {
		for (Event event : eventItems) {
			EventDateTime startTime = event.getStart();
			if (startTime == null) {
				LOG.error("GoogleCalendarUpdater.loadHistory(): CalendarEventEntry times is empty for event ["
						+ event.getSummary() + "]");
				continue;
			}
			EventDateTime endTime = event.getEnd();
			GoogleCalendarEntryFacet calendarFacet = new GoogleCalendarEntryFacet(event);
			DateTime startDate = startTime.getDate();
			calendarFacet.start = startTime.getDateTime() != null ? startTime.getDateTime().getValue() : startDate
					.getValue();
			DateTime endDate = endTime.getDate();
			calendarFacet.end = endTime.getDateTime() != null ? endTime.getDateTime().getValue() : endDate.getValue();
			calendarFacet.guestId = guestId;
			apiDataService.cacheApiDataObject(updateInfo, calendarFacet.start, calendarFacet.end, calendarFacet);
		}
	}

	private Calendar buildGoogleCalendar(UpdateInfo updateInfo) {
		Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
				.setAccessToken(getAccessToken(updateInfo.apiKey));
		NetHttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		Calendar calendar = new Calendar.Builder(httpTransport, jsonFactory, credential).build();
		return calendar;
	}
}
