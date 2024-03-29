package com.fluxtream.connectors.lastfm;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fluxtream.ApiData;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.facets.extractors.AbstractFacetExtractor;
import com.fluxtream.services.JPADaoService;

@Component
public class LastFmFacetExtractor extends AbstractFacetExtractor {

	@Autowired
	JPADaoService jpaDaoService;

	public List<AbstractFacet> extractFacets(ApiData apiData, ObjectType objectType) {
		List<AbstractFacet> facets = new ArrayList<AbstractFacet>();

		try {
			if (objectType == ObjectType.getObjectType(connector(), "recent_track")) {
				extractLastfmRecentTracks(apiData, facets);
			} else {
				extractLastfmLovedTracks(apiData, facets);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return facets;
	}

	private void extractLastfmRecentTracks(ApiData apiData, List<AbstractFacet> facets) throws Throwable {
		JSONObject tracksToExtract = apiData.jsonObject.getJSONObject("recenttracks");
		JSONArray tracks = new JSONArray();
		if (tracksToExtract.containsKey("track") && tracksToExtract.get("track") instanceof JSONArray)
			tracks = tracksToExtract.getJSONArray("track");
		else if (tracksToExtract.containsKey("track") && tracksToExtract.get("track") instanceof JSONObject) {
			JSONObject aTrack = tracksToExtract.getJSONObject("track");
			tracks.add(aTrack);
		} else
			return;

		int ntracks = tracks.size();

		for (int i = 0; i < ntracks; i++) {
			LastFmRecentTrackFacet facet = new LastFmRecentTrackFacet();

			super.extractCommonFacetData(facet, apiData);

			JSONObject it = tracks.getJSONObject(i);
			if (!it.containsKey("artist"))
				continue;
			if (!it.getJSONObject("artist").containsKey("#text")) {
				JSONObject artist = it.getJSONObject("artist");
				facet.artist = artist.getString("name");
				facet.artist_mbid = artist.getString("mbid");
			} else {
				facet.artist = it.getJSONObject("artist").getString("#text");
			}
			if (!it.containsKey("name"))
				continue;
			String name = it.getString("name");

			facet.name = name;

			JSONObject dateObject = it.getJSONObject("date");
			if (!dateObject.containsKey("uts"))
				continue;

			long uts = dateObject.getLong("uts");
			long date = (Long.valueOf(uts)) * 1000;
			facet.time = date;
			facet.start = date;
			facet.end = date;

			if (it.containsKey("image")) {
				JSONArray images = it.getJSONArray("image");
				if (images != null) {
					StringBuffer bf = new StringBuffer();
					for (int j = 0; j < images.size(); j++) {
						if (bf.length() != 0)
							bf.append(",");
						JSONObject imageObject = images.getJSONObject(j);
						String size = imageObject.getString("size");
						if (size.trim().equalsIgnoreCase("small"))
							bf.append(imageObject.getString("#text"));
					}
					facet.imgUrls = bf.toString();
				}
			}

			LastFmRecentTrackFacet duplicate = jpaDaoService.findOne("lastfm.recent_track.byStartEnd",
					LastFmRecentTrackFacet.class, apiData.updateInfo.getGuestId(), date, date);
			if (duplicate == null)
				facets.add(facet);
		}
	}

	private void extractLastfmLovedTracks(ApiData apiData, List<AbstractFacet> facets) throws Throwable {
		JSONObject tracksToExtract = apiData.jsonObject.getJSONObject("lovedtracks");
		JSONArray tracks = new JSONArray();
		if (tracksToExtract.containsKey("track") && tracksToExtract.get("track") instanceof JSONArray)
			tracks = tracksToExtract.getJSONArray("track");
		else if (tracksToExtract.containsKey("track") && tracksToExtract.get("track") instanceof JSONObject) {
			JSONObject aTrack = tracksToExtract.getJSONObject("track");
			tracks.add(aTrack);
		} else
			return;
		int ntracks = tracks.size();

		for (int i = 0; i < ntracks; i++) {
			LastFmLovedTrackFacet facet = new LastFmLovedTrackFacet();

			super.extractCommonFacetData(facet, apiData);

			JSONObject it = tracks.getJSONObject(i);
			if (!it.containsKey("artist"))
				continue;
			if (!it.getJSONObject("artist").containsKey("#text")) {
				JSONObject artist = it.getJSONObject("artist");
				facet.artist = artist.getString("name");
				facet.artist_mbid = artist.getString("mbid");
			} else {
				facet.artist = it.getJSONObject("artist").getString("#text");
			}
			if (!it.containsKey("name"))
				continue;
			String name = it.getString("name");

			facet.name = name;

			JSONObject dateObject = it.getJSONObject("date");
			if (!dateObject.containsKey("uts"))
				continue;

			long uts = dateObject.getLong("uts");
			long date = (Long.valueOf(uts)) * 1000;
			facet.time = date;
			facet.start = date;
			facet.end = date;

			if (it.containsKey("image")) {
				JSONArray images = it.getJSONArray("image");
				if (images != null) {
					StringBuffer bf = new StringBuffer();
					for (int j = 0; j < images.size(); j++) {
						if (bf.length() != 0)
							bf.append(",");
						JSONObject imageObject = images.getJSONObject(j);
						String size = imageObject.getString("size");
						if (size.trim().equalsIgnoreCase("small"))
							bf.append(imageObject.getString("#text"));
					}
					facet.imgUrls = bf.toString();
				}
			}

			LastFmLovedTrackFacet duplicate = jpaDaoService.findOne("lastfm.loved_track.byStartEnd",
					LastFmLovedTrackFacet.class, apiData.updateInfo.getGuestId(), date, date);
			if (duplicate == null)
				facets.add(facet);
		}
	}
}
