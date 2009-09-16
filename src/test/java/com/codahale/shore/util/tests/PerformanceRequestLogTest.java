package com.codahale.shore.util.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.UserIdentity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.jdbc.Stopwatch;
import com.codahale.shore.util.PerformanceRequestLog;

@RunWith(Enclosed.class)
public class PerformanceRequestLogTest {
	public static class Logging_A_Request {
		private PerformanceRequestLog log;
		private Request request;
		private Response response;
		private ByteArrayOutputStream output;
		private AsyncContinuation continuation;
		private Authentication.User authentication;
		private UserIdentity userIdentity;
		private Principal principal;
		
		@Before
		public void setup() throws Exception {
			Stopwatch.getInstance().reset();
			Logger.getLogger("org.eclipse").setLevel(Level.OFF);
			
			this.output = new ByteArrayOutputStream();
			
			this.continuation = mock(AsyncContinuation.class);
			when(continuation.isInitial()).thenReturn(true);
			
			this.principal = mock(Principal.class);
			when(principal.getName()).thenReturn("30029");
			
			this.userIdentity = mock(UserIdentity.class);
			when(userIdentity.getUserPrincipal()).thenReturn(principal);
			
			this.authentication = mock(Authentication.User.class);
			when(authentication.getUserIdentity()).thenReturn(userIdentity);
			
			this.request = mock(Request.class);
			when(request.getUri()).thenReturn(new HttpURI("/test/1"));
			when(request.getMethod()).thenReturn("POST");
			when(request.getRemoteAddr()).thenReturn("10.0.0.0");
			when(request.getTimeStamp()).thenReturn(1246073144000L); // 27/Jun/2009:03:25:44 +0000
			when(request.getProtocol()).thenReturn("HTTP/1.1");
			when(request.getAsyncContinuation()).thenReturn(continuation);
			when(request.getAuthentication()).thenReturn(authentication);
			
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
