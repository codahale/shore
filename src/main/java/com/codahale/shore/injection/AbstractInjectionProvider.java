package com.codahale.shore.injection;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;

import net.jcip.annotations.Immutable;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

/**
 * An abstract base class for injection provider classes.
 * 
 * @author coda
 *
 * @param <E> the type of object injected
 */
@Immutable
public abstract class AbstractInjectionProvider<E>
		extends AbstractHttpContextInjectable<E>
		implements InjectableProvider<Context, Type> {

	private final Type t;
	
	/**
	 * Create a new injection provider which injects a type, {@code t}.
	 * 
	 * @param t the type of objects this provider injects
	 */
	public AbstractInjectionProvider(Type t) {
		this.t = t;
	}

	@Override
	public Injectable<E> getInjectable(ComponentContext ic, Context a, Type c) {
		if (c.equals(t)) {
			return getInjectable(ic, a);
		}

		return null;
	}
	
	protected Injectable<E> getInjectable(ComponentContext ic, Context a) {
		return this;
	}
	
	/**
	 * Returns the type of objects this provider injects.
	 * 
	 * @return the type of objects this provider injects.
	 */
	public Type getInjectedType() {
		return t;
	}

	@Override
	public ComponentScope getScope() {
		return ComponentScope.PerRequest;
	}
}