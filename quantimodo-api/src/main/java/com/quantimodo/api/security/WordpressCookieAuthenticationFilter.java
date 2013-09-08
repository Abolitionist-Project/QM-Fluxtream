package com.quantimodo.api.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

public class WordpressCookieAuthenticationFilter extends GenericFilterBean implements ApplicationEventPublisherAware {
	private static final Logger logger = LoggerFactory.getLogger(WordpressCookieAuthenticationFilter.class);
	
	private final NamedParameterJdbcTemplate jdbcTemplate;
	private ApplicationEventPublisher eventPublisher;
	private AuthenticationManager authenticationManager;
	
	@Autowired
	public WordpressCookieAuthenticationFilter(final DataSource dataSource) {
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}
	
	@Override
	public void afterPropertiesSet() throws ServletException {
		Assert.notNull(authenticationManager, "authenticationManager must be specified");
	}
	
	public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
	@Override
	public void setApplicationEventPublisher(final ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			loginIfNeeded((HttpServletRequest) request, (HttpServletResponse) response);
		}
		chain.doFilter(request, response);
	}
	
	private void loginIfNeeded(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		final SecurityContext securityContext = SecurityContextHolder.getContext();
		if ((securityContext == null) || (securityContext.getAuthentication() != null)) return;
		
		final boolean userIsAdmin;
		final long userID; {
			final DefaultHttpClient httpClient = new DefaultHttpClient();
			try {
				final Cookie[] cookies = request.getCookies();
				if (cookies == null) return;
				
				final String domain = request.getHeader("Host");
				final CookieStore cookieStore = httpClient.getCookieStore();
				for (final Cookie c : cookies) {
					BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
					cookie.setDomain(domain);
					cookieStore.addCookie(cookie);
				}
				
				final String serverName = request.getServerName();
				final boolean isProduction = serverName.equals("quantimodo.com");
				final String wordpressURL = serverName.equals("quantimodo.com")
					? "https://quantimodo.com:443/user_id/"
					: String.format("%s://%s:%d/user_id/", request.getScheme(), serverName, request.getServerPort());
				logger.debug("Connecting to WordPress server at " + wordpressURL);
				String userString = httpClient.execute(new HttpGet(wordpressURL), new BasicResponseHandler()).trim();
				userIsAdmin = userString.startsWith("admin");
				if (userIsAdmin) userString = userString.substring(5);
				userID = Long.valueOf(userString);
			} catch (final NumberFormatException e) {
				return;
			} catch (final Throwable t) {
				logger.error("Unexpected error when communicating with WordPress server", t);
				// Do not reveal internal security workings to users!
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			} finally {
				httpClient.getConnectionManager().shutdown();
			}
		}
		
		GrantedAuthority userRole = new GrantedAuthorityImpl("ROLE_USER");
		GrantedAuthority adminRole = new GrantedAuthorityImpl("ROLE_ADMIN");
		try {
			// Do not use any workable password here in case someone screws up and the server starts accepting passwords
			final UserDetails user = new User(Long.toString(userID), "\0 unusable password", true, true, true, true,
				                 userIsAdmin ? Arrays.<GrantedAuthority>asList(adminRole, userRole) : Collections.<GrantedAuthority>singletonList(userRole));
			securityContext.setAuthentication(authenticationManager.authenticate(new WordpressCookieAuthenticationToken(user, user.getAuthorities())));
			
		} catch (final AuthenticationException e) {
			logger.error(String.format("Failed to log in with user ID %d", userID));
			return;
		}
		
		jdbcTemplate.update("INSERT IGNORE INTO `Guest` (id) VALUES (:userID)", Collections.<String, Object>singletonMap("userID", userID));
		
		if (eventPublisher != null) {
			eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(securityContext.getAuthentication(), this.getClass()));
		}
	}
}
