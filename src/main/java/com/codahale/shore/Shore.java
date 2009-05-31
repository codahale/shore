package com.codahale.shore;

import static com.google.common.base.Preconditions.*;

public final class Shore {
	private Shore() {} // nope
	
	public static void run(AbstractConfiguration configuration, String... arguments)
			throws Exception {
		new CommandFactory(checkNotNull(configuration)).getCommand(checkNotNull(arguments)).run();
	}
}
