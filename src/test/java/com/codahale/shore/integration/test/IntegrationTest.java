package com.codahale.shore.integration.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.codahale.shore.Shore;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;

public class IntegrationTest {
	private static IntegrationTestConfig config = new IntegrationTestConfig();
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	private static int port = new Random().nextInt(10000) + 8000;
	
	@BeforeClass
	public static void setupThread() throws Exception {
		Logger.getLogger("org.hibernate").setLevel(Level.OFF);
		Logger.getLogger("org.mortbay").setLevel(Level.OFF);
		Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
		Logger.getLogger("com.mchange").setLevel(Level.OFF);
		Logger.getLogger("com.codahale").setLevel(Level.OFF);
		
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Shore.run(config, "server", "-p", Integer.toString(port), "-c", "src/test/resources/hsql-memory.properties");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Thread.sleep(5000);
	}
	
	@Test
	public void itHandlesRequestsAndStoresAndRetrievesObjectsFromADatabase() throws Exception {
		final Client client = new Client();
		final WebResource r = client.resource("http://localhost:" + port + "/widget/dingo");

		try {
			r.get(String.class);
			fail("should have been a 404");
		} catch (UniformInterfaceException e) {
			assertThat(e.getResponse().getStatus(), is(404));
		}
		
		final ClientResponse response = r.post(ClientResponse.class, "awesome");
		assertThat(response.getStatus(), is(201));
		assertThat(response.getLocation(), is(r.getURI()));
		
		assertThat(r.get(String.class), is("[Widget name:dingo, description:awesome]"));
		
		assertThat(r.header("Accept-Encoding", "gzip").get(String.class), is(not("[Widget name:dingo, description:awesome]")));
		
		client.addFilter(new GZIPContentEncodingFilter());
		
		assertThat(r.header("Accept-Encoding", "gzip").get(String.class), is("[Widget name:dingo, description:awesome]"));
	}
	
	@AfterClass
	public static void teardownThread() throws Exception {
		executor.shutdown();
	}
}
