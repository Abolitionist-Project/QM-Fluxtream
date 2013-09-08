package com.fluxtream.connectors.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.fluxtream.connectors.Connector;
import com.fluxtream.domain.Guest;
import com.fluxtream.services.GuestService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public abstract class BaseGoogleOAuthController {

	public static class TokenInfo {
		public String accessToken, refreshToken;
		public long expiresInSeconds;
	}

	protected String buildAuthUrl(HttpServletRequest request, List<String> scopes) {
		NetHttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, getConsumerKey(), getConsumerSecret(), scopes).build();
		String authUrl = googleAuthorizationCodeFlow.newAuthorizationUrl().setAccessType("offline")
				.setApprovalPrompt("force").setRedirectUri(buildRedirectUrl()).build();
		return authUrl;
	}

	protected TokenInfo retrieveTokenInfo(HttpServletRequest request, List<String> scopes) throws IOException {
		String authCode = request.getParameter("code");
		NetHttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, getConsumerKey(), getConsumerSecret(), scopes).build();
		GoogleAuthorizationCodeTokenRequest newTokenRequest = googleAuthorizationCodeFlow.newTokenRequest(authCode);
		GoogleTokenResponse tokenResponse = newTokenRequest.setRedirectUri(buildRedirectUrl()).execute();
		TokenInfo tokenInfo = new TokenInfo();
		tokenInfo.accessToken = tokenResponse.getAccessToken();
		tokenInfo.refreshToken = tokenResponse.getRefreshToken();
		tokenInfo.expiresInSeconds = tokenResponse.getExpiresInSeconds();
		return tokenInfo;
	}

	public static void refreshToken(GuestService guestService, String accessToken, String refreshToken,
			String consumerKey, String consumerSecret, Connector connector, Guest guest) throws IOException {
		NetHttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		GoogleCredential credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory)
				.setTransport(httpTransport).setClientSecrets(consumerKey, consumerSecret).build();
		credential.setAccessToken(accessToken);
		credential.setRefreshToken(refreshToken);
		credential.refreshToken();
		TokenInfo resultTokenInfo = new TokenInfo();
		resultTokenInfo.accessToken = credential.getAccessToken();
		resultTokenInfo.refreshToken = credential.getRefreshToken();
		resultTokenInfo.expiresInSeconds = credential.getExpiresInSeconds();
		saveTokenInfo(guestService, resultTokenInfo, connector, guest);
	}

	public static void saveTokenInfo(GuestService guestService, TokenInfo tokenInfo, Connector connector, Guest guest) {
		guestService.setApiKeyAttribute(guest.getId(), connector, "accessToken", tokenInfo.accessToken);
		guestService.setApiKeyAttribute(guest.getId(), connector, "refreshToken", tokenInfo.refreshToken);
		guestService.setApiKeyAttribute(guest.getId(), connector, "expiresInSeconds", tokenInfo.expiresInSeconds + "");
	}

	protected abstract GuestService guestService();

	protected abstract String getConsumerKey();

	protected abstract String getConsumerSecret();

	protected abstract String buildRedirectUrl();

}
