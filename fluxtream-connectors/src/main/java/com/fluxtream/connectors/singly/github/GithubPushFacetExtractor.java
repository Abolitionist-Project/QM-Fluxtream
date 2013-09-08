package com.fluxtream.connectors.singly.github;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fluxtream.ApiData;
import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.facets.extractors.AbstractFacetExtractor;
import com.fluxtream.services.GuestService;

@Component
public class GithubPushFacetExtractor extends AbstractFacetExtractor {

	private static final String PUSH_EVENT = "PushEvent";

	@Autowired
	GuestService guestService;

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

	@Override
	public List<AbstractFacet> extractFacets(final ApiData apiData, final ObjectType objectType) throws Exception {
		List<AbstractFacet> facets = new ArrayList<AbstractFacet>();

		String login = guestService.getApiKeyAttribute(apiData.updateInfo.getGuestId(),
				Connector.getConnector("github"), "login");

		JSONArray eventsArray = JSONArray.fromObject(apiData.json);
		for (int i = 0; i < eventsArray.size(); i++) {
			JSONObject eventData = eventsArray.getJSONObject(i).getJSONObject("data");

			if (eventData == null || !PUSH_EVENT.equals(eventData.getString("type"))) {
				continue;
			}

			JSONObject payload = eventData.getJSONObject("payload");
			if (payload == null || skipEventData(eventData, login)) {
				continue;
			}

			GithubPushFacet facet = new GithubPushFacet();
			this.extractCommonFacetData(facet, apiData);
			String timestamp = eventData.getString("created_at");
			facet.start = dateTimeFormatter.parseDateTime(timestamp).getMillis();
			facet.end = facet.start;

			if (payload.has("commits")) {
				facet.commitsJSON = payload.getJSONArray("commits").toString();
			} else
				facet.commitsJSON = "{}";

			JSONObject repo = eventData.getJSONObject("repo");

			if (repo != null) {
				facet.repoName = repo.getString("name");
				facet.repoURL = repo.getString("url");
			}

			facets.add(facet);

		}

		return facets;
	}

	private boolean skipEventData(JSONObject eventData, String login) {
		JSONObject actor = eventData.getJSONObject("actor");
		return actor == null || !login.equals(actor.getString("login"));
	}
}
