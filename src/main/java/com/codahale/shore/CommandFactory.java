package com.codahale.shore;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Turns command line arguments into {@link Runnable} commands.
 * 
 * @author coda
 *
 */
public class CommandFactory {
	private final AbstractConfiguration configuration;
	private final Options options;
	
	/**
	 * Creates a new {@link CommandFactory} for an application.
	 * 
	 * @param configuration the application's configuration
	 */
	public CommandFactory(AbstractConfiguration configuration) {
		this.configuration = configuration;
		this.options = new Options();
		options.addOption("p", "port", true, "port to listen on");
		options.addOption("f", "file", true, "filename of config file");
		options.addOption("h", "help", false, "show this help message");
	}

	/**
	 * Parses {@code arguments} and returns either {@link ServerCommand} or
	 * {@link HelpCommand}.
	 * 
	 * @param arguments an array of command line arguments
	 * @return either {@link ServerCommand} or {@link HelpCommand}
	 */
	public Runnable getCommand(String... arguments) {
		final CommandLineParser parser = new GnuParser();
		try {
			final CommandLine line = parser.parse(options, arguments);
			
			if (arguments.length == 0 || line.hasOption("h")) {
				return buildHelpCommand(null);
			}
			
			if (!line.hasOption("f")) {
				return buildHelpCommand("--file is required.");
			}
			
			if (!line.hasOption("p")) {
				return buildHelpCommand("--port is required.");
			}
			
			final String filename = line.getOptionValue("f");
			final String port = line.getOptionValue("p");
			
			try {
				final Properties properties = new Properties();
				properties.load(new FileReader(filename));
				
				return new ServerCommand(configuration, Integer.valueOf(port), properties);
				
			} catch (NumberFormatException e) {
				return buildHelpCommand(port + " is not a valid port number.");
			} catch (FileNotFoundException e) {
				return buildHelpCommand(filename + " does not exist.");
			} catch (IOException e) {
				return buildHelpCommand(filename + " could not be read.");
			}
			
		} catch (ParseException e) {
			return buildHelpCommand(e.getMessage());
		}
	}

	private HelpCommand buildHelpCommand(final String msg) {
		return new HelpCommand(configuration.getExecutableName(), msg, options, System.err);
	}

}
