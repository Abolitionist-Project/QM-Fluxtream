package com.fluxtream.connectors.withings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Component;

import com.fluxtream.ApiData;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.facets.extractors.AbstractFacetExtractor;

@Component
public class WithingsFacetExtractor extends AbstractFacetExtractor {

	private static final int WEIGHT = 1;
	private static final int HEIGHT = 4;
	private static final int FAT_FREE_MASS = 5;
	private static final int FAT_RATIO = 6;
	private static final int FAT_MASS_WEIGHT = 8;
	private static final int DIASTOLIC_BLOOD_PRESSURE = 9;
	private static final int SYSTOLIC_BLOOD_PRESSURE = 10;
	private static final int HEART_PULSE = 11;

	@Override
	public List<AbstractFacet> extractFacets(ApiData apiData, ObjectType objectType) {
		List<AbstractFacet> facets = new ArrayList<AbstractFacet>();
		JSONObject bodyScaleResponse = JSONObject.fromObject(apiData.json);

		if (!(bodyScaleResponse.has("status")) || bodyScaleResponse.getInt("status") != 0) {
			return facets;
		}
		JSONObject body = bodyScaleResponse.getJSONObject("body");
		JSONArray measureGrps = body.getJSONArray("measuregrps");
		if (measureGrps == null) {
			return facets;
		}

		@SuppressWarnings("rawtypes")
		Iterator iterator = measureGrps.iterator();
		while (iterator.hasNext()) {

			JSONObject measureGrp = (JSONObject) iterator.next();
			long date = measureGrp.getLong("date") * 1000;
			JSONArray measures = measureGrp.getJSONArray("measures");

			WithingsBodyScaleMeasureFacet facet = buildBodyScaleFacet(apiData, date);

			@SuppressWarnings("rawtypes")
			Iterator measuresIterator = measures.iterator();
			boolean isBpm = false;
			while (measuresIterator.hasNext()) {
				JSONObject measure = (net.sf.json.JSONObject) measuresIterator.next();
				isBpm = fillBodyScaleFacetByData(facet, isBpm, measure);
			}
			if (!isBpm) {
				if (objectType == ObjectType.getObjectType(connector(), "weight")) {
					facets.add(facet);
				}
				continue;
			}
			if (objectType == ObjectType.getObjectType(connector(), "blood_pressure")) {
				WithingsBpmMeasureFacet bpmFacet = buildBpmMeasureFacet(apiData, date, facet);
				facets.add(bpmFacet);
			}

		}

		return facets;
	}

	private boolean fillBodyScaleFacetByData(WithingsBodyScaleMeasureFacet facet, boolean isBpm, JSONObject measure) {
		double pow = Math.abs(measure.getInt("unit"));
		double measureValue = measure.getDouble("value");
		double divisor = Math.pow(10, pow);
		switch (measure.getInt("type")) {
		case WEIGHT:
			float fValue = (float) (measureValue / divisor);
			facet.weight = fValue;
			break;
		case HEIGHT:
			facet.height = (float) (measureValue / divisor);
			break;
		case FAT_FREE_MASS:
			facet.fatFreeMass = (float) (measureValue / divisor);
			break;
		case FAT_RATIO:
			facet.fatRatio = (float) (measureValue / divisor);
			break;
		case FAT_MASS_WEIGHT:
			facet.fatMassWeight = (float) (measureValue / divisor);
			break;
		case DIASTOLIC_BLOOD_PRESSURE:
			isBpm = true;
			facet.diastolic = (float) (measureValue / divisor);
			break;
		case SYSTOLIC_BLOOD_PRESSURE:
			isBpm = true;
			facet.systolic = (float) (measureValue / divisor);
			break;
		case HEART_PULSE:
			isBpm = true;
			facet.heartPulse = (float) (measureValue / divisor);
			break;
		}
		return isBpm;
	}

	private WithingsBpmMeasureFacet buildBpmMeasureFacet(ApiData apiData, long date, WithingsBodyScaleMeasureFacet facet) {
		WithingsBpmMeasureFacet bpmFacet = new WithingsBpmMeasureFacet();
		super.extractCommonFacetData(bpmFacet, apiData);
		bpmFacet.objectType = ObjectType.getObjectType(connector(), "blood_pressure").value();
		bpmFacet.measureTime = date;
		bpmFacet.start = date;
		bpmFacet.end = date;
		bpmFacet.systolic = facet.systolic;
		bpmFacet.diastolic = facet.diastolic;
		bpmFacet.heartPulse = facet.heartPulse;
		return bpmFacet;
	}

	private WithingsBodyScaleMeasureFacet buildBodyScaleFacet(ApiData apiData, long date) {
		WithingsBodyScaleMeasureFacet facet = new WithingsBodyScaleMeasureFacet();
		facet.measureTime = date;
		facet.start = date;
		facet.end = date;
		super.extractCommonFacetData(facet, apiData);
		facet.objectType = ObjectType.getObjectType(connector(), "weight").value();
		return facet;
	}

}
