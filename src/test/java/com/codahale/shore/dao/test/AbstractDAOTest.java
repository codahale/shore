package com.codahale.shore.dao.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.dao.AbstractDAO;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;

@RunWith(Enclosed.class)
public class AbstractDAOTest {
	private static class MockDAO extends AbstractDAO<String> {
		@Inject
		public MockDAO(Provider<Session> provider) {
			super(provider, String.class);
		}
		
		public Session getSession() {
			return currentSession();
		}
		
		public Criteria getCriteria() {
			return criteria();
		}
		
		public String getUniqueResultFromCriteria() {
			return uniqueResult(criteria());
		}
		
		public Query getNamedQuery(String name) {
			return namedQuery(name);
		}

		public String getUniqueResultFromQuery(String name) {
			return uniqueResult(namedQuery(name));
		}

		public List<String> getListFromCriteria() {
			return list(criteria());
		}

		public List<String> getListFromQuery(String name) {
			return list(namedQuery(name));
		}

		public String getInstance(Integer i) {
			return get(i);
		}

		public String save(String entity) {
			return persist(entity);
		}
	}
	
	public static class A_Descendant_Of_AbstractDAO {
		private MockDAO dao;
		private Criteria critera;
		private Query query;
		private Session session;
		
		@Before
		public void setup() throws Exception {
			this.query = mock(Query.class);
			this.critera = mock(Criteria.class);
			this.session = mock(Session.class);
			when(session.createCriteria(String.class)).thenReturn(critera);
			when(session.getNamedQuery(anyString())).thenReturn(query);
			
			this.dao = Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(Session.class).toInstance(session);
				}
			}).getInstance(MockDAO.class);
		}
		
		@Test
		public void itGetsASessionFromTheInjectedProvider() throws Exception {
			assertThat(dao.getSession(), is(sameInstance(session)));
		}
		
		@Test
		public void itHasAnEntityClass() throws Exception {
			assertThat(dao.getEntityClass(), is(sameInstance(String.class)));
		}
		
		@Test
		public void itGetsNamedQueries() throws Exception {
			assertThat(dao.getNamedQuery("query-name"), is(query));
			
			verify(session).getNamedQuery("query-name");
		}
		
		@Test
		public void itCreatesNewCriteraQueries() throws Exception {
			assertThat(dao.getCriteria(), is(critera));
			
			verify(session).createCriteria(String.class);
		}
		
		@Test
		public void itReturnsUniqueResultsFromCriteriaQueries() throws Exception {
			when(critera.uniqueResult()).thenReturn("woo");
			
			assertThat(dao.getUniqueResultFromCriteria(), is("woo"));
			verify(session).createCriteria(String.class);
			verify(critera).uniqueResult();
		}
		
		@Test
		public void itReturnsUniqueResultsFromQueries() throws Exception {
			when(query.uniqueResult()).thenReturn("woo");
			
			assertThat(dao.getUniqueResultFromQuery("query-name"), is("woo"));
			verify(session).getNamedQuery("query-name");
			verify(query).uniqueResult();
		}
		
		@Test
		public void itReturnsUniqueListsFromCriteriaQueries() throws Exception {
			when(critera.list()).thenReturn(ImmutableList.of("woo"));
			
			assertThat(dao.getListFromCriteria(), is((List<String>) ImmutableList.of("woo")));
			verify(session).createCriteria(String.class);
			verify(critera).list();
		}
		

		@Test
		public void itReturnsUniqueListsFromQueries() throws Exception {
			when(query.list()).thenReturn(ImmutableList.of("woo"));
			
			assertThat(dao.getListFromQuery("query-name"), is((List<String>) ImmutableList.of("woo")));
			verify(session).getNamedQuery("query-name");
			verify(query).list();
		}
		
		@Test
		public void itGetsEntitiesById() throws Exception {
			when(session.get(String.class, 200)).thenReturn("woo!");
			
			assertThat(dao.getInstance(200), is("woo!"));
			verify(session).get(String.class, 200);
		}
		
		@Test
		public void itPersistsEntities() throws Exception {
			assertThat(dao.save("woo"), is("woo"));
			
			verify(session).saveOrUpdate("woo");
		}
	}
}
