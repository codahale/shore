package com.codahale.shore.dao.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.dao.AbstractDAO;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;

@RunWith(Enclosed.class)
public class AbstractDAOTest {
	private static class MockDAO extends AbstractDAO {
		@Inject
		public MockDAO(Provider<Session> provider) {
			super(provider);
		}
		
		public Session getSession() {
			return session();
		}
	}
	
	public static class A_Descendant_Of_AbstractDAO {
		private MockDAO dao;
		private Session session;
		
		@Before
		public void setup() throws Exception {
			this.session = mock(Session.class);
			
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
	}
}
