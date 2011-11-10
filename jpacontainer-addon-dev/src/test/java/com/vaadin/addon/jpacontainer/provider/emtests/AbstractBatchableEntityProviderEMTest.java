/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider.emtests;

import com.vaadin.addon.jpacontainer.BatchableEntityProvider;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.TestDataGenerator;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Abstract test case for {@link BatchableEntityProvider} that should work with
 * any entity manager that follows the specifications. Subclasses should provide
 * a concrete entity manager implementation to test. If the test passes, the
 * entity manager implementation should work with JPAContainer.
 * <p>
 * Note, that the test data used will not contain circular references that might
 * cause problems when committing buffered changes.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class AbstractBatchableEntityProviderEMTest extends
		AbstractMutableEntityProviderEMTest {

	@Test
	public void testBatchUpdate() {

		/*
		 * This is a very simple test, but better than nothing.
		 */

		final Person addedPerson = new Person();
		addedPerson.setFirstName("Hello");
		addedPerson.setLastName("World");
		addedPerson.setDateOfBirth(java.sql.Date.valueOf("2000-06-02"));
		addedPerson.setAddress(new Address());
		addedPerson.getAddress().setStreet("Street");
		addedPerson.getAddress().setPostalCode("Postal Code");
		addedPerson.getAddress().setPostOffice("Post Office");
		
		final Person updatedPerson = TestDataGenerator.getTestDataSortedByName().get(1).clone();
		updatedPerson.setFirstName("Another changed first name");
		addedPerson.setManager(updatedPerson);


		final Person removedPerson = TestDataGenerator.getTestDataSortedByName().get(2).clone();
		final Object[] addedPersonId = new Object[1];
		
		entityProvider.setEntitiesDetached(false);

		BatchableEntityProvider.BatchUpdateCallback<Person> callback = new BatchableEntityProvider.BatchUpdateCallback<Person>() {

			public void batchUpdate(MutableEntityProvider<Person> batchEnabledEntityProvider) {
				addedPersonId[0] = batchEnabledEntityProvider.addEntity(addedPerson).getId();
				batchEnabledEntityProvider.updateEntity(updatedPerson);
				batchEnabledEntityProvider.removeEntity(removedPerson.getId());
			}
			
		};
		((BatchableEntityProvider<Person>) entityProvider).batchUpdate(callback);

		assertEquals(updatedPerson, entityProvider.getEntity(updatedPerson.getId()));
		assertFalse(entityProvider.containsEntity(removedPerson.getId(), null));
		assertEquals(addedPerson, entityProvider.getEntity(addedPersonId[0]));
		assertEquals(addedPerson.getManager(), updatedPerson);
	}
}
