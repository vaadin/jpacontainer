/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider.emtests;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Person;

/**
 * Base class for the {@link MutableLocalEntityProvider} Entity Manager tests.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class AbstractMutableLocalEntityProviderEMTest extends
		AbstractMutableEntityProviderEMTest {

	@Override
	protected EntityProvider<Person> createEntityProvider() throws Exception {
		MutableLocalEntityProvider<Person> provider = new MutableLocalEntityProvider<Person>(
				Person.class, getEntityManager());
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}

	@Override
	protected EntityProvider<EmbeddedIdPerson> createEntityProvider_EmbeddedId() throws Exception {
		MutableLocalEntityProvider<EmbeddedIdPerson> provider = new MutableLocalEntityProvider<EmbeddedIdPerson>(
				EmbeddedIdPerson.class, getEntityManager());
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}
	
}
