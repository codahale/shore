package com.google.inject.spi;

import static com.google.common.base.Preconditions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.*;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.AnnotatedElementBuilder;
import com.google.inject.internal.AbstractBindingBuilder;
import com.google.inject.internal.BindingBuilder;
import com.google.inject.internal.ConstantBindingBuilderImpl;
import com.google.inject.internal.Errors;
import com.google.inject.internal.PrivateElementsImpl;
import com.google.inject.internal.ProviderMethodsModule;
import com.google.inject.internal.SourceProvider;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectableType.Listener;

public class RecordingBinder implements Binder, PrivateBinder {
	private final Stage stage;
	private final Set<Module> modules;
	private final List<Element> elements;
	private final Object source;
	private final SourceProvider sourceProvider;

	/** The binder where exposed bindings will be created */
	private final RecordingBinder parent;
	private final PrivateElementsImpl privateElements;

	public RecordingBinder(Stage stage) {
		this.stage = stage;
		this.modules = Sets.newHashSet();
		this.elements = Lists.newArrayList();
		this.source = null;
		this.sourceProvider = new SourceProvider().plusSkippedClasses(Elements.class,
				RecordingBinder.class, AbstractModule.class, ConstantBindingBuilderImpl.class,
				AbstractBindingBuilder.class, BindingBuilder.class);
		this.parent = null;
		this.privateElements = null;
	}

	/** Creates a recording binder that's backed by {@code prototype}. */
	private RecordingBinder(RecordingBinder prototype, Object source, SourceProvider sourceProvider) {
		checkArgument(source == null ^ sourceProvider == null);

		this.stage = prototype.stage;
		this.modules = prototype.modules;
		this.elements = prototype.elements;
		this.source = source;
		this.sourceProvider = sourceProvider;
		this.parent = prototype.parent;
		this.privateElements = prototype.privateElements;
	}

	/** Creates a private recording binder. */
	private RecordingBinder(RecordingBinder parent, PrivateElementsImpl privateElements) {
		this.stage = parent.stage;
		this.modules = Sets.newHashSet();
		this.elements = privateElements.getElementsMutable();
		this.source = parent.source;
		this.sourceProvider = parent.sourceProvider;
		this.parent = parent;
		this.privateElements = privateElements;
	}

	/* if[AOP] */
	public void bindInterceptor(Matcher<? super Class<?>> classMatcher,
			Matcher<? super Method> methodMatcher,
			org.aopalliance.intercept.MethodInterceptor... interceptors) {
		elements
				.add(new InterceptorBinding(getSource(), classMatcher, methodMatcher, interceptors));
	}

	/* end[AOP] */

	public void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
		elements.add(new ScopeBinding(getSource(), annotationType, scope));
	}

	@SuppressWarnings("unchecked")
	// it is safe to use the type literal for the raw type
	public void requestInjection(Object instance) {
		requestInjection((TypeLiteral) TypeLiteral.get(instance.getClass()), instance);
	}

	public <T> void requestInjection(TypeLiteral<T> type, T instance) {
		elements.add(new InjectionRequest<T>(getSource(), type, instance));
	}

	public <T> MembersInjector<T> getMembersInjector(final TypeLiteral<T> typeLiteral) {
		final MembersInjectorLookup<T> element = new MembersInjectorLookup<T>(getSource(),
				typeLiteral);
		elements.add(element);
		return element.getMembersInjector();
	}

	public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
		return getMembersInjector(TypeLiteral.get(type));
	}

	public void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher, TypeListener listener) {
		elements.add(new TypeListenerBinding(getSource(), listener, typeMatcher));
	}

	public void requestStaticInjection(Class<?>... types) {
		for (Class<?> type : types) {
			elements.add(new StaticInjectionRequest(getSource(), type));
		}
	}

	public void install(Module module) {
		if (modules.add(module)) {
			Binder binder = this;
			if (module instanceof PrivateModule) {
				binder = binder.newPrivateBinder();
			}

			try {
				module.configure(binder);
			} catch (RuntimeException e) {
				Collection<Message> messages = Errors.getMessagesFromThrowable(e);
				if (!messages.isEmpty()) {
					elements.addAll(messages);
				} else {
					addError(e);
				}
			}
			binder.install(ProviderMethodsModule.forModule(module));
		}
	}

	public Stage currentStage() {
		return stage;
	}

	public void addError(String message, Object... arguments) {
		elements.add(new Message(getSource(), Errors.format(message, arguments)));
	}

	public void addError(Throwable t) {
		String message = "An exception was caught and reported. Message: " + t.getMessage();
		elements.add(new Message(ImmutableList.of(getSource()), message, t));
	}

	public void addError(Message message) {
		elements.add(message);
	}

	public <T> AnnotatedBindingBuilder<T> bind(Key<T> key) {
		return new BindingBuilder<T>(this, elements, getSource(), key);
	}

	public <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
		return bind(Key.get(typeLiteral));
	}

	public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
		return bind(Key.get(type));
	}

	public AnnotatedConstantBindingBuilder bindConstant() {
		return new ConstantBindingBuilderImpl<Void>(this, elements, getSource());
	}

	public <T> Provider<T> getProvider(final Key<T> key) {
		final ProviderLookup<T> element = new ProviderLookup<T>(getSource(), key);
		elements.add(element);
		return element.getProvider();
	}

	public <T> Provider<T> getProvider(Class<T> type) {
		return getProvider(Key.get(type));
	}

	public void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
		elements.add(new TypeConverterBinding(getSource(), typeMatcher, converter));
	}

	public RecordingBinder withSource(final Object source) {
		return new RecordingBinder(this, source, null);
	}

	@SuppressWarnings("unchecked")
	public RecordingBinder skipSources(Class... classesToSkip) {
		// if a source is specified explicitly, we don't need to skip sources
		if (source != null) {
			return this;
		}

		SourceProvider newSourceProvider = sourceProvider.plusSkippedClasses(classesToSkip);
		return new RecordingBinder(this, null, newSourceProvider);
	}

	public PrivateBinder newPrivateBinder() {
		PrivateElementsImpl privateElements = new PrivateElementsImpl(getSource());
		elements.add(privateElements);
		return new RecordingBinder(this, privateElements);
	}

	public void expose(Key<?> key) {
		exposeInternal(key);
	}

	public AnnotatedElementBuilder expose(Class<?> type) {
		return exposeInternal(Key.get(type));
	}

	public AnnotatedElementBuilder expose(TypeLiteral<?> type) {
		return exposeInternal(Key.get(type));
	}

	private <T> AnnotatedElementBuilder exposeInternal(Key<T> key) {
		if (privateElements == null) {
			addError("Cannot expose %s on a standard binder. "
					+ "Exposed bindings are only applicable to private binders.", key);
			return new AnnotatedElementBuilder() {
				public void annotatedWith(Class<? extends Annotation> annotationType) {
				}

				public void annotatedWith(Annotation annotation) {
				}
			};
		}

		BindingBuilder<T> exposeBinding = new BindingBuilder<T>(this, parent.elements, getSource(),
				key);

		BindingBuilder.ExposureBuilder<T> builder = exposeBinding.usingKeyFrom(privateElements);
		privateElements.addExposureBuilder(builder);
		return builder;
	}

	protected Object getSource() {
		return sourceProvider != null ? sourceProvider.get() : source;
	}

	@Override
	public String toString() {
		return "Binder";
	}

	@Override
	public void bindListener(Matcher<? super TypeLiteral<?>> arg0, Listener arg1) {
		// TODO Auto-generated method stub
		
	}

	public List<Element> getElements() {
		return elements;
	}
}
