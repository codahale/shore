package com.codahale.shore.params.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.params.AbstractParam;

@RunWith(Enclosed.class)
public class AbstractParamTest {
	private static class IntegerParam extends AbstractParam<Integer> {

		public IntegerParam(String param) throws WebApplicationException {
			super(param);
		}

		@Override
		protected Integer parse(String param) throws Exception {
			return Integer.valueOf(param);
		}
	}
	
	public static class An_AbstractParam_Descendant {
		@Test
		public void itParsesAStringParameterAndReturnsAParamWithAValue() throws Exception {
			final IntegerParam param = new IntegerParam("200");
			
			assertThat(param.getValue(), is(200));
			assertThat(param.toString(), is("200"));
		}
		
		@Test
		public void itThrowsAWebApplicationExceptionForUnparsableValues() throws Exception {
			try {
				new IntegerParam("200$$00");
				fail("should have thrown a WebApplicationException but didn't");
			} catch (WebApplicationException e) {
				final Response response = e.getResponse();
				assertThat(response.getStatus(), is(400));
				assertThat((String) response.getEntity(), is("Invalid parameter: 200$$00 (For input string: \"200$$00\")."));
			}
		}
	}
}
