package com.codahale.shore.modules.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.modules.HibernateInitializer;
import com.codahale.shore.modules.HibernateModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.internal.InstanceBindingImpl;
import com.google.inject.internal.ProviderInstanceBindingImpl;
import com.google.inject.internal.Scoping;
import com.google.inject.internal.UntargettedBindingImpl;
import com.google.inject.spi.Element;
import com.google.inject.spi.RecordingBinder;
import com.wideplay.warp.persist.PersistenceService;

@RunWith(Enclosed.class)
public class HibernateModuleTest {
	private static abstract class Context {
		protected Logger logger;
		
		public void setup() throws Exception {
			Logger.getLogger("org.hibernate").setLevel(Level.OFF);
			this.logger = mock(Logger.class);
		}
		
		protected HibernateModule createModule() throws Exception {
			final Properties properties = new Properties();
			properties.setProperty(Environment.DIALECT, "org.hibernate.dialect.HSQLDialect");
			properties.setProperty(Environment.DRIVER, "org.hsqldb.jdbcDriver");
			properties.setProperty(Environment.USER, "sa");
			properties.setProperty(Environment.PASS, "");
			properties.setProperty(Environment.URL, "jdbc:hsqldb:mem:ShoreServerCommandTest");
			properties.setProperty(Environment.POOL_SIZE, "1");
			properties.setProperty(Environment.SHOW_SQL, "true");
			properties.setProperty(Environment.HBM2DDL_AUTO, "create-drop");
			
			return new HibernateModule(
				logger,
				properties,
				ImmutableList.of("com.codahale.shore.modules.test.fixtures")
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
		private HibernateModule module;
		
		@Before
		@Override
		public void setup() throws Exception {
			super.setup();
			this.module = createModule();
		}
		
		@Test
		public void itBindsHibernateConfigurationToTheModuleConfiguration() throws Exception {
			final Map<Key<?>, Object> bindings = getBindings(module);
			
			assertThat(bindings.get(Key.get(Configuration.class)), sameInstance((Object) module.getConfiguration()));
		}
		
		@Test
		public void itInstallsWarpsHibernatePersistenceService() throws Exception {
			final Map<Key<?>, Object> bindings = getBindings(module);
			final String className = bindings.get(Key.get(PersistenceService.class)).getClass().getCanonicalName();
			
			assertThat(className, is("com.wideplay.warp.hibernate.HibernatePersistenceService"));
		}
		
		@Test
		public void itBindsAHibernateInitializerAsAnEagerSingleton() throws Exception {
			final Map<Key<?>, Object> bindings = getBindings(module);
			final Scoping scoping = (Scoping) bindings.get(Key.get(HibernateInitializer.class));
			
			assertThat(scoping, is(Scoping.EAGER_SINGLETON));
		}
		
		/*
		 * HOLY CRAP THIS IS UGLY
		 * 
		 * Why yes. Yes it is. I haven't found a good way to accurately test
		 * Guice modules, so I ended up pasting in a bunch of code and banging
		 * it around until it worked. Fun, neh?
		 */
		private Map<Key<?>, Object> getBindings(HibernateModule module) {
			final Map<Key<?>, Object> bindings = Maps.newLinkedHashMap();
			final RecordingBinder binder = new RecordingBinder(Stage.PRODUCTION);
			module.configure(binder);
			for (Element element : binder.getElements()) {
				if (element instanceof InstanceBindingImpl<?>) {
					final InstanceBindingImpl<?> binding = (InstanceBindingImpl<?>) element;
					bindings.put(binding.getKey(), binding.getInstance());
				} else if (element instanceof ProviderInstanceBindingImpl<?>) {
					final ProviderInstanceBindingImpl<?> binding = (ProviderInstanceBindingImpl<?>) element;
					bindings.put(binding.getKey(), binding.getProviderInstance());
				} else if (element instanceof UntargettedBindingImpl<?>) {
					final UntargettedBindingImpl<?> binding = (UntargettedBindingImpl<?>) element;
					bindings.put(binding.getKey(), binding.getScoping());
				}
			}
			return bindings;
		}
	}
}
