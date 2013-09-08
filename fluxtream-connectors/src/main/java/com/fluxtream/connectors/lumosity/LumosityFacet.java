package com.fluxtream.connectors.lumosity;

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

@Entity(name = "Facet_Lumosity")
@ObjectTypeSpec(name = "lumosity", value = 1, isImageType = false, prettyname = "Lumosity")
@NamedQueries({
		@NamedQuery(name = "lumosity.lumosity.all", query = "SELECT facet FROM Facet_Lumosity facet WHERE facet.guestId=? ORDER BY facet.start DESC"),
		@NamedQuery(name = "lumosity.lumosity.deleteAll", query = "DELETE FROM Facet_Lumosity facet WHERE facet.guestId=?"),
		@NamedQuery(name = "lumosity.lumosity.between", query = "SELECT facet FROM Facet_Lumosity facet WHERE facet.guestId=? AND facet.start>=? AND facet.end<=?") })
@Indexed
public class LumosityFacet extends AbstractFacet {

	public enum Type {
		OVERALL("overall"), SPEED("speed"), MEMORY("memory"), ATTENTION("attention"), FLEXIBILITY("flexibility"), PROBLEM_SOLVING(
				"problem-solving");

		String value;

		private Type(String value) {
			this.value = value;
		}

		public static Type resolveTypeByValue(String value) {
			for (Type type : Type.values()) {
				if (type.value.equals(value)) {
					return type;
				}
			}
			return null;
		}
	}

	@Index(name = "lumosity_id")
	public long lumosity_id;
	public Type type;
	public int score;

	@Override
	protected void makeFullTextIndexable() {
	}

}
