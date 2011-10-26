/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider.emtests.eclipselink;

import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.DROP_AND_CREATE;
import static org.eclipse.persistence.config.PersistenceUnitProperties.CREATE_ONLY;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_READ_CONNECTIONS_MIN;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_WRITE_CONNECTIONS_MIN;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_DATABASE;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;

import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.eclipse.persistence.config.TargetDatabase;
import org.eclipse.persistence.config.TargetServer;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.junit.Assert;

import com.vaadin.addon.jpacontainer.provider.LocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.emtests.AbstractLocalEntityProviderEMTest;

/**
 * Entity Manager test for {@link LocalEntityProvider} that uses EclipseLink as
 * the entity manager implementation.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class LocalEntityProviderEclipseLinkTest extends AbstractLocalEntityProviderEMTest {

	protected EntityManager createEntityManager() throws Exception {
		HashMap<String, String> properties = new HashMap<String, String>();

		properties.put(TRANSACTION_TYPE,
				PersistenceUnitTransactionType.RESOURCE_LOCAL.name());

		properties.put(JDBC_DRIVER, "org.hsqldb.jdbcDriver");
		properties.put(JDBC_URL, getDatabaseUrl());
		properties.put(JDBC_USER, "sa");
		properties.put(JDBC_PASSWORD, "");
		properties.put(JDBC_READ_CONNECTIONS_MIN, "1");
		properties.put(JDBC_WRITE_CONNECTIONS_MIN, "1");
		properties.put(TARGET_DATABASE, TargetDatabase.HSQL);
		properties.put(TARGET_SERVER, TargetServer.None);
		properties.put(DDL_GENERATION, CREATE_ONLY);

//		properties.put(LOGGING_LEVEL, "FINE");

		PersistenceProvider pp = new PersistenceProvider();
		EntityManagerFactory emf = pp.createEntityManagerFactory("eclipselink-pu", properties);
		Assert.assertNotNull("EntityManagerFactory should not be null", emf);
		return emf.createEntityManager();
	}
}
