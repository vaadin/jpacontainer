/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider.emtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.DataGenerator;
import com.vaadin.addon.jpacontainer.testdata.Person;

/**
 * Abstract test case for {@link MutableEntityProvider} that should work with
 * any entity manager that follows the specifications. Subclasses should provide
 * a concrete entity manager implementation to test. If the test passes, the
 * entity manager implementation should work with JPAContainer.
 * 
 * @author Petter Holmström (Vaadin Ltd)
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

		int entityCount = entityProvider.getEntityCount(container, null);

		Person returned = ((MutableEntityProvider<Person>) entityProvider)
				.addEntity(p);
		assertEquals(p.getFirstName(), returned.getFirstName());
		assertEquals(p.getLastName(), returned.getLastName());
		assertEquals(p.getDateOfBirth(), returned.getDateOfBirth());
		assertEquals(p.getAddress().getStreet(), returned.getAddress().getStreet());
		assertEquals(p.getAddress().getPostalCode(), returned.getAddress().getPostalCode());
		assertEquals(p.getAddress().getPostOffice(), returned.getAddress().getPostOffice());
		// Note, we don't test skills as it may be lazy loaded and entities a detached
		
		assertTrue(entityProvider.containsEntity(container, returned.getId(), null));
		
		returned = entityProvider.getEntity(container, returned.getId());
		assertEquals(p.getFirstName(), returned.getFirstName());
		assertEquals(p.getLastName(), returned.getLastName());
		assertEquals(p.getDateOfBirth(), returned.getDateOfBirth());
		assertEquals(p.getAddress().getStreet(), returned.getAddress().getStreet());
		assertEquals(p.getAddress().getPostalCode(), returned.getAddress().getPostalCode());
		assertEquals(p.getAddress().getPostOffice(), returned.getAddress().getPostOffice());
		assertTrue(entityProvider.containsEntity(container, returned.getId(), null));
		
		assertEquals(entityCount + 1, entityProvider.getEntityCount(container, null));

		// Make some changes to the local instance
		returned.setFirstName("Another firstname");

		Person fromProvider = entityProvider.getEntity(container, returned.getId());
		
		// Now, the instances should not be equal any longer
		assertFalse(returned.getFirstName().equals(fromProvider.getFirstName()));
	}

	@Test
	public void testRemoveEntity() {
		Person p = DataGenerator.getTestDataSortedByName().get(0);

		int entityCount = entityProvider.getEntityCount(container, null);

		assertTrue(entityProvider.containsEntity(container, p.getId(), null));
		((MutableEntityProvider<Person>) entityProvider)
				.removeEntity(p.getId());

		assertEquals(entityCount - 1, entityProvider.getEntityCount(container, null));

		assertFalse(entityProvider.containsEntity(container, p.getId(), null));
	}

	@Test
	public void testUpdateEntity() {
		Person p = DataGenerator.getTestDataSortedByName().get(0);

		p = entityProvider.getEntity(null, p.getId());
		p.setFirstName("A changed first name");
		assertFalse(p.getFirstName().equals(entityProvider.getEntity(container, p.getId()).getFirstName()));

		Person returned = ((MutableEntityProvider<Person>) entityProvider)
				.updateEntity(p);

		assertEquals(returned.getFirstName(), p.getFirstName());
		assertEquals(p.getFirstName(), entityProvider.getEntity(container, p.getId()).getFirstName());
	}

	@Test
	public void testUpdateEntityProperty() {
		Person p = DataGenerator.getTestDataSortedByName().get(0);

		((MutableEntityProvider<Person>) entityProvider).updateEntityProperty(
				p.getId(), "firstName", "A changed first name again");

		Person returned = entityProvider.getEntity(container, p.getId());
		assertEquals("A changed first name again", returned.getFirstName());
	}
}
