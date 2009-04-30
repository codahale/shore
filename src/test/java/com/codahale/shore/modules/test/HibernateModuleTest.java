package com.codahale.shore.modules.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.cfg.AnnotationConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.modules.HibernateModule;
import com.codahale.shore.modules.test.fixtures.Cat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@RunWith(Enclosed.class)
public class HibernateModuleTest {
	private static abstract class Context {
		protected Logger logger;
		
		public void setup() throws Exception {
			Logger.getLogger("org.hibernate").setLevel(Level.OFF);
			this.logger = mock(Logger.class);
		}
		
		protected HibernateModule createModule() throws Exception {
			final Properties fixtureProperties = new Properties();
			fixtureProperties.load(ClassLoader.getSystemResource("hibernate-module-fixture.properties").openStream());
			final Package fixturePackage = Package.getPackage(Cat.class.getCanonicalName().replace(".Cat", ""));
			
			return new HibernateModule(
				logger,
				fixtureProperties,
				ImmutableList.of(fixturePackage)
			);
		}
	}
	
	public static class Creating_A_Hibernate_Configuration extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
		}
		
		@Test
		public void itLogsInformationAboutTheEntitiesItAdds() throws Exception {
			createModule();
			
			verify(logger).info("Scanning com.codahale.shore.modules.test.fixtures for entity classes");
			verify(logger).info("Configured entities: [com.codahale.shore.modules.test.fixtures.Cat, com.codahale.shore.modules.test.fixtures.Dog]");
		}
		
		@Test
		public void itHasAManagedSessionContext() throws Exception {
			assertThat(getProperty("hibernate.current_session_context_class"), is("managed"));
		}
		
		@Test
		public void itFormatsSQLQueries() throws Exception {
			assertThat(getProperty("hibernate.format_sql"), is("true"));
		}
		
		@Test
		public void itAddsCommentsToSQLQueries() throws Exception {
			assertThat(getProperty("hibernate.use_sql_comments"), is("true"));
		}
		
		@Test
		public void itKeepsUsageStatistics() throws Exception {
			assertThat(getProperty("hibernate.generate_statistics"), is("true"));
		}
		
		@Test
		public void itUsesC3P0sConnectionPool() throws Exception {
			assertThat(getProperty("hibernate.connection.provider_class"), is("org.hibernate.connection.C3P0ConnectionProvider"));
		}
		
		@Test
		public void itConfiguresC3P0toCheckConnectionValidity() throws Exception {
			assertThat(getProperty("hibernate.c3p0.idle_test_period"), is("3600"));
			assertThat(getProperty("hibernate.c3p0.timeout"), is("10800"));
			assertThat(getProperty("hibernate.c3p0.preferredTestQuery"), is("SELECT 1;"));
			assertThat(getProperty("hibernate.c3p0.maxConnectionAge"), is("14400"));
		}
		
		@Test
		public void itAddsAllAnnotatedEntityClassesInThePackages() throws Exception {
			final AnnotationConfiguration config = createModule().getConfiguration();
			
			assertThat(
				getAnnotatedClasses(config),
				hasItems("com.codahale.shore.modules.test.fixtures.Cat", "com.codahale.shore.modules.test.fixtures.Dog")
			);
		}
		
		private String getProperty(String name) throws Exception {
			final AnnotationConfiguration config = createModule().getConfiguration();
			return config.getProperties().getProperty(name);
		}
		
		// FIXME coda.hale@gmail.com -- Apr 29, 2009: Improve entity class scan tests.
		// Figure out a way to test entity class scanning without cracking open
		// AnnotationConfiguration's fields.
		@SuppressWarnings("unchecked")
		private List<String> getAnnotatedClasses(AnnotationConfiguration config) throws Exception {
			final Field annotatedClassesField = AnnotationConfiguration.class.getDeclaredField("annotatedClasses");
			annotatedClassesField.setAccessible(true);
			
			final Builder<String> annotatedClasses = ImmutableList.builder();
			for (XClass entityClass : (List<XClass>) annotatedClassesField.get(config)) {
				annotatedClasses.add(entityClass.toString());
			}
			return annotatedClasses.build();
		}
	}
	
	public static class Binding_The_Configuration extends Context {
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
		}
		
		@Ignore("figure out testing Guice modules")
		@Test
		public void itBindsHibernateConfigurationToTheModuleConfiguration() throws Exception {
			fail("not written yet");
		}
		
		@Ignore("figure out testing Guice modules")
		@Test
		public void itInstallsWarpsPersistenceService() throws Exception {
			fail("not written yet");
		}
		
		@Ignore("figure out testing Guice modules")
		@Test
		public void itBindsAHibernateInitializerAsAnEagerSingleton() throws Exception {
			fail("not written yet");
		}
	}
}
