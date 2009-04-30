package com.codahale.shore.dao;

import org.hibernate.Session;

import com.google.inject.Provider;

/**
 * An abstract base class for Guice-injected Hibernate DAO classes.
 * 
 * @author coda
 *
 */
public class AbstractDAO {
	private final Provider<Session> provider;
	
	/**
	 * Creates a new DAO with a given session provider.
	 * 
	 * @param provider a session provider
	 */
	public AbstractDAO(Provider<Session> provider) {
		this.provider = provider;
	}
	
	/**
	 * Returns the current {@link Session}.
	 * 
	 * @return the current session
	 */
	protected Session currentSession() {
		return provider.get();
	}
}
