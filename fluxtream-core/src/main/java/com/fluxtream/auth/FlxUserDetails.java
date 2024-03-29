package com.fluxtream.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fluxtream.domain.CoachingBuddy;
import com.fluxtream.domain.Guest;

@SuppressWarnings("serial")
public class FlxUserDetails implements UserDetails {

	private transient Guest guest;
	public transient CoachingBuddy coachee;

	public FlxUserDetails(Guest guest) {
		this.guest = guest;
	}

	public Guest getGuest() {
		return this.guest;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public String getUsername() {
		return guest != null ? guest.username : null;
	}

	public String getSalt() {
		return guest != null ? guest.salt : null;
	}

	public String getPassword() {
		return guest != null ? guest.password : null;
	}

	public Collection<GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> result = new ArrayList<GrantedAuthority>();
		List<String> userRoles = guest.getUserRoles();
		for (String userRole : userRoles)
			result.add(new SimpleGrantedAuthority(userRole));
		return result;
	}

}
