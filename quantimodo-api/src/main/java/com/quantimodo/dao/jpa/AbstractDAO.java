package com.quantimodo.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class AbstractDAO {

	@PersistenceContext(unitName = "Quantimodo")
	protected EntityManager em;
}
