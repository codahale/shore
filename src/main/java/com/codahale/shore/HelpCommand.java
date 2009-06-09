package com.codahale.shore;

import static com.google.common.base.Preconditions.*;

import java.io.OutputStream;
import java.io.PrintWriter;

import net.jcip.annotations.Immutable;

/**
 * Prints usage information for an application to an output stream.
 * 
 * @author coda
 * 
 */
@Immutable
public class HelpCommand implements Runnable {
	private final String text;
	private final OutputStream outputStream;

	/**
	 * Creates a new {@link HelpCommand}.
	 * 
	 * @param text the help text to display
	 * @param outputStream
	 *            the stream to print the text to
	 */
	public HelpCommand(String text, OutputStream outputStream) {
		this.text = checkNotNull(text);
		this.outputStream = checkNotNull(outputStream);
	}
	
	public String getText() {
		return text;
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
		out.println(text);
		out.flush();
	}
}
