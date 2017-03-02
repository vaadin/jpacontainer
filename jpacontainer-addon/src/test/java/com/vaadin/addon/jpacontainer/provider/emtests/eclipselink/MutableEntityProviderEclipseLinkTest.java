/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vaadin.addon.jpacontainer.provider.emtests.eclipselink;

import static org.eclipse.persistence.config.PersistenceUnitProperties.CREATE_ONLY;
import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_READ_CONNECTIONS_MIN;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_WRITE_CONNECTIONS_MIN;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_DATABASE;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;

import junit.framework.Assert;

import org.eclipse.persistence.config.TargetDatabase;
import org.eclipse.persistence.config.TargetServer;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.emtests.AbstractMutableLocalEntityProviderEMTest;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.Skill;
import com.vaadin.addon.jpacontainer.testdata.DataGenerator;
import com.vaadin.v7.data.util.filter.Compare.Equal;

/**
 * Entity Manager test for {@link MutableLocalEntityProvider} that uses
 * EclipseLink as the entity manager implementation.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class MutableEntityProviderEclipseLinkTest extends
        AbstractMutableLocalEntityProviderEMTest {

    @Override
    protected EntityManager createEntityManager() throws Exception {
        Map<String, String> properties = new HashMap<String, String>();

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

        // properties.put(LOGGING_LEVEL, "FINE");

        PersistenceProvider pp = new PersistenceProvider();
        EntityManagerFactory emf = pp.createEntityManagerFactory(
                "eclipselink-pu", properties);
        Assert.assertNotNull("EntityManagerFactory should not be null", emf);
        return emf.createEntityManager();
    }

    @Test
    public void testImplicitJoin() throws Exception {
        // Save some testing data
        Random rnd = new Random();
        Map<Skill, Collection<Object>> skillPersonMap = new HashMap<Skill, Collection<Object>>();
        getEntityManager().getTransaction().begin();
        for (Skill s : DataGenerator.getSkills()) {
            Set<Object> persons = new HashSet<Object>();
            for (int i = 0; i < 10; i++) {
                Person p = DataGenerator.getTestDataSortedByPrimaryKey()
                        .get(rnd.nextInt(DataGenerator
                                .getTestDataSortedByPrimaryKey().size()));
                System.out.println("Skill: " + s + " Person: " + p);
                if (!persons.contains(p.getId())) {
                    persons.add(p.getId());
                    p.addSkill(s, i + 1);
                    getEntityManager().merge(p);
                }
            }
            skillPersonMap.put(s, persons);
        }
        getEntityManager().flush();
        getEntityManager().getTransaction().commit();

        // Now try out the filter
        for (final Skill s : DataGenerator.getSkills()) {
            Collection<Object> returnedIds = entityProvider
                    .getAllEntityIdentifiers(container, new Equal("skills.skill", s), null);
            assertTrue(skillPersonMap.get(s).containsAll(returnedIds));
            assertEquals(skillPersonMap.get(s).size(), returnedIds.size());
        }

        entityProvider.setQueryModifierDelegate(null);
    }

}
