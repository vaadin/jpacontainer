/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.provider.emtests.eclipselink;

import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.emtests.AbstractCachingMutableLocalEntityProviderEMTest;
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
 * Entity Manager test for {@link CachingMutableLocalEntityProvider} that uses
 * EclipseLink as the entity manager implementation.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class CachingMutableEntityProviderEclipseLinkTest extends AbstractCachingMutableLocalEntityProviderEMTest {

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
