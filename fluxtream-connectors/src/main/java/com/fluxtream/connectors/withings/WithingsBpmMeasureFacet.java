package com.fluxtream.connectors.withings;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.search.annotations.Indexed;

import com.fluxtream.connectors.annotations.ObjectTypeSpec;
import com.fluxtream.domain.AbstractFacet;

@Entity(name = "Facet_WithingsBpmMeasure")
@ObjectTypeSpec(name = "blood_pressure", value = 2, extractor = WithingsFacetExtractor.class, prettyname = "Blood Pressure Measures")
@NamedQueries({
		@NamedQuery(name = "withings.blood_pressure.deleteAll", query = "DELETE FROM Facet_WithingsBpmMeasure facet WHERE facet.guestId=?"),
		@NamedQuery(name = "withings.blood_pressure.between", query = "SELECT facet FROM Facet_WithingsBpmMeasure facet WHERE facet.guestId=? AND facet.start>=? AND facet.end<=?") })
@Indexed
public class WithingsBpmMeasureFacet extends AbstractFacet {

	public long measureTime;

	public float systolic;
	public float diastolic;
	public float heartPulse;

	@Override
	protected void makeFullTextIndexable() {
	}

}
