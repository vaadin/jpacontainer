/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider.emtests;

import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.Person;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Abstract test case for {@link MutableEntityProvider} that should work with
 * any entity manager that follows the specifications. Subclasses should provide
 * a concrete entity manager implementation to test. If the test passes, the
 * entity manager implementation should work with JPAContainer.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractMutableEntityProviderEMTest extends
		AbstractEntityProviderEMTest {

	@Test
	public void testAddEntity() {
		Person p = new Person();
		p.setFirstName("Hello");
		p.setLastName("World");
		p.setDateOfBirth(java.sql.Date.valueOf("2000-06-02"));
		p.setAddress(new Address());
		p.getAddress().setStreet("Street");
		p.getAddress().setPostalCode("Postal Code");
		p.getAddress().setPostOffice("Post Office");

		Person returned = ((MutableEntityProvider<Person>) entityProvider)
				.addEntity(p);
		assertEquals(returned, p);
		assertTrue(entityProvider.containsEntity(returned.getId(), null));
		assertEquals(returned, entityProvider.getEntity(returned.getId()));

		// Make some changes to the local instance
		returned.setFirstName("Another firstname");

		// Now, the instances should not be equal any longer
		assertFalse(returned.equals(entityProvider.getEntity(returned.getId())));
	}

	@Test
	public void testRemoveEntity() {
		Person p = testDataSortedByName.get(0);

		assertTrue(entityProvider.containsEntity(p.getId(), null));
		((MutableEntityProvider<Person>) entityProvider)
				.removeEntity(p.getId());
		assertFalse(entityProvider.containsEntity(p.getId(), null));
	}

	@Test
	public void testUpdateEntity() {
		Person p = testDataSortedByName.get(0);

		p = entityProvider.getEntity(p.getId());
		p.setFirstName("A changed first name");
		assertFalse(p.equals(entityProvider.getEntity(p.getId())));

		Person returned = ((MutableEntityProvider<Person>) entityProvider)
				.updateEntity(p);

		assertEquals(returned, p);
		assertEquals(p, entityProvider.getEntity(p.getId()));
	}

	@Test
	public void testUpdateEntityProperty() {
		Person p = testDataSortedByName.get(0);

		((MutableEntityProvider<Person>) entityProvider).updateEntityProperty(p
				.getId(), "firstName", "A changed first name again");

		Person returned = entityProvider.getEntity(p.getId());
		assertEquals("A changed first name again", returned.getFirstName());
	}
}
