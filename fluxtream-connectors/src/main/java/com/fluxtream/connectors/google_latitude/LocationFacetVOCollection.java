package com.fluxtream.connectors.google_latitude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fluxtream.TimeInterval;
import com.fluxtream.connectors.vos.AbstractFacetVOCollection;
import com.fluxtream.connectors.vos.StartMinuteComparator;
import com.fluxtream.domain.GuestSettings;

public class LocationFacetVOCollection extends AbstractFacetVOCollection<LocationFacet> {

	List<LocationFacetVO> positions;

	@Override
	public void extractFacets(List<LocationFacet> facets, TimeInterval timeInterval, GuestSettings settings) {
		positions = new ArrayList<LocationFacetVO>();
		for (LocationFacet locationResource : facets) {
			LocationFacetVO facet = new LocationFacetVO();
			facet.extractValues(locationResource, timeInterval, settings);
			positions.add(facet);
		}
		Collections.sort(positions, new StartMinuteComparator());
	}

}
