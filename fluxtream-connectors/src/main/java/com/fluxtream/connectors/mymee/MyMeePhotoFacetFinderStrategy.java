package com.fluxtream.connectors.mymee;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;

import com.fluxtream.TimeInterval;
import com.fluxtream.connectors.Connector;
import com.fluxtream.connectors.ObjectType;
import com.fluxtream.domain.AbstractFacet;
import com.fluxtream.domain.PhotoFacetFinderStrategy;
import com.fluxtream.utils.JPAUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@Component
public class MyMeePhotoFacetFinderStrategy implements PhotoFacetFinderStrategy {

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<AbstractFacet> findAll(final long guestId, final Connector connector, final ObjectType objectType,
			final TimeInterval timeInterval) {
		return (List<AbstractFacet>) JPAUtils.find(em, getFacetClass(connector, objectType), "mymee.photo.between",
				guestId, timeInterval.start, timeInterval.end);
	}

	@Override
	public List<AbstractFacet> findBefore(final long guestId, final Connector connector, final ObjectType objectType,
			final long timeInMillis, final int desiredCount) {
		final Class<? extends AbstractFacet> facetClass = getFacetClass(connector, objectType);
		final Entity entity = facetClass.getAnnotation(Entity.class);
		final Query query = em.createQuery("SELECT facet FROM " + entity.name()
				+ " facet WHERE facet.imageURL IS NOT NULL AND facet.guestId = " + guestId + " AND facet.start <= "
				+ timeInMillis + " ORDER BY facet.start DESC LIMIT " + desiredCount);
		query.setMaxResults(desiredCount);
		return (List<AbstractFacet>) query.getResultList();
	}

	@Override
	public List<AbstractFacet> findAfter(final long guestId, final Connector connector, final ObjectType objectType,
			final long timeInMillis, final int desiredCount) {
		final Class<? extends AbstractFacet> facetClass = getFacetClass(connector, objectType);
		final Entity entity = facetClass.getAnnotation(Entity.class);
		final Query query = em.createQuery("SELECT facet FROM " + entity.name()
				+ " facet WHERE facet.imageURL IS NOT NULL AND facet.guestId = " + guestId + " AND facet.start >= "
				+ timeInMillis + " ORDER BY facet.start ASC LIMIT " + desiredCount);
		query.setMaxResults(desiredCount);
		return (List<AbstractFacet>) query.getResultList();
	}

	@Override
	public AbstractFacet findOldest(final long guestId, final Connector connector, final ObjectType objectType) {
		return getOldestOrLatestFacet(em, guestId, connector, objectType, "asc");
	}

	@Override
	public AbstractFacet findLatest(final long guestId, final Connector connector, final ObjectType objectType) {
		return getOldestOrLatestFacet(em, guestId, connector, objectType, "desc");
	}

	private AbstractFacet getOldestOrLatestFacet(final EntityManager em, final long guestId, final Connector connector,
			final ObjectType objectType, final String sortOrder) {
		final Class<? extends AbstractFacet> facetClass = getFacetClass(connector, objectType);
		final Entity entity = facetClass.getAnnotation(Entity.class);
		final Query query = em.createQuery("SELECT facet FROM " + entity.name()
				+ " facet WHERE facet.imageURL IS NOT NULL AND facet.guestId = " + guestId + " ORDER BY facet.end "
				+ sortOrder + " LIMIT 1");
		query.setMaxResults(1);
		final List resultList = query.getResultList();
		if (resultList != null && resultList.size() > 0) {
			return (AbstractFacet) resultList.get(0);
		}
		return null;
	}

	private Class<? extends AbstractFacet> getFacetClass(final Connector connector, final ObjectType objectType) {
		return objectType != null ? objectType.facetClass() : connector.facetClass();
	}
}
