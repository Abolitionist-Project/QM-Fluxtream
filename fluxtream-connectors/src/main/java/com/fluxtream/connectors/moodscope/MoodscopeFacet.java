package com.fluxtream.connectors.moodscope;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import com.fluxtream.connectors.annotations.ObjectTypeSpec;
import com.fluxtream.domain.AbstractFacet;

/**
 * @author alucab
 * 
 */

@Entity(name = "Facet_Moodscope")
@ObjectTypeSpec(name = "moodscope", value = 1, isImageType = false, prettyname = "Moodscope")
@NamedQueries({
		@NamedQuery(name = "moodscope.moodscope.all", query = "SELECT facet FROM Facet_Moodscope facet WHERE facet.guestId=? ORDER BY facet.start DESC"),
		@NamedQuery(name = "moodscope.moodscope.deleteAll", query = "DELETE FROM Facet_Moodscope facet WHERE facet.guestId=?"),
		@NamedQuery(name = "moodscope.moodscope.between", query = "SELECT facet FROM Facet_Moodscope facet WHERE facet.guestId=? AND facet.start>=? AND facet.end<=?") })
@Indexed
public class MoodscopeFacet extends AbstractFacet {

	@Index(name = "moodscope_id")
	public long moodscope_id;
	public int type;
	public int score;

	@Override
	protected void makeFullTextIndexable() {
	}

	MoodscopeFacet() {
		this.type = 7;
	}

}
