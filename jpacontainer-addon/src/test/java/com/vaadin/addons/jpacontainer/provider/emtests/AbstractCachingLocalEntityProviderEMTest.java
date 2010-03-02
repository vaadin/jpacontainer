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
package com.vaadin.addons.jpacontainer.provider.emtests;

import com.vaadin.addons.jpacontainer.EntityProvider;
import com.vaadin.addons.jpacontainer.provider.CachingLocalEntityProvider;
import com.vaadin.addons.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addons.jpacontainer.testdata.Person;

/**
 * Base class for the {@link CachingLocalEntityProvider} Entity Manager tests.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractCachingLocalEntityProviderEMTest extends
		AbstractEntityProviderEMTest {

	@Override
	protected EntityProvider<Person> createEntityProvider() throws Exception {
		CachingLocalEntityProvider<Person> provider = new CachingLocalEntityProvider<Person>(
				Person.class, getEntityManager());
		provider.setCacheInUse(true);
		provider.setCloneCachedEntities(true);
		provider.setMaxCacheSize(400);
		return provider;
	}

	@Override
	protected EntityProvider<EmbeddedIdPerson> createEntityProvider_EmbeddedId() throws Exception {
		CachingLocalEntityProvider<EmbeddedIdPerson> provider = new CachingLocalEntityProvider<EmbeddedIdPerson>(
				EmbeddedIdPerson.class, getEntityManager());
		provider.setCacheInUse(true);
		provider.setCloneCachedEntities(true);
		return provider;
	}

	// TODO Add some test cases that try out the caching features as well
}
