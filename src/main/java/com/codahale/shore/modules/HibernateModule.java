package com.codahale.shore.modules;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.persistence.Entity;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.sun.jersey.server.impl.container.config.AnnotatedClassScanner;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.SessionFilter;
import com.wideplay.warp.persist.UnitOfWork;

/**
 * Binds {@link SessionFilter} (via Warp Persist's plumbing) to a Hibernate
 * {@link Configuration} for the annotated Hibernate {@link Entity} classes in
 * a set of packages.
 * 
 * @author coda
 */
public class HibernateModule extends AbstractModule {
	private final AnnotationConfiguration configuration;
	private final Logger logger;
	
	/**
	 * Creates a new {@link Guice} module to configure Hibernate.
	 * 
	 * @param properties
	 */
	public HibernateModule(Logger logger, Properties properties, Collection<String> entityPackages) {
		this.logger = checkNotNull(logger);
		this.configuration = new AnnotationConfiguration();
		configureDefaultProperties(configuration);
		configuration.addProperties(checkNotNull(properties));
		configureRequiredProperties(configuration);
		addAnnotatedEntities(configuration, checkNotNull(entityPackages));
	}

	private void addAnnotatedEntities(AnnotationConfiguration configuration,
			Iterable<String> entityPackages) {
		checkNotNull(configuration);
		final AnnotatedClassScanner scanner = new AnnotatedClassScanner(Entity.class);
		final List<String> entityClasses = Lists.newLinkedList();
		for (String entityPackage : checkNotNull(entityPackages)) {
			logger.info("Scanning " + entityPackage + " for entity classes");
			for (Class<?> entityClass : scanner.scan(new String[] { entityPackage })) {
				configuration.addAnnotatedClass(entityClass);
				entityClasses.add(entityClass.getCanonicalName());
			}
		}
		Collections.sort(entityClasses);
		logger.info("Configured entities: " + entityClasses);
	}
	
	private void configureRequiredProperties(Configuration configuration2) {
		// Required for WarpPersist's SessionFilter to work.
		configuration.setProperty("hibernate.current_session_context_class", "managed");
	}

	private void configureDefaultProperties(Configuration configuration) {
		// Handy for actually seeing what's going on.
		configuration.setProperty("hibernate.format_sql", "true");
		configuration.setProperty("hibernate.use_sql_comments", "true");
		
		// Required for performance logging.
		configuration.setProperty("hibernate.generate_statistics", "true");
		
		// C3P0's the best connection pool, and this is how to keep most
		// database connections from timing out after long periods of inactivity.
		configuration.setProperty("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
		configuration.setProperty("hibernate.c3p0.idle_test_period", "3600");
		configuration.setProperty("hibernate.c3p0.timeout", "10800");
		configuration.setProperty("hibernate.c3p0.preferredTestQuery", "SELECT 1;");
		configuration.setProperty("hibernate.c3p0.maxConnectionAge", "14400");
	}

	@Override
	protected void configure() {
		bind(Configuration.class).toInstance(configuration);
		install(
			PersistenceService
				.usingHibernate()
				.across(UnitOfWork.REQUEST)
				.buildModule()
		);
		bind(HibernateInitializer.class).asEagerSingleton();
	}
	
	public AnnotationConfiguration getConfiguration() {
		return configuration;
	}
}
