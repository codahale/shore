package com.codahale.shore.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.OutputStream;

import org.hibernate.cfg.Environment;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.AbstractConfiguration;
import com.codahale.shore.CommandFactory;
import com.codahale.shore.HelpCommand;
import com.codahale.shore.SchemaCommand;
import com.codahale.shore.ServerCommand;

@RunWith(Enclosed.class)
public class CommandFactoryTest {
	private static final AbstractConfiguration config = new AbstractConfiguration() {
		@Override
		protected void configure() {
			
		}

		@Override
		public String getExecutableName() {
			return "shoretest";
		}
	};
	
	private static Runnable parse(String... args) {
		return new CommandFactory(config).getCommand(args);
	}
	
	public static class No_Arguments {
		@Test
		public void itReturnsAHelpCommandWithASubcommandList() throws Exception {
			final Runnable runnable = parse();
			assertThat(runnable, is(HelpCommand.class));
			
			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"usage: shoretest <subcommand> [options]\n" +
				"\n" +
				"Available subcommands:\n" +
				"   server    Run shoretest as an HTTP server.\n" +
				"   schema    Generate a database schema for shoretest.\n" +
				"\n" +
				"Type 'shoretest <subcommand> --help' for help on a specific subcommand."
			));
		}
	}
	
	public static class A_Short_Help_Command {
		@Test
		public void itReturnsAHelpCommandWithASubcommandList() throws Exception {
			final Runnable runnable = parse("-h");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"usage: shoretest <subcommand> [options]\n" +
				"\n" +
				"Available subcommands:\n" +
				"   server    Run shoretest as an HTTP server.\n" +
				"   schema    Generate a database schema for shoretest.\n" +
				"\n" +
				"Type 'shoretest <subcommand> --help' for help on a specific subcommand."
			));
		}
	}

	public static class A_Long_Help_Command {
		@Test
		public void itReturnsAHelpCommandWithASubcommandList() throws Exception {
			final Runnable runnable = parse("--help");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"usage: shoretest <subcommand> [options]\n" +
				"\n" +
				"Available subcommands:\n" +
				"   server    Run shoretest as an HTTP server.\n" +
				"   schema    Generate a database schema for shoretest.\n" +
				"\n" +
				"Type 'shoretest <subcommand> --help' for help on a specific subcommand."
			));
		}
	}

	public static class Server_Without_Args {
		@Test
		public void itReturnsAHelpCommandWithServerOptions() throws Exception {
			final Runnable runnable = parse("server");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"usage: shoretest server -c <file> -p <port>\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   -p, --port=PORT      Which port to bind to"
			));
		}
	}
	
	public static class Server_With_A_Short_Help_Flag {
		@Test
		public void itReturnsAHelpCommandWithServerOptions() throws Exception {
			final Runnable runnable = parse("server", "-h");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"usage: shoretest server -c <file> -p <port>\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   -p, --port=PORT      Which port to bind to"
			));
			
		}
	}
	
	public static class Server_With_A_Long_Help_Flag {
		@Test
		public void itReturnsAHelpCommandWithServerOptions() throws Exception {
			final Runnable runnable = parse("server", "--help");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"usage: shoretest server -c <file> -p <port>\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   -p, --port=PORT      Which port to bind to"
			));
			
		}
	}

	public static class Server_With_No_Config_File {
		@Test
		public void itReturnsAHelpCommandWithServerOptions() throws Exception {
			final Runnable runnable = parse("server", "--port=8080");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"Error: Missing required option: c\n" +
				"\n" +
				"usage: shoretest server -c <file> -p <port>\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   -p, --port=PORT      Which port to bind to"
			));
		}
	}

	public static class Server_With_No_Port {
		@Test
		public void itReturnsAHelpCommandWithServerOptions() throws Exception {
			final Runnable runnable = parse("server", "--config=src/test/resources/hsql-memory.properties");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"Error: Missing required option: p\n" +
				"\n" +
				"usage: shoretest server -c <file> -p <port>\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   -p, --port=PORT      Which port to bind to"
			));
		}
	}

	public static class Server_With_A_Missing_Config_File {
		@Test
		public void itReturnsAHelpCommandWithServerOptions() throws Exception {
			final Runnable runnable = parse("server", "--config=src/test/resources/not-here.properties", "--port=8080");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"Error: Config file does not exist\n" +
				"\n" +
				"usage: shoretest server -c <file> -p <port>\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   -p, --port=PORT      Which port to bind to"
			));
		}
	}

	public static class Server_With_Malformed_Arguments {
		@Test
		public void itReturnsAHelpCommandWithServerOptions() throws Exception {
			final Runnable runnable = parse("server", "--ffffuuuuu");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"Error: Unrecognized option: --ffffuuuuu\n" +
				"\n" +
				"usage: shoretest server -c <file> -p <port>\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   -p, --port=PORT      Which port to bind to"
			));
		}
	}

	public static class Server_With_A_Bad_Port_Number {
		@Test
		public void itReturnsAHelpCommandWithServerOptions() throws Exception {
			final Runnable runnable = parse("server", "--config=src/test/resources/hsql-memory.properties", "--port=808f0");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"Error: Invalid port number\n" +
				"\n" +
				"usage: shoretest server -c <file> -p <port>\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   -p, --port=PORT      Which port to bind to"
			));
		}
	}

	public static class Server_With_A_Config_File_And_A_Port {
		@Test
		public void itReturnsAServerCommand() throws Exception {
			assertCorrectServerCommand(
				parse("server", "-c", "src/test/resources/hsql-memory.properties", "-p", "8080")
			);
		}

		@Test
		public void itHandlesALongFileFlag() throws Exception {
			assertCorrectServerCommand(
				parse("server", "--config", "src/test/resources/hsql-memory.properties", "-p", "8080")
			);
		}

		@Test
		public void itHandlesALongPortFlag() throws Exception {
			assertCorrectServerCommand(
				parse("server", "--config", "src/test/resources/hsql-memory.properties", "--port", "8080")
			);
		}

		@Test
		public void itHandlesAGroupedFileFlag() throws Exception {
			assertCorrectServerCommand(
				parse("server", "--config=src/test/resources/hsql-memory.properties", "-p", "8080")
			);
		}

		@Test
		public void itHandlesAGroupedPortFlag() throws Exception {
			assertCorrectServerCommand(
				parse("server", "--config=src/test/resources/hsql-memory.properties", "--port=8080")
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
	
	public static class Schema_Without_Args {
		@Test
		public void itReturnsAHelpCommandWithSchemaOptions() throws Exception {
			final Runnable runnable = parse("schema");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"usage: shoretest schema -c <file> [--migration]\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   --migration          Generate a migration script"
			));
		}
	}
	
	public static class Schema_With_A_Short_Help_Flag {
		@Test
		public void itReturnsAHelpCommandWithSchemaOptions() throws Exception {
			final Runnable runnable = parse("schema", "-h");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"usage: shoretest schema -c <file> [--migration]\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   --migration          Generate a migration script"
			));
		}
	}
	
	public static class Schema_With_A_Long_Help_Flag {
		@Test
		public void itReturnsAHelpCommandWithSchemaOptions() throws Exception {
			final Runnable runnable = parse("schema", "--help");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"usage: shoretest schema -c <file> [--migration]\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   --migration          Generate a migration script"
			));
		}
	}
	
	public static class Schema_With_A_Missing_Config_File {
		@Test
		public void itReturnsAHelpCommandWithSchemaOptions() throws Exception {
			final Runnable runnable = parse("schema", "--config=src/test/resources/not-here.properties");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"Error: Config file does not exist\n" +
				"\n" +
				"usage: shoretest schema -c <file> [--migration]\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   --migration          Generate a migration script"
			));
		}
	}

	public static class Schema_With_Malformed_Arguments {
		@Test
		public void itReturnsAHelpCommandWithSchemaOptions() throws Exception {
			final Runnable runnable = parse("schema", "--ffffuuuuu");
			assertThat(runnable, is(HelpCommand.class));

			final HelpCommand cmd = (HelpCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getText(), is(
				"Error: Unrecognized option: --ffffuuuuu\n" +
				"\n" +
				"usage: shoretest schema -c <file> [--migration]\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   --migration          Generate a migration script"
			));
		}
	}
	
	public static class Schema_With_A_Config_File {
		@Test
		public void itReturnsAFullSchemaCommand() throws Exception {
			final Runnable runnable = parse("schema", "-c", "src/test/resources/hsql-memory.properties");
			assertThat(runnable, is(SchemaCommand.class));
			
			final SchemaCommand cmd = (SchemaCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getConfiguration(), is(sameInstance(config)));
			assertThat(cmd.isMigration(), is(false));
			assertThat(cmd.getProperties().getProperty(Environment.URL), is("jdbc:hsqldb:mem:ShoreTest"));
		}
	}
	
	public static class Schema_With_A_Config_File_And_Migration {
		@Test
		public void itReturnsAMigrationSchemaCommand() throws Exception {
			final Runnable runnable = parse("schema", "-c", "src/test/resources/hsql-memory.properties", "--migration");
			assertThat(runnable, is(SchemaCommand.class));
			
			final SchemaCommand cmd = (SchemaCommand) runnable;
			assertThat(cmd.getOutputStream(), is(sameInstance((OutputStream) System.out)));
			assertThat(cmd.getConfiguration(), is(sameInstance(config)));
			assertThat(cmd.isMigration(), is(true));
			assertThat(cmd.getProperties().getProperty(Environment.URL), is("jdbc:hsqldb:mem:ShoreTest"));
		}
	}
}
