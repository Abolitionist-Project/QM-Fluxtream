package com.fluxtream.connectors.bodymedia;

import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

import com.fluxtream.domain.AbstractFacet;

/**
 * <p>
 * <code>BodymediaAbstractFacet</code> does something...
 * </p>
 * 
 * @author Prasanth Somasundar
 */
@MappedSuperclass
public abstract class BodymediaAbstractFacet extends AbstractFacet {
	// The date that this facet represents
	public String date;
	public Long lastSync;
	@Lob
	public String json;

	public String getDate() {
		return date;
	}

	@SuppressWarnings("unused")
	public Long getLastSync() {
		return lastSync;
	}

	@SuppressWarnings("unused")
	public String getJson() {
		return json;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	@SuppressWarnings("unused")
	public void setLastSync(final Long lastSync) {
		this.lastSync = lastSync;
	}

	@SuppressWarnings("unused")
	public void setJson(final String json) {
		this.json = json;
	}

	@Override
	protected void makeFullTextIndexable() {
	}
}
