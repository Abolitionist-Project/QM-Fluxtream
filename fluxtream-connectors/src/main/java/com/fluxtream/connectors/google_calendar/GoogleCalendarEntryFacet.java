package com.fluxtream.connectors.google_calendar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import org.hibernate.search.annotations.Indexed;

import com.fluxtream.connectors.annotations.ObjectTypeSpec;
import com.fluxtream.domain.AbstractFacet;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

@SuppressWarnings("serial")
@Entity(name = "Facet_CalendarEventEntry")
@ObjectTypeSpec(name = "entry", value = 1, parallel = true, prettyname = "Entry")
@NamedQueries({
		@NamedQuery(name = "google_calendar.entry.deleteAll", query = "DELETE FROM Facet_CalendarEventEntry facet WHERE facet.guestId=?"),
		@NamedQuery(name = "google_calendar.entry.between", query = "SELECT facet FROM Facet_CalendarEventEntry facet WHERE facet.guestId=? AND facet.start>=? AND facet.end<=?"),
		@NamedQuery(name = "google_calendar.entry.newest", query = "SELECT facet FROM Facet_CalendarEventEntry facet WHERE facet.guestId=? ORDER BY facet.end DESC LIMIT 1") })
@Indexed
public class GoogleCalendarEntryFacet extends AbstractFacet implements Serializable {

	public String icalUID;
	public String entryId;
	public String kind;

	@Lob
	public String linkHref;
	@Lob
	public String summary;

	@Lob
	@Column(length = 100000)
	public String description;
	public long updated;

	@Lob
	public String title;

	@Lob
	public String location;

	public transient List<SEventWho> participants;
	public transient List<SWhen> times;

	@Lob
	byte[] whenStorage;
	@Lob
	byte[] participantsStorage;

	public GoogleCalendarEntryFacet() {
	}

	@SuppressWarnings("unchecked")
	@PostLoad
	void deserialize() {
		try {
			if (whenStorage != null) {
				ObjectInputStream objectinput = new ObjectInputStream(new ByteArrayInputStream(whenStorage));
				times = (List<SWhen>) objectinput.readObject();
			}
			if (participantsStorage != null) {
				ObjectInputStream objectinput = new ObjectInputStream(new ByteArrayInputStream(participantsStorage));
				participants = (List<SEventWho>) objectinput.readObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@PrePersist
	void serialize() {
		try {
			if (times != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream objectoutput = new ObjectOutputStream(baos);
				objectoutput.writeObject(times);
				whenStorage = baos.toByteArray();
			}
			if (participants != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream objectoutput = new ObjectOutputStream(baos);
				objectoutput.writeObject(participants);
				participantsStorage = baos.toByteArray();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GoogleCalendarEntryFacet(Event event) {
		DateTime updated = event.getUpdated();
		if (updated != null) {
			this.updated = updated.getValue();
		}

		this.linkHref = event.getHtmlLink();
		this.icalUID = event.getICalUID();
		this.entryId = event.getId();
		this.kind = event.getKind();

		this.location = event.getLocation();

		if (event.getAttendees() != null) {
			this.participants = new ArrayList<SEventWho>();
			for (EventAttendee attendee : event.getAttendees()) {
				SEventWho seventWho = new SEventWho();
				seventWho.attendeeEmail = attendee.getEmail();
				seventWho.displayName = attendee.getDisplayName();
				seventWho.attendeeStatus = attendee.getResponseStatus();
				this.participants.add(seventWho);
			}
		}
		this.description = event.getDescription();

		EventDateTime start = event.getStart();
		EventDateTime end = event.getEnd();

		this.times = new ArrayList<SWhen>();
		SWhen swhen = new SWhen();
		swhen.startDateTime = (start.getDateTime() == null ? null : start.getDateTime().getValue());
		swhen.endDateTime = (end.getDateTime() == null ? null : end.getDateTime().getValue());
		swhen.startDate = (start.getDate() == null ? null : start.getDate().getValue());
		swhen.endDate = (end.getDate() == null ? null : end.getDate().getValue());
		this.times.add(swhen);

		this.title = event.getSummary();
	}

	@Override
	protected void makeFullTextIndexable() {
		this.fullTextDescription = "";
		if (description != null)
			fullTextDescription += " " + description;
		if (title != null)
			fullTextDescription += " " + title;
	}

}
