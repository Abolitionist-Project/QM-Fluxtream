package com.fluxtream.connectors.withings;

import java.util.Date;

import com.fluxtream.TimeInterval;
import com.fluxtream.connectors.vos.AbstractInstantFacetVO;
import com.fluxtream.domain.GuestSettings;

public class WithingsBpmMeasureFacetVO extends AbstractInstantFacetVO<WithingsBpmMeasureFacet> {

	float systolic, diastolic, pulse;

	@Override
	public void fromFacet(WithingsBpmMeasureFacet facet, TimeInterval timeInterval, GuestSettings settings) {
		this.startMinute = toMinuteOfDay(new Date(facet.measureTime), timeInterval.timeZone);
		systolic = facet.systolic;
		diastolic = facet.diastolic;
		pulse = facet.heartPulse;
		description = facet.systolic + "/" + facet.diastolic + " mmHg";
	}

}
