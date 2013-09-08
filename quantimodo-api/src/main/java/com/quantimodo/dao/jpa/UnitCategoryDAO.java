package com.quantimodo.dao.jpa;

import com.quantimodo.data.UnitCategory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class UnitCategoryDAO extends AbstractDAO {

	public List<UnitCategory> get() {
		Query query = em.createQuery("FROM UnitCategory");
		return query.getResultList();
	}

	public void put(ArrayList<UnitCategory> unitCategories) {
		//TODO: check permission
		//if (!database.currentUserIsAdmin()) throw new AuthorizationException("Only administrative users can add unit categories.");
		for (UnitCategory each : unitCategories) {
			em.persist(toEntity(each));
		}
	}

	private com.quantimodo.entities.UnitCategory toEntity(UnitCategory unitCategory) {
		return new com.quantimodo.entities.UnitCategory(unitCategory.getName());
	}
}
