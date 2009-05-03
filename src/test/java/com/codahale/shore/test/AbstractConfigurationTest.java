package com.codahale.shore.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.util.List;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.servlet.GzipFilter;
import org.mortbay.servlet.ThrottlingFilter;
import org.mortbay.servlet.WelcomeFilter;

import com.codahale.shore.AbstractConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Stage;

@RunWith(Enclosed.class)
public class AbstractConfigurationTest {
	private static class MockModule extends AbstractModule {
		@Override
		protected void configure() {
			
		}
	}
	private static Module MOCK_MODULE = new MockModule();
	
	private static class MockConfiguration extends AbstractConfiguration {
		@Override
		public String getExecutableName() {
			return "dingo";
		}
		
		@Override
		protected void configure() {
			addEntityPackage("com.codahale.shore.test");
			addResourcePackage("com.codahale.shore.test");
			addModule(MOCK_MODULE);
			setStage(Stage.PRODUCTION);
			addServletFilter(GzipFilter.class, "/*");
			addServletFilter(ThrottlingFilter.class, "/hard-to-serve/*");
			addServletFilter(new FilterHolder(WelcomeFilter.class), "/welcome");
		}
		
		public void doConfig() {
			configure();
		}
	}
	
	public static class A_Configuration {
		private MockConfiguration config;
		
		@Before
		public void setup() throws Exception {
			this.config = new MockConfiguration();
			config.doConfig();
		}
		
		@Test
		public void itHasAListOfEntityPackages() throws Exception {
			assertThat(
				config.getEntityPackages(),
				hasItem("com.codahale.shore.test")
			);
		}
		
		@Test
		public void itHasAListOfResourcePackages() throws Exception {
			assertThat(
				config.getResourcePackages(),
				hasItem("com.codahale.shore.test")
			);
		}
		
		@Test
		public void itHasGuiceModules() throws Exception {
			assertThat(
				config.getModules(),
				hasItem(MOCK_MODULE)
			);
		}
		
		@Test
		public void itHasAGuiceStage() throws Exception {
			assertThat(config.getStage(), is(Stage.PRODUCTION));
		}
		
		@Test
		public void itHasAConnector() throws Exception {
			assertThat(config.getConnector(), is(SocketConnector.class));
		}
		
		@Test
		public void itHasServletFiltersInOrderOfAddition() throws Exception {
			final List<String> filters = Lists.newLinkedList();
			final List<String> patterns = Lists.newLinkedList();
			for (Entry<FilterHolder, String> entry : config.getServletFilters().entrySet()) {
				filters.add(entry.getKey().getHeldClass().getName());
				patterns.add(entry.getValue());
			}
			
			final List<String> expectedFilterClasses = ImmutableList.of("org.mortbay.servlet.GzipFilter", "org.mortbay.servlet.ThrottlingFilter", "org.mortbay.servlet.WelcomeFilter");
			assertThat(filters, is(expectedFilterClasses));
			
			final List<String> expectedUrlPatterns = ImmutableList.of("/*", "/hard-to-serve/*", "/welcome");
			assertThat(patterns, is(expectedUrlPatterns));
		}
	}
}
