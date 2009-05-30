package com.codahale.shore;

import static com.google.common.base.Preconditions.*;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Settings;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import com.codahale.shore.modules.HibernateModule;


/**
 * Outputs a schema or migration SQL script for a Shore application.
 * 
 * @author coda
 *
 */
public class SchemaCommand implements Runnable {
	private final AbstractConfiguration configuration;
	private final Properties properties;
	private final boolean migration;
	private final OutputStream outputStream;
	
	/**
	 * Creates a new {@link SchemaCommand}.
	 * 
	 * @param configuration
	 *            the application's configuration
	 * @param properties
	 *            the connection properties
	 * @param migration
	 *            if {@code true}, generate a migration script
	 * @param outputStream
	 *            the stream to write the script to
	 */
	public SchemaCommand(AbstractConfiguration configuration, Properties properties, boolean migration, OutputStream outputStream) {
		this.configuration = checkNotNull(configuration);
		this.properties = checkNotNull(properties);
		this.migration = migration;
		this.outputStream = checkNotNull(outputStream);
	}
	
	public AbstractConfiguration getConfiguration() {
		return configuration;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public boolean isMigration() {
		return migration;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	@Override
	public void run() {
		Logger.getLogger("org.hibernate").setLevel(Level.OFF);
		Logger.getLogger("com.mchange").setLevel(Level.OFF);
		configuration.configure();
		
		final PrintWriter writer = new PrintWriter(outputStream);
		
		try {
			final Logger silentLogger = Logger.getLogger(SchemaCommand.class.getCanonicalName());
			silentLogger.setLevel(Level.OFF);
			
			final HibernateModule module = new HibernateModule(silentLogger, properties, configuration.getEntityPackages());
			final AnnotationConfiguration hibernateConfig = module.getConfiguration();
			final Settings settings = hibernateConfig.buildSettings();
			
			
			
			if (migration) {
				try {
					final Connection connection = settings.getConnectionProvider().getConnection();
					try {
						final DatabaseMetadata metadata = new DatabaseMetadata(connection, settings.getDialect());
						
						writer.println("/* migration script */");
						printSQL(writer, hibernateConfig.generateSchemaUpdateScript(settings.getDialect(), metadata));
					} finally {
						connection.close();
					}
				} catch (SQLException e) {
					throw new HibernateException(e);
				}
			} else {
				writer.println("/* full drop-and-create script */");
				printSQL(writer, hibernateConfig.generateDropSchemaScript(settings.getDialect()));
				printSQL(writer, hibernateConfig.generateSchemaCreationScript(settings.getDialect()));
			}
		} catch (HibernateException e) {
			writer.println("Error: unable to connect to the database.\n");
			e.printStackTrace(writer);
		}
		
		writer.flush();
	}

	private void printSQL(PrintWriter writer, String[] sqlCmds) {
		for (String cmd : sqlCmds) {
			writer.print(cmd);
			writer.println(";");
		}
	}

}
