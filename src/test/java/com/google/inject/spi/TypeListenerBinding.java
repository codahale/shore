package com.google.inject.spi;

import java.util.List;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.InjectableType.Listener;

public final class TypeListenerBinding implements Element, PrivateElements {

	  private final Object source;
	  private final Matcher<? super TypeLiteral<?>> typeMatcher;
	  private final TypeListener listener;

	  TypeListenerBinding(Object source, TypeListener listener,
	      Matcher<? super TypeLiteral<?>> typeMatcher) {
	    this.source = source;
	    this.listener = listener;
	    this.typeMatcher = typeMatcher;
	  }

	  /** Returns the registered listener. */
	  public TypeListener getListener() {
	    return listener;
	  }

	  /** Returns the type matcher which chooses which types the listener should be notified of. */
	  public Matcher<? super TypeLiteral<?>> getTypeMatcher() {
	    return typeMatcher;
	  }

	  public Object getSource() {
	    return source;
	  }

	  public <T> T acceptVisitor(ElementVisitor<T> visitor) {
	    return visitor.visit(this);
	  }

	  public void applyTo(Binder binder) {
	    binder.withSource(getSource()).bindListener(typeMatcher, (Listener) listener);
	  }

	@Override
	public List<Element> getElements() {
		return null;
	}

	@Override
	public Set<Key<?>> getExposedKeys() {
		return null;
	}

	@Override
	public Injector getInjector() {
		return null;
	}
	}
