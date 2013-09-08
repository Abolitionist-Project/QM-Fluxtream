package com.fluxtream.connectors.updaters;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import com.fluxtream.domain.ApiKey;

public abstract class AbstractGoogleOAuthUpdater extends AbstractUpdater {

	protected static final long TIME_SHIFT = 10;

	protected final static DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

	public AbstractGoogleOAuthUpdater() {
		super();
	}

	protected String getAccessToken(ApiKey apiKey) {
		return apiKey.getAttributeValue("accessToken", env);
	}

	protected String getTokenSecret(ApiKey apiKey) {
		return apiKey.getAttributeValue("tokenSecret", env);
	}

	protected String getRefreshToken(ApiKey apiKey) {
		return apiKey.getAttributeValue("refreshToken", env);
	}

	protected long getExpiresInSeconds(ApiKey apiKey) {
		String attributeValue = apiKey.getAttributeValue("expiresInSeconds", env);
		return StringUtils.hasText(attributeValue) ? Long.parseLong(attributeValue) : 0;
	}

	protected String getConsumerKey() {
		return env.get("googleConsumerKey");
	}

	protected String getConsumerSecret() {
		return env.get("googleConsumerSecret");
	}

}
