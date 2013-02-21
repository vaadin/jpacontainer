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
import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Person;

/**
 * Base class for the {@link BatchableLocalEntityProvider} Entity Manager tests.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class AbstractBatchableLocalEntityProviderEMTest extends AbstractBatchableEntityProviderEMTest {

	@Override
	protected EntityProvider<Person> createEntityProvider() throws Exception {
		BatchableLocalEntityProvider<Person> provider = new BatchableLocalEntityProvider<Person>(
				Person.class, getEntityManager());
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}

	@Override
	protected EntityProvider<EmbeddedIdPerson> createEntityProvider_EmbeddedId() throws Exception {
		BatchableLocalEntityProvider<EmbeddedIdPerson> provider = new BatchableLocalEntityProvider<EmbeddedIdPerson>(
				EmbeddedIdPerson.class, getEntityManager());
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}
	
}
