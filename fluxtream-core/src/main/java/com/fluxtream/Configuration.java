package com.fluxtream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.WordUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import com.fluxtream.domain.Guest;
import com.fluxtream.utils.DesEncrypter;
import com.google.api.client.http.LowLevelHttpRequest;

public class Configuration implements InitializingBean {

	public static final int PASSWORD_MIN_LENGTH = 8;

	public static final DateTimeZone DEFAULT_TIMEZONE = DateTimeZone.UTC;

	public static final String GLOBAL_DATA_OWNER = "database.defaults.global_data_owner";

	private static final Logger LOG = Logger.getLogger(Configuration.class);

	private DesEncrypter encrypter;

	public PropertiesConfiguration commonProperties;
	public PropertiesConfiguration bodytrackProperties;
	public PropertiesConfiguration targetEnvironmentProps;
	public PropertiesConfiguration connectors;
	public PropertiesConfiguration oauth;

	private Map<String, String> countries;
	private Map<String, String> countryCodes;

	public Map<String, String> bodytrackToFluxtreamConnectorNames;

	// TODO: change it to real timezone if needed
	public DateTimeZone getTimeZoneForGuest(Guest guest) {
		return Configuration.DEFAULT_TIMEZONE;
	}

	public void setCommonProperties(PropertiesConfiguration properties) throws IOException {
		this.commonProperties = properties;
	}

	public void setTargetEnvProperties(PropertiesConfiguration properties) throws IOException {
		this.targetEnvironmentProps = properties;
	}

	public void setConnectorsProperties(PropertiesConfiguration properties) throws IOException {
		this.connectors = properties;
	}

	public void setOauthProperties(PropertiesConfiguration properties) throws IOException {
		this.oauth = properties;
	}

	public void setBodytrackProperties(PropertiesConfiguration properties) throws IOException {
		final Iterator<String> keys = properties.getKeys();
		bodytrackToFluxtreamConnectorNames = new ConcurrentHashMap<String, String>();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.indexOf(".dev_nickname") != -1) {
				bodytrackToFluxtreamConnectorNames.put(properties.getString(key), key.substring(0, key.indexOf(".")));
			}
		}
		this.bodytrackProperties = properties;
	}

	public void setCountries(Properties properties) throws IOException {
		countries = new ConcurrentHashMap<String, String>();
		countryCodes = new ConcurrentHashMap<String, String>();
		for (Object key : properties.keySet()) {
			String code = (String) key;
			String countryName = properties.getProperty(code);
			String capitalizedCountryName = WordUtils.capitalize(countryName.toLowerCase());
			String upperCaseCountryCode = code.toUpperCase();
			countries.put(upperCaseCountryCode, capitalizedCountryName);
			countryCodes.put(capitalizedCountryName, upperCaseCountryCode);
		}
	}

	public String encrypt(String s) {
		return encrypter.encrypt(s);
	}

	public String decrypt(String s) {
		return encrypter.decrypt(s);
	}

	public String get(String key) {
		String property = getAsString(commonProperties, key);
		if (property == null) property = getAsString(targetEnvironmentProps, key);
		if (property == null) property = getAsString(oauth, key);
		if (property == null) property = getAsString(connectors, key);
		if (property == null) property = getAsString(bodytrackProperties, key);
		if (property == null) LOG.warn("Property for " + key + " is null");
		return StringUtils.trimWhitespace(property);
	}

	public String getHomeBaseUrl() {
		return get("homeBaseUrl");
	}

	private String getAsString(PropertiesConfiguration properties, String key) {
		if (properties == null) {
			LOG.error("Properties are null");
			return null;
		}
		final Object property = properties.getProperty(key);
		if (property == null) {
			return null;
		}
		if (!(property instanceof String)) {
			LOG.error("Property " + key + " was supposed to be a String, found " + property.getClass());
			return "";
		}
		return (String) property;
	}

	public Integer getInt(String key) {
		return Integer.valueOf(get(key));
	}

	public Long getLong(String key) {
		return Long.valueOf(get(key));
	}

	public Float getFloat(String key) {
		return Float.valueOf(get(key));
	}

	/**
	 * Get property as list of Integer for values which is separated by ; in
	 * properties
	 * 
	 * @param key
	 * @return
	 */
	public List<Integer> getAsIntList(String key) {
		String[] values = get(key).split(";");
		List<Integer> list = new ArrayList<Integer>();
		for (String value : values) {
			list.add(Integer.parseInt(value));
		}
		return list;
	}

	public HttpClient getHttpClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		return client;
	}

	public void setProxyAuthHeaders(LowLevelHttpRequest request) throws IOException {
		if (get("proxyUser") == null) {
			return;
		}
		String credentials = get("proxyUser") + ":" + get("proxyPassword");
		String encodedPassword = new String(Base64.encodeBase64(credentials.getBytes()));
		request.addHeader("Proxy-Authorization", "Basic " + encodedPassword);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.encrypter = new DesEncrypter(get("crypto"));
	}

	public String getCountry(String geo_country_code) {
		return countries.get(geo_country_code.toUpperCase());
	}

	public String getCountryCode(String country) {
		return countryCodes.get(country);
	}

	public String resolvePathToPage(String pageName) {
		return get("resources.path") + "/" + pageName;
	}

}
