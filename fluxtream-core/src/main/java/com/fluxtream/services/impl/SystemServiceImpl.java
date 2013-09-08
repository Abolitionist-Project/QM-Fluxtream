package com.fluxtream.services.impl;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fluxtream.Configuration;
import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.ConnectorNames;
import com.fluxtream.domain.ApiKey;
import com.fluxtream.domain.ConnectorInfo;
import com.fluxtream.services.ConnectorUpdateService;
import com.fluxtream.services.SystemService;
import com.fluxtream.utils.JPAUtils;

@Service
@Transactional
public class SystemServiceImpl implements SystemService {

	@Autowired
	ConnectorUpdateService connectorUpdateService;

	@Autowired
	Configuration env;

	@PersistenceContext
	EntityManager em;

	static Map<String, Connector> scopedApis = new Hashtable<String, Connector>();

	// static {
	// scopedApis.put("https://www.googleapis.com/auth/latitude.all.best",
	// Connector.getConnector("google_latitude"));
	// }

	@Override
	public List<ConnectorInfo> getConnectors() {
		List<ConnectorInfo> all = JPAUtils.find(em, ConnectorInfo.class, "connectors.all", (Object[]) null);
		if (all.size() == 0) {
			initializeConnectorList();
			all = JPAUtils.find(em, ConnectorInfo.class, "connectors.all", (Object[]) null);
		}
		for (ConnectorInfo connectorInfo : all) {
			em.detach(connectorInfo);
			connectorInfo.image = "/" + connectorInfo.image;
		}
		return all;
	}

	private void initializeConnectorList() {
		ResourceBundle res = ResourceBundle.getBundle("messages/connectors");
		int order = 0;

		em.persist(new ConnectorInfo("Google Calendar", "imgs/connectors/connector-google_calendar.jpg", res
				.getString(ConnectorNames.GOOGLE_CALENDAR), "/calendar/token", Connector
				.getConnector(ConnectorNames.GOOGLE_CALENDAR), order++, true));

		em.persist(new ConnectorInfo("Google Latitude", "imgs/connectors/connector-google_latitude.jpg", res
				.getString(ConnectorNames.GOOGLE_LATITUDE), "/google_latitude/token", Connector
				.getConnector(ConnectorNames.GOOGLE_LATITUDE), order++, true));

		em.persist(new ConnectorInfo("Fitbit", "imgs/connectors/connector-fitbit.jpg", res
				.getString(ConnectorNames.FITBIT), "/fitbit/token", Connector.getConnector(ConnectorNames.FITBIT),
				order++, true));

		em.persist(new ConnectorInfo("BodyMedia", "imgs/connectors/connector-bodymedia.jpg", res
				.getString(ConnectorNames.BODYMEDIA), "/bodymedia/token", Connector
				.getConnector(ConnectorNames.BODYMEDIA), order++, true));
		
		em.persist(new ConnectorInfo("BodyMedia", "imgs/connectors/connector-healthvault.jpg", res
				.getString(ConnectorNames.HEALTHVAULT), "/healthvault/token", Connector
				.getConnector(ConnectorNames.HEALTHVAULT), order++, true));

		em.persist(new ConnectorInfo("Lumosity", "imgs/connectors/connector-lumosity.png", res
				.getString(ConnectorNames.LUMOSITY), "ajax:/lumosity/enterCredentials", Connector
				.getConnector(ConnectorNames.LUMOSITY), order++, true));

		em.persist(new ConnectorInfo("Moodscope", "imgs/connectors/connector-moodscope.png", res
				.getString(ConnectorNames.MOODSCOPE), "ajax:/moodscope/enterCredentials", Connector
				.getConnector(ConnectorNames.MOODSCOPE), order++, true));

		em.persist(new ConnectorInfo("MyFitnessPal", "imgs/connectors/connector-myfitnesspal.gif", res
				.getString(ConnectorNames.MYFITNESSPAL), "ajax:/myfitnesspal/enterCredentials", Connector
				.getConnector(ConnectorNames.MYFITNESSPAL), order++, true));
		em.persist(new ConnectorInfo("Withings", "imgs/connectors/connector-withings.jpg", res
				.getString(ConnectorNames.WITHINGS), "/withings/token",
				Connector.getConnector(ConnectorNames.WITHINGS), order++, true));

		em.persist(new ConnectorInfo("Github", "imgs/connectors/connector-github.jpg", res.getString("github"),
				singlyAuthorizeUrl("github"), Connector.getConnector("github"), order++, true));

		// em.persist(new ConnectorInfo("Zeo",
		// "/imgs/connectors/connector-zeo.jpg",
		// res.getString("zeo"),
		// "ajax:/zeo/enterCredentials",
		// Connector.getConnector("zeo"), order++, true));
		// em.persist(new ConnectorInfo("Mymee",
		// "/imgs/connectors/connector-mymee.jpg",
		// res.getString("mymee"),
		// "ajax:/mymee/enterFetchURL",
		// Connector.getConnector("mymee"), order++, true));
		// em.persist(new ConnectorInfo("QuantifiedMind",
		// "/imgs/connectors/connector-quantifiedmind.jpg",
		// res.getString("quantifiedmind"),
		// "ajax:/quantifiedmind/getTokenDialog",
		// Connector.getConnector("quantifiedmind"), order++, true));
		// Interfacing with Picasa has been so problematic we've decided to just
		// disable it. Do so by simply commenting
		// it out. We'll keep the supporting classes around in case we change
		// our minds.
		// em.persist(new ConnectorInfo("Picasa",
		// "/imgs/connectors/connector-picasa.jpg",
		// res.getString("picasa"),
		// "/picasa/token",
		// Connector.getConnector("picasa"), order++, true));
		// em.persist(new ConnectorInfo("Flickr",
		// "/imgs/connectors/connector-flickr.jpg",
		// res.getString("flickr"),
		// "/flickr/token",
		// Connector.getConnector("flickr"), order++, true));
		// em.persist(new ConnectorInfo("Last fm",
		// "/imgs/connectors/connector-lastfm.jpg",
		// res.getString("lastfm"),
		// "/lastfm/token",
		// Connector.getConnector("lastfm"), order++, true));
		// em.persist(new ConnectorInfo("Twitter",
		// "/imgs/connectors/connector-twitter.jpg",
		// res.getString("twitter"), "/twitter/token",
		// Connector.getConnector("twitter"), order++, true));
		// em.persist(new ConnectorInfo("Fluxtream Capture",
		// "/imgs/connectors/connector-fluxtream_capture.png",
		// res.getString("fluxtream_capture"),
		// "ajax:/fluxtream_capture/about",
		// Connector.getConnector("fluxtream_capture"), order++, true));
		// em.persist(new ConnectorInfo("RunKeeper",
		// "/imgs/connectors/connector-runkeeper.jpg",
		// res.getString("runkeeper"),
		// "/runkeeper/token",
		// Connector.getConnector("runkeeper"), order++, true));
		// em.persist(new ConnectorInfo("Evernote",
		// "/imgs/connectors/connector-evernote.jpg",
		// res.getString("evernote"),
		// "/evernote/token",
		// Connector.getConnector("evernote"), order, false));
	}

	private String singlyAuthorizeUrl(final String service) {
		return "https://api.singly.com/oauth/authorize?client_id=" + env.get("singly.client.id") + "&redirect_uri="
				+ env.get("homeBaseUrl") + "singly/" + service + "/callback&service=" + service;
	}

	@Override
	public Connector getApiFromGoogleScope(String scope) {
		return scopedApis.get(scope);
	}

	@Override
	public void deleteApiKey(ApiKey apiKey) {
		removeApiKeyAttrs(apiKey);
		JPAUtils.execute(em, "apiKeys.delete", apiKey.getId());
	}

	private void removeApiKeyAttrs(ApiKey apiKey) {
		for (String attribute : apiKey.getAttributes(env).keySet()) {
			JPAUtils.execute(em, "apiKeyAttribute.delete", apiKey, attribute);
		}
	}

}
