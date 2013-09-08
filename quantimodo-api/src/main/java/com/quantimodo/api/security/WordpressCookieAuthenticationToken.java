package com.quantimodo.api.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class WordpressCookieAuthenticationToken extends AbstractAuthenticationToken {
	private static final long serialVersionUID = -671990544681819863L;
	private final Object principal;

	public WordpressCookieAuthenticationToken(final Object principal, final Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = principal;
	}

	@Override
	public Object getCredentials() {
		return "";
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}
}
