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

package com.vaadin.addon.jpacontainer.provider.emtests;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Person;

/**
 * Base class for the {@link CachingMutableLocalEntityProvider} Entity Manager tests.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class AbstractCachingMutableLocalEntityProviderEMTest extends
		AbstractMutableLocalEntityProviderEMTest {

	@Override
	protected EntityProvider<Person> createEntityProvider() throws Exception {
		CachingMutableLocalEntityProvider<Person> provider = new CachingMutableLocalEntityProvider<Person>(
				Person.class, getEntityManager());
		provider.setCacheEnabled(true);
		provider.setCloneCachedEntities(true);
		provider.setEntityCacheMaxSize(400);
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}

	@Override
	protected EntityProvider<EmbeddedIdPerson> createEntityProvider_EmbeddedId() throws Exception {
		CachingMutableLocalEntityProvider<EmbeddedIdPerson> provider = new CachingMutableLocalEntityProvider<EmbeddedIdPerson>(
				EmbeddedIdPerson.class, getEntityManager());
		provider.setCacheEnabled(true);
		provider.setCloneCachedEntities(true);
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}

	// TODO Add some test cases that try out the caching features as well
}
