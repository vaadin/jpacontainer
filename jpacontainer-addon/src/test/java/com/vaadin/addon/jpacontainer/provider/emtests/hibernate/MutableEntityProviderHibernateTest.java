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

package com.vaadin.addon.jpacontainer.provider.emtests.hibernate;

import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.emtests.AbstractMutableLocalEntityProviderEMTest;
import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Name;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.PersonSkill;
import com.vaadin.addon.jpacontainer.testdata.Skill;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.hibernate.ejb.Ejb3Configuration;

/**
 * Entity Manager test for {@link MutableLocalEntityProvider} that uses
 * Hibernate as the entity manager implementation.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class MutableEntityProviderHibernateTest extends
		AbstractMutableLocalEntityProviderEMTest {

	protected EntityManager createEntityManager() throws Exception {
		Ejb3Configuration cfg = new Ejb3Configuration().setProperty(
				"hibernate.dialect", "org.hibernate.dialect.HSQLDialect")
				.setProperty("hibernate.connection.driver_class",
						"org.hsqldb.jdbcDriver").setProperty(
						"hibernate.connection.url",
						getDatabaseUrl()).setProperty(
						"hibernate.connection.username", "sa").setProperty(
						"hibernate.connection.password", "").setProperty(
						"hibernate.connection.pool_size", "1").setProperty(
						"hibernate.connection.autocommit", "true").setProperty(
						"hibernate.cache.provider_class",
						"org.hibernate.cache.HashtableCacheProvider")
				.setProperty("hibernate.hbm2ddl.auto", "create")
				.setProperty("hibernate.show_sql", "false")
				.addAnnotatedClass(Person.class)
				.addAnnotatedClass(Address.class)
				.addAnnotatedClass(EmbeddedIdPerson.class)
				.addAnnotatedClass(Name.class)
				.addAnnotatedClass(PersonSkill.class)
				.addAnnotatedClass(Skill.class);
		EntityManagerFactory emf = cfg.buildEntityManagerFactory();
		return emf.createEntityManager();
	}
}
