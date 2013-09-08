package com.fluxtream.connectors.mymee;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fluxtream.ApiData;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.facets.extractors.AbstractFacetExtractor;

/**
 * 
 * @author Candide Kemmler (candide@fluxtream.com)
 */
@Component
public class MymeeObservationFacetExtractor extends AbstractFacetExtractor {

	@Override
	public List<AbstractFacet> extractFacets(final ApiData apiData, final ObjectType objectType) throws Exception {
		return new ArrayList<AbstractFacet>();
	}

}
