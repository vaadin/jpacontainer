/*
${license.header.text}
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
		provider.setCacheInUse(true);
		provider.setCloneCachedEntities(true);
		provider.setEntityCacheMaxSize(400);
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}

	@Override
	protected EntityProvider<EmbeddedIdPerson> createEntityProvider_EmbeddedId() throws Exception {
		CachingMutableLocalEntityProvider<EmbeddedIdPerson> provider = new CachingMutableLocalEntityProvider<EmbeddedIdPerson>(
				EmbeddedIdPerson.class, getEntityManager());
		provider.setCacheInUse(true);
		provider.setCloneCachedEntities(true);
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}

	// TODO Add some test cases that try out the caching features as well
}
