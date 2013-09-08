package com.fluxtream.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

@Entity(name = "Guest")
@NamedQueries({
		@NamedQuery(name = "guests.count", query = "SELECT COUNT(guest) FROM Guest"),
		@NamedQuery(name = "guest.byEmail", query = "SELECT guest FROM Guest guest WHERE guest.email=?"),
		@NamedQuery(name = "guest.byUsername", query = "SELECT guest FROM Guest guest WHERE guest.username=?"),
		@NamedQuery(name = "guests.all", query = "SELECT guest FROM Guest guest"),
		@NamedQuery(name = "guest.byOauthCredentinals", query = "SELECT guest FROM Guest guest WHERE "
				+ "guest.oauthUserId=? and guest.oauthProviderName=?") })
public class Guest extends AbstractEntity {

	public static final String ROLE_USER = "ROLE_USER";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_COACH = "ROLE_COACH";
	public static final String ROLE_ROOT = "ROLE_ROOT";

	@Index(name = "username_index")
	public String username;
	public String firstname, lastname, password;
	@Index(name = "email_index")
	public String email;
	public String salt;
	@Index(name = "oauth_user_id_index")
	@Column(name = "oauth_user_id")
	public String oauthUserId;
	@Index(name = "oauth_provider_name_index")
	@Column(name = "oauth_provider_name")
	public String oauthProviderName;

	transient List<String> userRoles;
	public String roles = ROLE_USER;

	public Guest() {
	}

	public boolean hasRole(String role) {
		return getUserRoles().contains(role);
	}

	public List<String> getUserRoles() {
		if (userRoles == null) {
			userRoles = new ArrayList<String>();
			final String[] splits = StringUtils.split(roles, ",");
			for (int i = 0; i < splits.length; i++)
				userRoles.add("" + splits[i].trim());
		}
		return userRoles;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Guest))
			return false;
		return ((Guest) o).getId() == getId();
	}

	public String getGuestName() {
		if (!StringUtils.isEmpty(firstname)) {
			if (!StringUtils.isEmpty(lastname))
				return firstname + " " + lastname;
			else
				return firstname;
		} else
			return username;
	}

}
