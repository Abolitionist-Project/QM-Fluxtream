package com.quantimodo.dao.jpa;

import com.quantimodo.data.VariableCategory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class VariableCategoryDAO extends AbstractDAO {

	public List<VariableCategory> get() {
		Query query = em.createQuery("FROM VariableCategory");
		return query.getResultList();
	}

	public void put(ArrayList<VariableCategory> variableCategories) {
		//TODO: check permission
		//if (!database.currentUserIsAdmin()) throw new AuthorizationException("Only administrative users can add unit categories.");
		for (VariableCategory each : variableCategories) {
			em.persist(toEntity(each));
		}
	}

	private com.quantimodo.entities.VariableCategory toEntity(VariableCategory variableCategory) {
		return new com.quantimodo.entities.VariableCategory(variableCategory.getName());
	}
}
