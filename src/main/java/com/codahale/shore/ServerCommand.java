package com.codahale.shore;

import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;

import com.codahale.shore.modules.HibernateModule;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.wideplay.warp.persist.SessionFilter;

/**
 * Starts a new Jetty server and runs a Shore application.
 * 
 * @author coda
 *
 */
public class ServerCommand implements Runnable {
	private static final int GRACEFUL_SHUTDOWN_PERIOD = 5000; //ms
	private static final Logger LOGGER = Logger.getLogger(ServerCommand.class.getCanonicalName());
	private final AbstractConfiguration configuration;
	private final int port;
	private final Properties properties;
	
	/**
	 * Creates a new {@link ServerCommand}.
	 * 
	 * @param configuration
	 *            the application's configuration
	 * @param port
	 *            the port to listen on
	 * @param properties
	 *            the connection properties
	 */
	public ServerCommand(AbstractConfiguration configuration, int port, Properties properties) {
		this.configuration = configuration;
		this.port = port;
		this.properties = properties;
	}
	
	public AbstractConfiguration getConfiguration() {
		return configuration;
	}
	
	public int getPort() {
		return port;
	}
	
	public Properties getProperties() {
		return properties;
	}

	@Override
	public void run() {
		final Server server = new Server();
		configuration.configure();
		server.addConnector(buildConnector());
		server.addHandler(buildContext(buildServletHolder()));
		server.setSendServerVersion(false);
		server.setGracefulShutdown(GRACEFUL_SHUTDOWN_PERIOD);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Connector buildConnector() {
		final Connector connector = configuration.getConnector();
		connector.setPort(port);
		return connector;
	}

	private Context buildContext(ServletHolder servletHolder) {
		final Context root = new Context();
		root.setContextPath("/");
		root.addServlet(servletHolder, "/*");
		for (Entry<FilterHolder, String> filter : configuration.getServletFilters().entrySet()) {
			root.addFilter(filter.getKey(), filter.getValue(), 0);
		}
		root.addFilter(SessionFilter.class, "/*", 0);
		return root;
	}

	private ServletHolder buildServletHolder() {
		final ServletHolder servletHolder = new ServletHolder(new GuiceContainer(buildInjector()));
		servletHolder.setInitParameter("com.sun.jersey.config.property.packages", getResourcePackages());
		LOGGER.info("Configured resource packages: " + configuration.getResourcePackages());
		return servletHolder;
	}

	private String getResourcePackages() {
		return Joiner.on(";").join(Iterables.transform(
			configuration.getResourcePackages(),
			Functions.toStringFunction()
		));
	}

	private Injector buildInjector() {
		return Guice.createInjector(
			configuration.getStage(),
			Iterables.concat(
				configuration.getModules(),
				ImmutableList.of(buildHibernateModule())
			)
		);
	}

	private Module buildHibernateModule() {
		return new HibernateModule(LOGGER, properties, configuration.getEntityPackages());
	}
}