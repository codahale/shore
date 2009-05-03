package com.codahale.shore;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 * Prints usage information for an application to an output stream.
 * 
 * @author coda
 * 
 */
public class HelpCommand implements Runnable {
	private final OutputStream outputStream;
	private final Options options;
	private final String name;

	/**
	 * Creates a new {@link HelpCommand}.
	 * 
	 * @param name
	 *            the application's name
	 * @param options
	 *            command line options
	 * @param outputStream
	 *            the stream to print usage information to
	 */
	public HelpCommand(String name, Options options, OutputStream outputStream) {
		this.name = name;
		this.outputStream = outputStream;
		this.options = options;
	}

	/**
	 * Returns the application's name
	 * 
	 * @return the application's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the command line options.
	 * 
	 * @return the command line options
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * Returns the output stream.
	 * 
	 * @return the output stream.
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public void run() {
		final PrintWriter out = new PrintWriter(outputStream);
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(out, HelpFormatter.DEFAULT_WIDTH, name, null, options,
				HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null, true);
		out.flush();
	}
}
