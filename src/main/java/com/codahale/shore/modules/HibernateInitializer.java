package com.codahale.shore.modules;

import com.google.inject.Inject;
import com.wideplay.warp.persist.PersistenceService;

/**
 * Allows Warp-Persist to start Hibernate.
 * 
 * @author coda
 */
public class HibernateInitializer {
	@Inject
	public HibernateInitializer(PersistenceService service) {
		service.start();
	}
}
