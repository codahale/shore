package com.codahale.shore;

public abstract class Shore {
	private Shore() {} // nope
	
	public static void run(AbstractConfiguration configuration, String... arguments)
			throws Exception {
		new CommandFactory(configuration).getCommand(arguments).run();
	}
}
