package com.codahale.shore.injection.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Type;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.injection.AbstractInjectionProvider;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;

@RunWith(Enclosed.class)
public class AbstractInjectionProviderTest {
	private static class StringInjectionProvider extends AbstractInjectionProvider<String> {

		public StringInjectionProvider() {
			super(String.class);
		}

		@Override
		public String getValue(HttpContext c) {
			return c.toString();
		}
	}
	
	public static class A_Descendant_Of_AbstractInjectionProvider {
		private final StringInjectionProvider provider = new StringInjectionProvider();
		
		@Test
		public void itHasAnInjectedType() throws Exception {
			assertThat(provider.getInjectedType(), is(sameInstance((Type) String.class)));
		}
		
		@Test
		public void itIsItsOwnInjectableForItsInjectedType() throws Exception {
			assertThat(provider.getInjectable(null, null, String.class), is(sameInstance((Injectable<String>) provider)));
		}
		
		@Test
		public void itDoesNotInjectOtherTypes() throws Exception {
			assertThat(provider.getInjectable(null, null, Integer.class), is(nullValue()));
		}
		
		@Test
		public void itIsAPerRequestInjection() throws Exception {
			assertThat(provider.getScope(), is(ComponentScope.PerRequest));
		}
		
		@Test
		public void itInjectsValuesFromTheHttpContect() throws Exception {
			final HttpContext context = mock(HttpContext.class);
			when(context.toString()).thenReturn("I'm a string.");
			
			assertThat(provider.getValue(context), is("I'm a string."));
		}
	}
}
