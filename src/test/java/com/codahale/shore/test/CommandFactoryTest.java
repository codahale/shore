package com.codahale.shore.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.hibernate.cfg.Environment;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.AbstractConfiguration;
import com.codahale.shore.CommandFactory;
import com.codahale.shore.HelpCommand;
import com.codahale.shore.ServerCommand;

@RunWith(Enclosed.class)
public class CommandFactoryTest {
	private static final AbstractConfiguration config = mock(AbstractConfiguration.class);
	
	private static Runnable parse(String... args) {
		return new CommandFactory(config).getCommand(args);
	}
	
	public static class No_Arguments {
		@Test
		public void itReturnsAHelpCommand() throws Exception {
			final Runnable runnable = parse();
			assertThat(runnable, is(HelpCommand.class));
			
			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getErrorMessage(), is(nullValue()));
		}
	}
	
	public static class A_Short_Help_Command {
		@Test
		public void itReturnsAHelpCommand() throws Exception {
			final Runnable runnable = parse("-h");
			assertThat(runnable, is(HelpCommand.class));
			
			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getErrorMessage(), is(nullValue()));
		}
	}
	
	public static class A_Long_Help_Command {
		@Test
		public void itReturnsAHelpCommand() throws Exception {
			final Runnable runnable = parse("--help");
			assertThat(runnable, is(HelpCommand.class));
			
			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getErrorMessage(), is(nullValue()));
		}
	}
	
	public static class No_Config_File {
		@Test
		public void itReturnsAHelpCommand() throws Exception {
			final Runnable runnable = parse("--port=8080");
			assertThat(runnable, is(HelpCommand.class));
			
			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getErrorMessage(), is("--file is required."));
		}
	}
	
	public static class No_Port {
		@Test
		public void itReturnsAHelpCommand() throws Exception {
			final Runnable runnable = parse("--file=src/test/resources/hsql-memory.properties");
			assertThat(runnable, is(HelpCommand.class));
			
			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getErrorMessage(), is("--port is required."));
		}
	}
	
	public static class A_Missing_Config_File {
		@Test
		public void itReturnsAHelpCommand() throws Exception {
			final Runnable runnable = parse("--file=src/test/resources/not-here.properties", "--port=8080");
			assertThat(runnable, is(HelpCommand.class));
			
			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getErrorMessage(), is("src/test/resources/not-here.properties does not exist."));
		}
	}
	
	public static class Malformed_Arguments {
		@Test
		public void itReturnsAHelpCommand() throws Exception {
			final Runnable runnable = parse("--ffffuuuuu");
			assertThat(runnable, is(HelpCommand.class));
			
			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getErrorMessage(), is("Unrecognized option: --ffffuuuuu"));
		}
	}
	
	public static class A_Bad_Port_Number {
		@Test
		public void itReturnsAHelpCommand() throws Exception {
			final Runnable runnable = parse("--file=src/test/resources/hsql-memory.properties", "--port=808f0");
			assertThat(runnable, is(HelpCommand.class));
			
			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getErrorMessage(), is("808f0 is not a valid port number."));
		}
	}
	
	public static class A_Config_File_And_A_Port {
		@Test
		public void itReturnsAServerCommand() throws Exception {
			assertCorrectServerCommand(
				parse("-f", "src/test/resources/hsql-memory.properties", "-p", "8080")
			);
		}
		
		@Test
		public void itHandlesALongFileFlag() throws Exception {
			assertCorrectServerCommand(
				parse("--file", "src/test/resources/hsql-memory.properties", "-p", "8080")
			);
		}
		
		@Test
		public void itHandlesALongPortFlag() throws Exception {
			assertCorrectServerCommand(
				parse("--file", "src/test/resources/hsql-memory.properties", "--port", "8080")
			);
		}
		
		@Test
		public void itHandlesAGroupedFileFlag() throws Exception {
			assertCorrectServerCommand(
				parse("--file=src/test/resources/hsql-memory.properties", "-p", "8080")
			);
		}
		
		@Test
		public void itHandlesAGroupedPortFlag() throws Exception {
			assertCorrectServerCommand(
				parse("--file=src/test/resources/hsql-memory.properties", "--port=8080")
			);
		}

		private void assertCorrectServerCommand(final Runnable runnable) {
			assertThat(runnable, is(ServerCommand.class));
			
			final ServerCommand cmd = (ServerCommand) runnable;
			assertThat(cmd.getConfiguration(), is(sameInstance(config)));
			assertThat(cmd.getPort(), is(8080));
			assertThat(cmd.getProperties().getProperty(Environment.URL), is("jdbc:hsqldb:mem:ShoreTest"));
		}
	}
}
