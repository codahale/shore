package com.codahale.shore;

import static com.google.common.base.Preconditions.*;

/**
 * The entry point for Shore applications. Run this using a class in your
 * application:
 * <pre>
 * public class Runner {
 *   public static void main(String[] args) {
 *     Shore.run(new MyConfiguration(), args);
 *   }
 * }
 * </pre>
 * 
 * @author coda
 *
 */
public final class Shore {
	private Shore() {} // nope
	
	/**
	 * Given a {@link AbstractConfiguration} implementation and a set of
	 * command-line arguments, runs the application.
	 * 
	 * @param configuration an implementation of {@link AbstractConfiguration}
	 * @param arguments a set of command line arguments
	 * @see ServerCommand
	 * @see SchemaCommand
	 * @see HelpCommand
	 */
	public static void run(AbstractConfiguration configuration, String... arguments) {
		new CommandFactory(checkNotNull(configuration)).getCommand(checkNotNull(arguments)).run();
	}
}
