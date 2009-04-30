package com.codahale.shore.modules.test;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.modules.HibernateInitializer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wideplay.warp.persist.PersistenceService;

@RunWith(Enclosed.class)
public class HibernateInitializerTest {
	public static class Creating_A_Hibernate_Initializer {
		@Test
		public void itStartsAPersistenceService() throws Exception {
			final PersistenceService service = mock(PersistenceService.class);
			
			final Injector injector = Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(PersistenceService.class).toInstance(service);
				}
			});
			
			injector.getInstance(HibernateInitializer.class);
			
			verify(service).start();
		}
	}
}
