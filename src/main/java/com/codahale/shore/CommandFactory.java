package com.codahale.shore;

import static com.google.common.base.Preconditions.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import net.jcip.annotations.Immutable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Turns command line arguments into {@link Runnable} commands.
 * 
 * @author coda
 *
 */
@Immutable
public class CommandFactory {
	private final static String MAIN_USAGE_TEMPLATE =
				"usage: {app} <subcommand> [options]\n" +
				"\n" +
				"Available subcommands:\n" +
				"   server    Run {app} as an HTTP server.\n" +
				"   schema    Generate a database schema for {app}.\n" +
				"\n" +
				"Type '{app} <subcommand> --help' for help on a specific subcommand.";
	private final static String SERVER_USAGE_TEMPLATE =
				"usage: {app} server -c <file> -p <port>\n" +
				"   -c, --config=FILE    Which Hibernate config file to use\n" +
				"   -p, --port=PORT      Which port to bind to\n" +
				"   -jarent, --jar_entities=jar_file_name		JAR file to entities";
	private final static String SCHEMA_USAGE_TEMPLATE =
		"usage: {app} schema -c <file> [--migration]\n" +
		"   -c, --config=FILE    Which Hibernate config file to use\n" +
		"   --migration          Generate a migration script";
	private final AbstractConfiguration configuration;
	private final GnuParser parser;
	
	/**
	 * Creates a new {@link CommandFactory} for an application.
	 * 
	 * @param configuration the application's configuration
	 */
	public CommandFactory(AbstractConfiguration configuration) {
		this.configuration = checkNotNull(configuration);
		this.parser = new GnuParser();
	}
	
	public Runnable getCommand(String... arguments) {
		checkNotNull(arguments);
		if (isCommand(arguments, "server")) {
			return buildServerCommand(arguments);
		} else if (isCommand(arguments, "schema")) {
			return buildSchemaCommand(arguments);
		}
		
		return new HelpCommand(mainUsage(), System.out);
	}

	private Runnable buildSchemaCommand(String[] arguments) {
		final String[] subArgs = getSubArgs(arguments);
		
		if (isHelpCommand(subArgs)) {
			return new HelpCommand(schemaUsage(null), System.out);
		}
		
		final Options options = new Options();
		
		final Option configOption = new Option("c", "config", true, null);
		configOption.setRequired(true);
		options.addOption(configOption);
		
		options.addOption(null, "migration", false, null);
		
		try {
			final CommandLine cmdLine = parser.parse(options, subArgs);
			
			final Properties properties = new Properties();
			final FileReader reader = new FileReader(cmdLine.getOptionValue("c"));
			try {
				properties.load(reader);
			} finally {
				reader.close();
			}
			final boolean migration = cmdLine.hasOption("migration");
			
			return new SchemaCommand(configuration, properties, migration, System.out);
		} catch (ParseException e) {
			return new HelpCommand(schemaUsage(e.getMessage()), System.out);
		} catch (FileNotFoundException e) {
			return new HelpCommand(schemaUsage("Config file does not exist"), System.out);
		} catch (IOException e) {
			return new HelpCommand(schemaUsage("Unable to read config file"), System.out);
		}
	}

	

	private boolean isCommand(String[] arguments, String command) {
		return (arguments.length > 0) && arguments[0].equals(command);
	}

	private Runnable buildServerCommand(String[] arguments) {
		final String[] subArgs = getSubArgs(arguments);
		
		if (isHelpCommand(subArgs)) {
			return new HelpCommand(serverUsage(null), System.out);
		}
		
		final Options options = new Options();
		
		final Option configOption = new Option("c", "config", true, null);
		configOption.setRequired(true);
		options.addOption(configOption);
		
		final Option portOption = new Option("p", "port", true, null);
		portOption.setRequired(true);
		options.addOption(portOption);
		
		final Option jarOption = new Option("jarent", "jar_entities", true, null);
		portOption.setRequired(true);
		options.addOption(jarOption);
		
		options.addOption(null, "graceless", false, null);
		
		try {
			
			final CommandLine cmdLine = parser.parse(options, subArgs);
			
			final Properties properties = new Properties();
			final FileReader reader = new FileReader(cmdLine.getOptionValue("c"));
			try {
				properties.load(reader);
			} finally {
				reader.close();
			}
			final int port = Integer.valueOf(cmdLine.getOptionValue("p"));		
			
			final String jarent = cmdLine.getOptionValue("jarent");
			
			ServerCommand ser = new ServerCommand(configuration, port, !cmdLine.hasOption("graceless"), properties);
			if ( jarent != null && jarent.length() > 0 )
				ser.setJarent(jarent);
			return ser;
		} catch (ParseException e) {
			return new HelpCommand(serverUsage(e.getMessage()), System.out);
		} catch (FileNotFoundException e) {
			return new HelpCommand(serverUsage("Config file does not exist"), System.out);
		} catch (IOException e) {
			return new HelpCommand(serverUsage("Unable to read config file"), System.out);
		} catch (NumberFormatException e) {
			return new HelpCommand(serverUsage("Invalid port number"), System.out);
		}
	}

	private boolean isHelpCommand(final String[] subArgs) {
		return (subArgs.length == 0) || ((subArgs.length == 1) && (subArgs[0].equals("-h") || subArgs[0].equals("--help")));
	}

	private String[] getSubArgs(String[] arguments) {
		return Arrays.copyOfRange(arguments, 1, arguments.length);
	}
	
	private String mainUsage() {
		return usage(MAIN_USAGE_TEMPLATE, null);
	}
	
	private String schemaUsage(String error) {
		return usage(SCHEMA_USAGE_TEMPLATE, error);
	}
	
	private String serverUsage(String message) {
		return usage(SERVER_USAGE_TEMPLATE, message);
	}

	private String usage(String template, String error) {
		final StringBuilder builder = new StringBuilder();
		if (error != null) {
			builder.append("Error: ").append(error).append("\n\n");
		}
		return builder.append(template.replace("{app}", configuration.getExecutableName())).toString();
	}
}

