package com.codahale.shore.util.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mortbay.jetty.HttpURI;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

import com.codahale.jdbc.Stopwatch;
import com.codahale.shore.util.PerformanceRequestLog;

@RunWith(Enclosed.class)
public class PerformanceRequestLogTest {
	public static class Logging_A_Request {
		private PerformanceRequestLog log;
		private Request request;
		private Response response;
		private ByteArrayOutputStream output;
		
		@Before
		public void setup() throws Exception {
			Stopwatch.getInstance().reset();
			Logger.getLogger("org.mortbay.log").setLevel(Level.OFF);
			
			this.output = new ByteArrayOutputStream();
			
			this.request = mock(Request.class);
			when(request.getUri()).thenReturn(new HttpURI("/test/1"));
			when(request.getMethod()).thenReturn("POST");
			when(request.getRemoteAddr()).thenReturn("10.0.0.0");
			when(request.getRemoteUser()).thenReturn("30029");
			when(request.getTimeStamp()).thenReturn(1246073144000L); // 27/Jun/2009:03:25:44 +0000
			when(request.getProtocol()).thenReturn("HTTP/1.1");
			
			this.response = mock(Response.class);
			when(response.getStatus()).thenReturn(200);
			when(response.getContentCount()).thenReturn(299181L);
			
			this.log = new PerformanceRequestLog();
			log.start();
			
			Field field = NCSARequestLog.class.getDeclaredField("_writer");
			field.setAccessible(true);
			field.set(log, new OutputStreamWriter(output));
		}
		
		@After
		public void teardown() throws Exception {
			log.stop();
		}
		
		@Test
		public void itPrintsTheTimeSpentInTheDatabase() throws Exception {
			Stopwatch.getInstance().start();
			Thread.sleep(123);
			Stopwatch.getInstance().stop();
			
			log.log(request, response);
			
			assertTrue(output.toString().startsWith("10.0.0.0 - 30029 [27/Jun/2009:03:25:44 +0000] \"POST /test/1 HTTP/1.1\" 200 299181 \"-\" \"-\"  123 "));
		}
	}
}
