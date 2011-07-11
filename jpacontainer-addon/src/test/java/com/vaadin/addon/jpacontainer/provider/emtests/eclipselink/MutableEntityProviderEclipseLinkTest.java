/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider.emtests.eclipselink;

import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.emtests.AbstractMutableLocalEntityProviderEMTest;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;
import junit.framework.Assert;
import org.eclipse.persistence.config.TargetDatabase;
import org.eclipse.persistence.config.TargetServer;
import org.eclipse.persistence.jpa.PersistenceProvider;
import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

/**
 * Entity Manager test for {@link MutableLocalEntityProvider} that uses
 * EclipseLink as the entity manager implementation.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class MutableEntityProviderEclipseLinkTest extends AbstractMutableLocalEntityProviderEMTest {

	protected EntityManager createEntityManager() throws Exception {
		Map properties = new HashMap();

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
		properties.put(DDL_GENERATION, DROP_AND_CREATE);

//		properties.put(LOGGING_LEVEL, "FINE");

		PersistenceProvider pp = new PersistenceProvider();
		EntityManagerFactory emf = pp.createEntityManagerFactory("eclipselink-pu", properties);
		Assert.assertNotNull("EntityManagerFactory should not be null", emf);
		return emf.createEntityManager();
	}
}
