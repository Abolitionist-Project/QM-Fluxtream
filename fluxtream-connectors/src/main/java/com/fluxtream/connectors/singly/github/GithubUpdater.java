package com.fluxtream.connectors.singly.github;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.annotations.Updater;
import com.fluxtream.connectors.updaters.AbstractUpdater;
import com.fluxtream.connectors.updaters.UpdateInfo;
import com.fluxtream.domain.ApiUpdate;
import com.fluxtream.services.GuestService;
import com.fluxtream.utils.HttpUtils;
import com.fluxtream.utils.Utils;

@Component
@Updater(prettyName = "Github", value = 200, updateStrategyType = Connector.UpdateStrategyType.INCREMENTAL, objectTypes = { GithubPushFacet.class }, extractor = GithubPushFacetExtractor.class)
public class GithubUpdater extends AbstractUpdater {

	private static final String GITHUB_EVENTS = "https://api.singly.com/services/github/events";
	@Autowired
	GuestService guestService;

	public GithubUpdater() {
		super();
	}

	@Override
	protected void updateConnectorDataHistory(final UpdateInfo updateInfo) throws Exception {
		if (!connectorUpdateService.isHistoryUpdateCompleted(updateInfo.getGuestId(), getConnector().getName(),
				updateInfo.objectTypes)) {
			apiDataService.eraseApiData(updateInfo.getGuestId(), getConnector());
		}
		loadHistory(updateInfo, 0);
	}

	@Override
	public void updateConnectorData(UpdateInfo updateInfo) throws Exception {
		ApiUpdate lastUpdate = connectorUpdateService.getLastSuccessfulUpdate(updateInfo.apiKey.getGuestId(),
				getConnector());
		loadHistory(updateInfo, lastUpdate.ts);
	}

	private void loadHistory(UpdateInfo updateInfo, long from) throws Exception {
		String queryUrl = "request url not set yet";
		long then = System.currentTimeMillis();

		String accessToken = guestService.getApiKeyAttribute(updateInfo.getGuestId(), Connector.getConnector("github"),
				"accessToken");

		try {
			queryUrl = GITHUB_EVENTS + "?access_token=" + accessToken;
			final String json = HttpUtils.fetch(queryUrl);
			apiDataService.cacheApiDataJSON(updateInfo, json, -1, -1);
			countSuccessfulApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, queryUrl, json);
		} catch (Exception e) {
			countFailedApiCall(updateInfo.apiKey.getGuestId(), updateInfo.objectTypes, then, queryUrl,
					Utils.stackTrace(e));
			throw new Exception("Could not get GitHub Commits (from Singly): " + e.getMessage() + "\n"
					+ Utils.stackTrace(e));
		}

	}

}