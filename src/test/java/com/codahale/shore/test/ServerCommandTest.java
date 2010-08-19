package com.codahale.shore.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.AbstractConfiguration;
import com.codahale.shore.ServerCommand;

@RunWith(Enclosed.class)
public class ServerCommandTest {
	public static class A_Server_Command {
		private ServerCommand cmd;
		private AbstractConfiguration config;
		private Properties properties;
		
		
		@Before
		public void setup() throws Exception {
			this.config = mock(AbstractConfiguration.class);
			this.properties = new Properties();
			
			
			this.cmd = new ServerCommand(config, "localhost", 8080, false, properties);
		}
		
		@Test
		public void itHasAConfiguration() throws Exception {
			assertThat(cmd.getConfiguration(), is(sameInstance(config)));
		}
		
		@Test
		public void itHasAHost() throws Exception {
			assertThat(cmd.getHost(), is("localhost"));
		}
		
		@Test
		public void itHasAPort() throws Exception {
			assertThat(cmd.getPort(), is(8080));
		}
		
		@Test
		public void itHasProperties() throws Exception {
			assertThat(cmd.getProperties(), is(sameInstance(properties)));
		}
		
		@Test
		public void itHasAGracefulShutdown() throws Exception {
			assertThat(cmd.getGracefulShutdown(), is(false));
		}
	}
}
