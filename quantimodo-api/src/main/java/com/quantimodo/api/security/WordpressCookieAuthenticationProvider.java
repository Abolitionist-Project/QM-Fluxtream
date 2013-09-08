package com.quantimodo.api.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class WordpressCookieAuthenticationProvider implements AuthenticationProvider {
	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
		return supports(authentication.getClass()) ? authentication : null;
	}

	@Override
	public boolean supports(final Class<?> authentication) {
		return WordpressCookieAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
