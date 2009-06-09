package com.codahale.shore.dao;

import static com.google.common.base.Preconditions.*;

import java.io.Serializable;
import java.util.List;

import net.jcip.annotations.Immutable;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.google.inject.Provider;

/**
 * An abstract base class for Guice-injected Hibernate DAO classes.
 * 
 * @author coda
 * 
 * @param <E>
 *            the class which this DAO manages
 */
@Immutable
public class AbstractDAO<E> {
	private final Provider<Session> provider;
	private final Class<E> entityClass;

	/**
	 * Creates a new DAO with a given session provider.
	 * 
	 * @param provider
	 *            a session provider
	 * @param entityClass
	 *            the entity class this DAO manages
	 */
	public AbstractDAO(Provider<Session> provider, Class<E> entityClass) {
		this.provider = checkNotNull(provider);
		this.entityClass = checkNotNull(entityClass);
	}

	/**
	 * Returns the current {@link Session}.
	 * 
	 * @return the current session
	 */
	protected Session currentSession() {
		return provider.get();
	}

	/**
	 * Creates a new {@link Criteria} query for {@code <E>}.
	 * 
	 * @return a new {@link Criteria} query
	 * @see Session#createCriteria(Class)
	 */
	protected Criteria criteria() {
		return currentSession().createCriteria(entityClass);
	}

	/**
	 * Returns a named {@link Query}.
	 * 
	 * @param queryName
	 *            the name of the query
	 * @return the named query
	 * @see Session#getNamedQuery(String)
	 */
	protected Query namedQuery(String queryName) throws HibernateException {
		return currentSession().getNamedQuery(checkNotNull(queryName));
	}

	/**
	 * Returns the entity class managed by this DAO.
	 * 
	 * @return this DAO's entity class
	 */
	public Class<E> getEntityClass() {
		return entityClass;
	}

	/**
	 * Convenience method to return a single instance that matches the criteria, or null if the
	 * critera returns no results.
	 * 
	 * @param criteria
	 *            the {@link Criteria} query to run
	 * @return the single result or {@code null}
	 * @throws HibernateException
	 *             if there is more than one matching result
	 * @see Criteria#uniqueResult()
	 */
	@SuppressWarnings("unchecked")
	protected E uniqueResult(Criteria criteria) throws HibernateException {
		return (E) checkNotNull(criteria).uniqueResult();
	}

	/**
	 * Convenience method to return a single instance that matches the query, or null if the query
	 * returns no results.
	 * 
	 * @param query
	 *            the query to run
	 * @return the single result or {@code null}
	 * @throws HibernateException
	 *             if there is more than one matching result
	 * @see Query#uniqueResult()
	 */
	@SuppressWarnings("unchecked")
	protected E uniqueResult(Query query) throws HibernateException {
		return (E) checkNotNull(query).uniqueResult();
	}

	/**
	 * Get the results of a {@link Criteria} query.
	 * 
	 * @param criteria
	 *            the {@link Criteria} query to run
	 * @return the list of matched query results
	 * @see Criteria#list()
	 */
	@SuppressWarnings("unchecked")
	protected List<E> list(Criteria criteria) throws HibernateException {
		return checkNotNull(criteria).list();
	}

	/**
	 * Get the results of aquery.
	 * 
	 * @param query
	 *            the query to run
	 * @return the list of matched query results
	 * @see Query#list()
	 */
	@SuppressWarnings("unchecked")
	protected List<E> list(Query query) throws HibernateException {
		return checkNotNull(query).list();
	}

	/**
	 * Return the persistent instance of {@code <E>} with the given identifier, or {@code null} if
	 * there is no such persistent instance. (If the instance, or a proxy for the instance, is
	 * already associated with the session, return that instance or proxy.)
	 * 
	 * @param id
	 *            an identifier
	 * @return a persistent instance or {@code null}
	 * @throws HibernateException
	 * @see Session#get(Class, Serializable)
	 */
	@SuppressWarnings("unchecked")
	protected E get(Serializable id) {
		return (E) currentSession().get(entityClass, checkNotNull(id));
	}

	/**
	 * Either save or update the given instance, depending upon resolution of the unsaved-value
	 * checks (see the manual for discussion of unsaved-value checking).
	 * <p/>
	 * This operation cascades to associated instances if the association is mapped with
	 * <tt>cascade="save-update"</tt>.
	 * 
	 * @param entity
	 *            a transient or detached instance containing new or updated state
	 * @throws HibernateException
	 * @see Session#saveOrUpdate(Object)
	 */
	protected E persist(E entity) throws HibernateException {
		currentSession().saveOrUpdate(checkNotNull(entity));
		return entity;
	}
}
