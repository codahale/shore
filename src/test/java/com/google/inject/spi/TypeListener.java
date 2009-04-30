package com.google.inject.spi;

import com.google.inject.TypeLiteral;

public interface TypeListener {

	  /**
	   * Invoked when Guice encounters a new type eligible for constructor or members injection.
	   * Called during injector creation (or afterwords if Guice encounters a type at run time and
	   * creates a JIT binding).
	   *
	   * @param type encountered by Guice
	   * @param encounter context of this encounter, enables reporting errors, registering injection
	   *     listeners and binding method interceptors for {@code type}.
	   *
	   * @param <I> the injectable type
	   */
	  <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter);

	}
