/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider.emtests;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Person;

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
		provider.setEntityCacheMaxSize(400);
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
