package com.codahale.shore;

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.servlet.Filter;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * A base class for configuring Shore applications. Subclass it to create the
 * configuration for your app:
 * <pre>
 * import com.codahale.shore.AbstractConfiguration;
 * 
 * public class MyConfiguration extends AbstractConfiguration {
 * 		@Override
 * 		protected void configure() {
 * 			addEntityPackage("com.example.myapp.entities");
 * 			addResourcePackage("com.example.myapp.resources");
 * 			addResourcePackage("com.example.myapp.providers");
 * 			addModule(new DAOModule());
 * 			setStage(Stage.PRODUCTION);
 * 			addServletFilter(GzipFilter.class, "/*");
 * `	}
 * }
 * </pre>
 * 
 * @author coda
 *
 */
public abstract class AbstractConfiguration {
	private final List<String> entityPackages = Lists.newLinkedList();
	private final List<String> resourcePackages = Lists.newLinkedList();
	private final List<Module> modules = Lists.newLinkedList();
	private final Map<FilterHolder, String> servletFilters = Maps.newLinkedHashMap();
	private Stage stage = Stage.DEVELOPMENT;
	
	/**
	 * Add a package containing Hibernate {@link Entity} classes.
	 * 
	 * @param entityPackage a package containing Hibernate entity classes
	 */
	protected void addEntityPackage(String entityPackage) {
		entityPackages.add(checkNotNull(entityPackage));
	}
	
	/**
	 * Add a Guice module.
	 * 
	 * @param module a Guice module
	 */
	protected void addModule(Module module) {
		modules.add(checkNotNull(module));
	};
	
	/**
	 * Add a package containing Jersey {@link Provider} classes or
	 * {@link Path}-annotated resource classes.
	 * 
	 * @param resourcePackage a package containing Jersey classes
	 */
	protected void addResourcePackage(String resourcePackage) {
		resourcePackages.add(checkNotNull(resourcePackage));
	}
	
	/**
	 * Add a servlet {@link Filter}.
	 * 
	 * @param filterClass the filter's class
	 * @param urlPattern the URL pattern for requests to filter
	 */
	protected void addServletFilter(Class<? extends Filter> filterClass, String urlPattern) {
		final FilterHolder holder = new FilterHolder(checkNotNull(filterClass));
		servletFilters.put(holder, checkNotNull(urlPattern));
	};
	
	/**
	 * Add a servlet {@link Filter} in a Jetty {@link FilterHolder}.
	 * 
	 * @param holder a configred filter holder
	 * @param urlPattern the URL pattern for requests to filter
	 */
	protected void addServletFilter(FilterHolder holder, String urlPattern) {
		servletFilters.put(checkNotNull(holder), checkNotNull(urlPattern));
	}
	
	/**
	 * Configures this configuration via the exposed protected methods.
	 */
	protected abstract void configure();
	
	/**
	 * Returns a new {@link Connector} to be used by Jetty to response to HTTP
	 * reqests. Override this method to return a specifically-configured
	 * {@link Connector}. By default, a stock {@link SelectChannelConnector} is
	 * returned.
	 * 
	 * @return a {@link SelectChannelConnector}
	 */
	public Connector getConnector() {
		return new SelectChannelConnector();
	}
	
	/**
	 * Returns a list of Hibernate entity packages.
	 */
	public List<String> getEntityPackages() {
		return ImmutableList.copyOf(entityPackages);
	}
	
	/**
	 * Returns a list of Guice modules.
	 */
	public List<Module> getModules() {
		return ImmutableList.copyOf(modules);
	}
	
	/**
	 * Returns a list of Jersey resource/provider packages.
	 */
	public List<String> getResourcePackages() {
		return ImmutableList.copyOf(resourcePackages);
	}
	
	/**
	 * Returns an ordered map of servlet filter holders and their URL patterns.
	 */
	public Map<FilterHolder, String> getServletFilters() {
		return ImmutableMap.copyOf(servletFilters);
	}
	
	/**
	 * Returns the Guice stage.
	 */
	public Stage getStage() {
		return stage;
	}
	
	/**
	 * Sets the {@link Stage} that Guice uses when binding classes.
	 * 
	 * @param stage Guice's stage
	 */
	protected void setStage(Stage stage) {
		this.stage = checkNotNull(stage);
	}
	
	/**
	 * Returns the application binary's names.
	 * 
	 * @return the application binary's name
	 */
	public abstract String getExecutableName();
	
	/**
	 * Configures the Jetty {@link Server} before it is started. Override this
	 * to customize the server behavior.
	 * 
	 * @param server
	 */
	protected void configureServer(Server server) {
		// no customizations needed
	}
	
	/**
	 * Configures the Jetty {@link Context} before it is started. Override this
	 * to customize the context behavior.
	 * 
	 * @param context
	 */
	protected void configureContext(Context context) {
		// no customizations needed
	}
}
