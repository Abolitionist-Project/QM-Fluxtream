package com.fluxtream.connectors.google_latitude;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Index;

import com.fluxtream.connectors.annotations.ObjectTypeSpec;
import com.fluxtream.domain.AbstractFacet;
import com.google.api.client.googleapis.json.JsonCContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

@Entity(name="Facet_GoogleLatitudeLocation")
@ObjectTypeSpec(name = "location", value = 1, prettyname = "Location")
@NamedQueries({
		@NamedQuery(name = "google_latitude.location.lastSeen", query = "SELECT facet FROM " +
				"Facet_GoogleLatitudeLocation facet WHERE " +
				"facet.guestId=? AND facet.timestampMs<? " +
				"ORDER BY facet.timestampMs DESC"),
		@NamedQuery(name = "google_latitude.location.nextSeen", query = "SELECT facet FROM " +
				"Facet_GoogleLatitudeLocation facet WHERE " +
				"facet.guestId=? AND facet.timestampMs>? " +
				"ORDER BY facet.timestampMs ASC"),
		@NamedQuery(name = "openpath.location.deleteAll", query = "DELETE FROM " +
				"Facet_GoogleLatitudeLocation facet WHERE " +
				"facet.source=5 AND facet.guestId=?"),
		@NamedQuery(name = "google_latitude.location.deleteAll", query = "DELETE FROM " +
				"Facet_GoogleLatitudeLocation facet WHERE " +
				"facet.source=2 AND facet.guestId=?"),
		@NamedQuery(name = "google_latitude.location.between", query = "SELECT facet FROM " +
				"Facet_GoogleLatitudeLocation facet WHERE " +
				"facet.guestId=? AND facet.timestampMs>=? AND " +
				"facet.timestampMs<=? ORDER BY facet.timestampMs"),
        @NamedQuery(name = "google_latitude.location.newest", query = "SELECT facet FROM Facet_GoogleLatitudeLocation facet WHERE facet.guestId=? ORDER BY facet.timestampMs DESC LIMIT 1")
})
public class LocationFacet extends AbstractFacet implements Comparable<LocationFacet>, Serializable {

	private static final long serialVersionUID = 2882496521084143121L;

	public static enum Source {
		OTHER, USER, GOOGLE_LATITUDE, GEO_IP_DB, IP_TO_LOCATION, OPEN_PATH, RUNKEEPER
	}
	
	public Source source = Source.GOOGLE_LATITUDE;
	
	@Key
	@Index(name="timestamp_index")
	public Long timestampMs;

	@Key
	@Index(name="latitude_index")
	public Float latitude;

	@Key
	@Index(name="longitude_index")
	public Float longitude;

	@Key
	public Integer accuracy;

	@Key
	public Integer speed;

	@Key
	public Integer heading;

	@Key
	public Integer altitude;

	@Key
	public Integer altitudeAccuracy;

	@Key
	public Integer placeid;

	public String device;

	public String version;

	public String os;

    /**
     * serves as a backreference to the resource that originated in this coordinate,
     * e.g. a runkeeper run or bike ride
     */
	// @Override
	// @Index(name = "uri")
	// public String uri;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LocationFacet))
			return false;
		LocationFacet lr = (LocationFacet) o;
		return lr.timestampMs.equals(timestampMs) && lr.latitude.equals(this.latitude)
				&& lr.longitude.equals(this.longitude);
	}

	JsonCContent toContent() {
		JsonCContent content = new JsonCContent(new JacksonFactory(), this);
		return content;
	}

	@Override
	public int compareTo(LocationFacet o1) {
		return (o1.timestampMs > timestampMs)?-1:1;
	}

	@Override
	protected void makeFullTextIndexable() { }
}
