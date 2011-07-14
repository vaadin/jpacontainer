/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider.emtests;

import com.vaadin.addon.jpacontainer.testdata.Address;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.SortBy;
import com.vaadin.addon.jpacontainer.Filter;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Name;
import com.vaadin.addon.jpacontainer.testdata.Skill;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 * Abstract test case for {@link EntityProvider} that should work with any
 * entity manager that follows the specifications. Subclasses should provide a
 * concrete entity manager implementation to test. If the test passes, the
 * entity manager implementation should work with JPAContainer.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class AbstractEntityProviderEMTest {

    protected static String getDatabaseFileName() throws IOException {
        File f = File.createTempFile("jpacontainer_integration_test", "");
        return f.getAbsolutePath();
    }

    protected static String getDatabaseUrl() throws IOException {
        return "jdbc:hsqldb:file:" + getDatabaseFileName();
    }

    protected abstract EntityManager createEntityManager() throws Exception;

    private static EntityManager entityManager;

	protected EntityManager getEntityManager() throws Exception {
        /*if (entityManager == null) {
            entityManager = createEntityManager();
        }*/
        return entityManager;
    }
	
	protected EntityProvider<Person> entityProvider;
	protected EntityProvider<EmbeddedIdPerson> entityProvider_EmbeddedId;
	protected static List<Person> testDataSortedByPrimaryKey;
	protected static List<Person> testDataSortedByName;
	protected static List<EmbeddedIdPerson> testDataEmbeddedIdSortedByName;
	protected static List<Person> testDataSortedByLastNameAndStreet;
	protected static List<Person> filteredTestDataSortedByPrimaryKey;
	protected static List<Person> filteredTestDataSortedByName;
	protected static Filter testFilter;
	protected static List<SortBy> sortByName;
	protected static List<SortBy> sortByLastNameAndStreet;
	protected static List<Skill> skills;
	protected static String[] firstNames = { "John", "Maxwell", "Joe", "Bob",
			"Eve", "Alice", "Scrooge", "Donald", "Mick", "Zandra" };
	protected static String[] lastNames = { "Smith", "Smart", "Cool",
			"Thornton", "McDuck", "Lee", "Anderson", "Zucker", "Jackson",
			"Gordon" };
	protected static String[] streets = { "Magna Avenue", "Fringilla Street",
			"Aliquet St.", "Pharetra Avenue", "Gravida St.", "Risus Street",
			"Ultricies Street", "Mi Avenue", "Libero Av.", "Purus Avenue" };
	protected static String[] postOffices = { "Stockholm", "Helsinki", "Paris",
			"London", "Luxemburg", "Duckburg", "New York", "Tokyo", "Athens",
			"Sydney" };
	protected static String[] skillNames = { "Java", "C", "C++", "Delphi", "PHP", "Vaadin", "JavaScript", "SQL", "HTML", "SOA"};

/*    @BeforeClass
    public static void setUpClass() throws Exception {
        createTestData();
    }*/

	/*
	 * The original idea was to create the test data once, persist it every
	 * time into a new, clean database and then run the test. Unfortunately,
	 * some changes made to the database seem to be reflected in the test data.
	 * Therefore, the test data is currently created in the beginning of each
	 * test.
	 */

	@Before
	public void setUp() throws Exception {
		createTestData();
		System.out.println("Setting up " + getClass());
		entityManager = createEntityManager();
        entityProvider = createEntityProvider();
        entityProvider_EmbeddedId = createEntityProvider_EmbeddedId();
        persistTestData();
	}
    
	@After
	public void tearDown() throws Exception {
		System.out.println("Tearing down " + getClass());
		entityProvider = null;
		entityProvider_EmbeddedId = null;
		entityManager.close();
		entityManager = null;
	}

	protected abstract EntityProvider<Person> createEntityProvider()
			throws Exception;

	protected abstract EntityProvider<EmbeddedIdPerson> createEntityProvider_EmbeddedId()
			throws Exception;

	@SuppressWarnings("unchecked")
	protected static void createTestData() throws Exception {
		// Create the test data
		skills = new ArrayList<Skill>();
		for (String skillName : skillNames) {
			Skill skill = new Skill();
			skill.setSkillName(skillName);
			skills.add(skill);
		}

		Random rnd = new Random();
		testDataSortedByPrimaryKey = new ArrayList<Person>();
		filteredTestDataSortedByPrimaryKey = new ArrayList<Person>();
		testDataEmbeddedIdSortedByName = new ArrayList<EmbeddedIdPerson>();
		for (int i = 0; i < 500; i++) {
			Person p = new Person();
			p.setFirstName(firstNames[(i / 10) % 10] + " " + i);
			p.setLastName(lastNames[i % 10]);
			p.setDateOfBirth(new Date(rnd.nextLong()));
			p.setAddress(new Address());
			p.getAddress().setStreet(
					rnd.nextInt(1000) + " "
							+ streets[rnd.nextInt(streets.length)]);
			p.getAddress().setPostOffice(
					postOffices[rnd.nextInt(postOffices.length)]);
			StringBuffer pc = new StringBuffer();
			for (int j = 0; j < 5; j++) {
				pc.append(rnd.nextInt(10));
			}
			p.getAddress().setPostalCode(pc.toString());
			testDataSortedByPrimaryKey.add(p);
			/*
			 * Our filter only includes persons whose lastname begin with S
			 */
			if (p.getLastName().startsWith("S")) {
				filteredTestDataSortedByPrimaryKey.add(p);
			}

			// EmbeddedId test data
			EmbeddedIdPerson eip = new EmbeddedIdPerson();
			eip.setName(new Name());
			eip.getName().setFirstName(p.getFirstName());
			eip.getName().setLastName(p.getLastName());
			eip.setAddress(p.getAddress().clone());
			eip.setDateOfBirth(p.getDateOfBirth());
			testDataEmbeddedIdSortedByName.add(eip);
		}

		testDataSortedByName = (ArrayList<Person>) ((ArrayList<Person>) testDataSortedByPrimaryKey)
				.clone();
		testDataSortedByLastNameAndStreet = (ArrayList<Person>) ((ArrayList<Person>) testDataSortedByPrimaryKey)
				.clone();
		filteredTestDataSortedByName = (ArrayList<Person>) ((ArrayList<Person>) filteredTestDataSortedByPrimaryKey)
				.clone();
		// Set up some helper fields

		sortByName = new ArrayList<SortBy>();
		sortByName.add(new SortBy("lastName", true));
		sortByName.add(new SortBy("firstName", true));

		sortByLastNameAndStreet = new ArrayList<SortBy>();
		sortByLastNameAndStreet.add(new SortBy("lastName", true));
		sortByLastNameAndStreet.add(new SortBy("address.street", true));

		testFilter = Filters.like("lastName", "S%", true);

		// Sort the test data lists

		Comparator<Person> nameComparator = new Comparator<Person>() {

			public int compare(Person o1, Person o2) {
				int result = o1.getLastName().compareTo(o2.getLastName());
				if (result == 0) {
					result = o1.getFirstName().compareTo(o2.getFirstName());
					/*if (result == 0) {
						result = o1.getId().compareTo(o2.getId());
					}*/
				}
				return result;
			}
		};

		Comparator<EmbeddedIdPerson> nameComparatorEmbeddedId = new Comparator<EmbeddedIdPerson>() {

			public int compare(EmbeddedIdPerson o1, EmbeddedIdPerson o2) {
				int result = o1.getName().getFirstName().compareTo(o2.getName().getFirstName());
				if (result == 0) {
					result = o1.getName().getLastName().compareTo(o2.getName().getLastName());
				}
				return result;
			}

		};

		Comparator<Person> nameStreetComparator = new Comparator<Person>() {

			public int compare(Person o1, Person o2) {
				int result = o1.getLastName().compareTo(o2.getLastName());
				if (result == 0) {
					result = o1.getAddress().getStreet().compareTo(
							o2.getAddress().getStreet());
					/*if (result == 0) {
						result = o1.getId().compareTo(o2.getId());
					}*/
				}
				return result;
			}
		};

		Collections.sort(testDataSortedByName, nameComparator);
		Collections.sort(testDataSortedByLastNameAndStreet,
				nameStreetComparator);
		Collections.sort(filteredTestDataSortedByName, nameComparator);
		Collections.sort(testDataEmbeddedIdSortedByName, nameComparatorEmbeddedId);

		assertFalse(testDataSortedByName.equals(testDataSortedByPrimaryKey));
		assertFalse(testDataSortedByLastNameAndStreet
				.equals(testDataSortedByPrimaryKey));
		assertFalse(filteredTestDataSortedByName
				.equals(filteredTestDataSortedByPrimaryKey));

        // Make the collections unmodifiable
        testDataEmbeddedIdSortedByName = Collections.unmodifiableList(testDataEmbeddedIdSortedByName);
        testDataSortedByLastNameAndStreet = Collections.unmodifiableList(
                testDataSortedByLastNameAndStreet);
        testDataSortedByName = Collections.unmodifiableList(testDataSortedByName);
        testDataSortedByPrimaryKey = Collections.unmodifiableList(testDataSortedByPrimaryKey);
	}

	@SuppressWarnings("unchecked")
	protected void persistTestData() throws Exception {
		getEntityManager().getTransaction().begin();
		for (Skill s : skills) {
			s.setId(null);
			getEntityManager().persist(s);
		}
        for (Person p : testDataSortedByPrimaryKey) {
            p.setId(null);
			if (p.getManager() != null) {
				p.getManager().setId(null);
			}
            getEntityManager().persist(p);
        }
        for (EmbeddedIdPerson p : testDataEmbeddedIdSortedByName) {
            getEntityManager().persist(p);
        }
        getEntityManager().flush();
		getEntityManager().getTransaction().commit();
	}

	protected void doTestGetEntity(final List<Person> testData) {
		for (Person p : testData) {
			Person returned = entityProvider.getEntity(p.getId());
			assertEquals(p, returned);
			// Make sure the entities are detached
			returned.setFirstName("Different firstname");
			assertFalse(returned.equals(entityProvider.getEntity(p.getId())));
		}
	}

	protected void doTestGetEntity_EmbeddedId(final List<EmbeddedIdPerson> testData) {
		for (EmbeddedIdPerson p : testData) {
			EmbeddedIdPerson returned = entityProvider_EmbeddedId.getEntity(
					p.getName());
			assertEquals(p, returned);
			// Make sure the entities are detached
			returned.getAddress().setStreet("another street");
			assertFalse(returned.equals(entityProvider_EmbeddedId.getEntity(
					p.getName())));
		}
	}

	protected void doTestGetEntityCount(final List<Person> testData,
			final Filter filter) {
		assertEquals(testData.size(), entityProvider.getEntityCount(filter));
	}

	protected void doTestGetEntityCount_EmbeddedId(final List<EmbeddedIdPerson> testData,
			final Filter filter) {
		assertEquals(testData.size(), entityProvider_EmbeddedId.getEntityCount(
				filter));
	}

	protected void doTestContainsEntity(final List<Person> testData,
			final Filter filter) {
		long maxKey = 0;
		for (Person p : testData) {
			if (maxKey < p.getId()) {
				maxKey = p.getId();
			}
			assertTrue(entityProvider.containsEntity(p.getId(), filter));
		}
		assertFalse(entityProvider.containsEntity(maxKey + 1, filter));
	}

	protected void doTestContainsEntity_EmbeddedId(final List<EmbeddedIdPerson> testData,
			final Filter filter) {
		for (EmbeddedIdPerson p : testData) {
			assertTrue(entityProvider_EmbeddedId.containsEntity(p.getName(), filter));
		}
	}

	protected void doTestGetFirstEntity(final List<Person> testData,
			final Filter filter, final List<SortBy> sortBy) {
		assertEquals(testData.get(0).getId(), entityProvider
				.getFirstEntityIdentifier(filter, sortBy));
	}

	protected void doTestGetFirstEntity_EmbeddedId(final List<EmbeddedIdPerson> testData,
			final Filter filter, final List<SortBy> sortBy) {
		assertEquals(testData.get(0).getName(), entityProvider_EmbeddedId
				.getFirstEntityIdentifier(filter, sortBy));
	}

	protected void doTestGetNextEntity(final List<Person> testData,
			final Filter filter, final List<SortBy> sortBy) {
		for (int i = 0; i < testData.size() - 1; i++) {
			assertEquals(testData.get(i + 1).getId(), entityProvider
					.getNextEntityIdentifier(testData.get(i).getId(), filter,
							sortBy));
		}
		assertNull(entityProvider.getNextEntityIdentifier(testData.get(
				testData.size() - 1).getId(), filter, sortBy));
	}

	protected void doTestGetNextEntity_EmbeddedId(final List<EmbeddedIdPerson> testData,
			final Filter filter, final List<SortBy> sortBy) {
		for (int i = 0; i < testData.size() - 1; i++) {
			assertEquals(testData.get(i + 1).getName(), entityProvider_EmbeddedId
					.getNextEntityIdentifier(testData.get(i).getName(), filter,
							sortBy));
		}
		assertNull(entityProvider_EmbeddedId.getNextEntityIdentifier(testData.get(
				testData.size() - 1).getName(), filter, sortBy));
	}

	protected void doTestGetLastEntity(final List<Person> testData,
			final Filter filter, final List<SortBy> sortBy) {
		assertEquals(testData.get(testData.size() - 1).getId(), entityProvider
				.getLastEntityIdentifier(filter, sortBy));
	}

	protected void doTestGetLastEntity_EmbeddedId(final List<EmbeddedIdPerson> testData,
			final Filter filter, final List<SortBy> sortBy) {
		assertEquals(testData.get(testData.size() -1).getName(), entityProvider_EmbeddedId
				.getLastEntityIdentifier(filter, sortBy));
	}

	protected void doTestGetPreviousEntity(final List<Person> testData,
			final Filter filter, final List<SortBy> sortBy) {
		for (int i = testData.size() - 1; i > 0; i--) {
//			System.out.println("testData[" + (i-1) + "] = " + testData.get(i-1).getId());
//			System.out.println(" actual = " + entityProvider
//					.getPreviousEntityIdentifier(testData.get(i).getId(),
//							filter, sortBy));
			assertEquals(testData.get(i - 1).getId(), entityProvider
					.getPreviousEntityIdentifier(testData.get(i).getId(),
							filter, sortBy));
		}
		assertNull(entityProvider.getPreviousEntityIdentifier(testData.get(0)
				.getId(), filter, sortBy));
	}

	protected void doTestGetPreviousEntity_EmbeddedId(final List<EmbeddedIdPerson> testData,
			final Filter filter, final List<SortBy> sortBy) {
		for (int i = testData.size() - 1; i > 0; i--) {
			assertEquals(testData.get(i - 1).getName(), entityProvider_EmbeddedId
					.getPreviousEntityIdentifier(testData.get(i).getName(),
							filter, sortBy));
		}
		assertNull(entityProvider_EmbeddedId.getPreviousEntityIdentifier(testData.get(0)
				.getName(), filter, sortBy));
	}

	protected void doTestGetEntityIdentifierAt(final List<Person> testData,
			final Filter filter, final List<SortBy> sortBy) {
		for (int i = 0; i < testData.size(); i++) {
			assertEquals(testData.get(i).getId(), entityProvider
					.getEntityIdentifierAt(filter, sortBy, i));
		}
		assertNull(entityProvider.getEntityIdentifierAt(filter, sortBy,
				testData.size()));
	}

	protected void doTestGetEntityIdentifierAtBackwards(final List<Person> testData,
			final Filter filter, final List<SortBy> sortBy) {
		assertNull(entityProvider.getEntityIdentifierAt(filter, sortBy,
				testData.size()));
		for (int i = testData.size() -1; i >= 0; i--) {
//			System.out.println("testData[" + i + "] = " + testData.get(i).getId());
//			System.out.println("  result = " + entityProvider.getEntityIdentifierAt(filter, sortBy, i));
			assertEquals(testData.get(i).getId(), entityProvider
					.getEntityIdentifierAt(filter, sortBy, i));
		}
	}

	protected void doTestGetEntityIdentifierAt_EmbeddedId(final List<EmbeddedIdPerson> testData,
			final Filter filter, final List<SortBy> sortBy) {
		for (int i = 0; i < testData.size(); i++) {
			assertEquals(testData.get(i).getName(), entityProvider_EmbeddedId
					.getEntityIdentifierAt(filter, sortBy, i));
		}
		assertNull(entityProvider_EmbeddedId.getEntityIdentifierAt(filter, sortBy,
				testData.size()));
	}

	protected void doTestGetEntityIdentifierAtBackwards_EmbeddedId(final List<EmbeddedIdPerson> testData,
			final Filter filter, final List<SortBy> sortBy) {
		for (int i = testData.size() -1; i >= 0; i--) {
			assertEquals(testData.get(i).getName(), entityProvider_EmbeddedId
					.getEntityIdentifierAt(filter, sortBy, i));
		}
		assertNull(entityProvider_EmbeddedId.getEntityIdentifierAt(filter, sortBy,
				testData.size()));
	}

	@Test
	public void testGetEntity() {
		System.out.println("testGetEntity");
		doTestGetEntity(testDataSortedByName);
	}

	@Test
	public void testGetEntity_EmbeddedId() {
		System.out.println("testGetEntity_EmbeddedId");
		doTestGetEntity_EmbeddedId(testDataEmbeddedIdSortedByName);
	}

	@Test
	public void testGetEntityCount() {
		System.out.println("testGetEntityCount");
		doTestGetEntityCount(testDataSortedByName, null);
	}

	@Test
	public void testGetEntityCount_EmbeddedId() {
		System.out.println("testGetEntityCount_EmbeddedId");
		doTestGetEntityCount_EmbeddedId(testDataEmbeddedIdSortedByName,
				null);
	}

	@Test
	public void testContainsEntity() {
		System.out.println("testContainsEntity");
		doTestContainsEntity(testDataSortedByName, null);
	}

	@Test
	public void testContainsEntity_EmbeddedId() {
		System.out.println("testContainsEntity_EmbeddedId");
		doTestContainsEntity_EmbeddedId(testDataEmbeddedIdSortedByName,
				null);
	}

	@Test
	public void testGetFirstEntity() {
		System.out.println("testGetFirstEntity");
		doTestGetFirstEntity(testDataSortedByName, null, sortByName);
	}

	@Test
	public void testGetFirstEntity_EmbeddedId() {
		System.out.println("testGetFirstEntity_EmbeddedId");
		doTestGetFirstEntity_EmbeddedId(testDataEmbeddedIdSortedByName,
				null, null);
	}

	@Test
	public void testGetNextEntity() {
		System.out.println("testGetNextEntity");
		doTestGetNextEntity(testDataSortedByName, null, sortByName);
	}

	@Test
	public void testGetNextEntity_EmbeddedId() {
		System.out.println("testGetNextEntity_EmbeddedId");
		doTestGetNextEntity_EmbeddedId(testDataEmbeddedIdSortedByName,
				null, null);
	}

	@Test
	public void testGetLastEntity() {
		System.out.println("testGetLastEntity");
		doTestGetLastEntity(testDataSortedByName, null, sortByName);
	}

	@Test
	public void testGetLastEntity_EmbeddedId() {
		System.out.println("testGetLastEntity_EmbeddedID");
		doTestGetLastEntity_EmbeddedId(testDataEmbeddedIdSortedByName,
				null, null);
	}

	@Test
	public void testGetPreviousEntity() {
		System.out.println("testGetPreviousEntity");
		doTestGetPreviousEntity(testDataSortedByName, null, sortByName);
	}

	@Test
	public void testGetPreviousEntity_EmbeddedId() {
		System.out.println("testGetPreviousEntity_EmbeddedId");
		doTestGetPreviousEntity_EmbeddedId(testDataEmbeddedIdSortedByName,
				null, null);
	}

	@Test
	public void testGetEntityIdentifierAt() {
		System.out.println("testGetEntityIdentifierAt");
		doTestGetEntityIdentifierAt(testDataSortedByName, null, sortByName);
	}

	@Test
	public void testGetEntityIdentifierAtBackwards() {
		System.out.println("testGetEntityIdentifierAtBackwards");
		doTestGetEntityIdentifierAtBackwards(testDataSortedByName, null, sortByName);
	}

	@Test
	public void testGetEntityIdentifierAt_EmbeddedId() {
		System.out.println("testGetEntityIdentifierAt_EmbeddedId");
		doTestGetEntityIdentifierAt_EmbeddedId(testDataEmbeddedIdSortedByName, null, null);
	}

	@Test
	public void testGetEntityIdentifierAtBackwards_EmbeddedId() {
		System.out.println("testGetEntityIdentifierAtBackwards_EmbeddedId");
		doTestGetEntityIdentifierAtBackwards_EmbeddedId(testDataEmbeddedIdSortedByName, null, null);
	}
	// TODO Add tests for container with duplicate sorted values

	@Test
	public void testGetFirstEntity_SortedByLastNameAndStreet() {
		System.out.println("testGetFirstEntity_SortedByLastNameAndStreet");
		doTestGetFirstEntity(testDataSortedByLastNameAndStreet, null,
				sortByLastNameAndStreet);
	}

	@Test
	public void testGetNextEntity_SortedByLastNameAndStreet() {
		System.out.println("testGetNextEntity_SortedByLastNameAndStreet");
		doTestGetNextEntity(testDataSortedByLastNameAndStreet, null,
				sortByLastNameAndStreet);
	}

	@Test
	public void testGetLastEntity_SortedByLastNameAndStreet() {
		System.out.println("testGetLastEntity_SortedByLastNameAndStreet");
		doTestGetLastEntity(testDataSortedByLastNameAndStreet, null,
				sortByLastNameAndStreet);
	}

	@Test
	public void testGetPreviousEntity_SortedByLastNameAndStreet() {
		System.out.println("testGetPreviousEntity_SortedByLastNameAndStreet");
		doTestGetPreviousEntity(testDataSortedByLastNameAndStreet, null,
				sortByLastNameAndStreet);
	}

	@Test
	public void testGetEntityIdentifierAt_SortedByLastNameAndStreet() {
		System.out
				.println("testGetEntityIdentifierAt_SortedByLastNameAndStreet");
		doTestGetEntityIdentifierAt(testDataSortedByLastNameAndStreet, null,
				sortByLastNameAndStreet);
	}

	@Test
	public void testGetEntityIdentifierAtBackwards_SortedByLastNameAndStreet() {
		System.out
				.println("testGetEntityIdentifierAtBackwards_SortedByLastNameAndStreet");
		doTestGetEntityIdentifierAtBackwards(testDataSortedByLastNameAndStreet, null,
				sortByLastNameAndStreet);
	}

	@Test
	public void testGetFirstEntity_SortedByPrimaryKey() {
		System.out.println("testGetFirstEntity_SortedByPrimaryKey");
		doTestGetFirstEntity(testDataSortedByPrimaryKey, null, null);
	}

	@Test
	public void testGetNextEntity_SortedByPrimaryKey() {
		System.out.println("testGetNextEntity_SortedByPrimaryKey");
		doTestGetNextEntity(testDataSortedByPrimaryKey, null, null);
	}

	@Test
	public void testGetLastEntity_SortedByPrimaryKey() {
		System.out.println("testGetLastEntity_SortedByPrimaryKey");
		doTestGetLastEntity(testDataSortedByPrimaryKey, null, null);
	}

	@Test
	public void testGetPreviousEntity_SortedByPrimaryKey() {
		System.out.println("testGetPreviousEntity_SortedByPrimaryKey");
		doTestGetPreviousEntity(testDataSortedByPrimaryKey, null, null);
	}

	@Test
	public void testGetEntityIdentifierAt_SortedByPrimaryKey() {
		System.out.println("testGetEntityIdentifierAt_SortedByPrimaryKey");
		doTestGetEntityIdentifierAt(testDataSortedByPrimaryKey, null, null);
	}

	@Test
	public void testGetEntityIdentifierAtBackwards_SortedByPrimaryKey() {
		System.out.println("testGetEntityIdentifierAtBackwards_SortedByPrimaryKey");
		doTestGetEntityIdentifierAtBackwards(testDataSortedByPrimaryKey, null, null);
	}

	@Test
	public void testGetEntityCount_Filtered() {
		System.out.println("testGetEntityCount_Filtered");
		doTestGetEntityCount(filteredTestDataSortedByName, testFilter);
	}

	@Test
	public void testGetContainsEntity_Filtered() {
		System.out.println("testGetContainsEntity_Filtered");
		doTestContainsEntity(filteredTestDataSortedByName, testFilter);
	}

	@Test
	public void testGetFirstEntity_Filtered() {
		System.out.println("testGetFirstEntity_Filtered");
		doTestGetFirstEntity(filteredTestDataSortedByName, testFilter,
				sortByName);
	}

	@Test
	public void testGetNextEntity_Filtered() {
		System.out.println("testGetNextEntity_Filtered");
		doTestGetNextEntity(filteredTestDataSortedByName, testFilter,
				sortByName);
	}

	@Test
	public void testGetLastEntity_Filtered() {
		System.out.println("testGetLastEntity_Filtered");
		doTestGetLastEntity(filteredTestDataSortedByName, testFilter,
				sortByName);
	}

	@Test
	public void testGetPreviousEntity_Filtered() {
		System.out.println("testGetPreviousEntity_Filtered");
		doTestGetPreviousEntity(filteredTestDataSortedByName, testFilter,
				sortByName);
	}

	@Test
	public void testGetEntityIdentifierAt_Filtered() {
		System.out.println("testGetEntityIdentifierAt_Filtered");
		doTestGetEntityIdentifierAt(filteredTestDataSortedByName, testFilter,
				sortByName);
	}

	@Test
	public void testGetEntityIdentifierAtBackwards_Filtered() {
		System.out.println("testGetEntityIdentifierAtBackwards_Filtered");
		doTestGetEntityIdentifierAtBackwards(filteredTestDataSortedByName, testFilter,
				sortByName);
	}

	@Test
	public void testGetFirstEntity_Filtered_SortedByPrimaryKey() {
		System.out.println("testGetFirstEntity_Filtered_SortedByPrimaryKey");
		doTestGetFirstEntity(filteredTestDataSortedByPrimaryKey, testFilter,
				null);
	}

	@Test
	public void testGetNextEntity_Filtered_SortedByPrimaryKey() {
		System.out.println("testGetNextEntity_Filtered_SortedByPrimaryKey");
		doTestGetNextEntity(filteredTestDataSortedByPrimaryKey, testFilter,
				null);
	}

	@Test
	public void testGetLastEntity_Filtered_SortedByPrimaryKey() {
		System.out.println("testGetLastEntity_Filtered_SortedByPrimaryKey");
		doTestGetLastEntity(filteredTestDataSortedByPrimaryKey, testFilter,
				null);
	}

	@Test
	public void testGetPreviousEntity_Filtered_SortedByPrimaryKey() {
		System.out.println("testGetPreviousEntity_Filtered_SortedByPrimaryKey");
		doTestGetPreviousEntity(filteredTestDataSortedByPrimaryKey, testFilter,
				null);
	}

	@Test
	public void testGetEntityIdentifierAt_Filtered_SortedByPrimaryKey() {
		System.out
				.println("testGetEntityIdentifierAt_Filtered_SortedByPrimaryKey");
		doTestGetEntityIdentifierAt(filteredTestDataSortedByPrimaryKey,
				testFilter, null);
	}

	@Test
	public void testGetEntityIdentifierAtBackwards_Filtered_SortedByPrimaryKey() {
		System.out
				.println("testGetEntityIdentifierAtBackwards_Filtered_SortedByPrimaryKey");
		doTestGetEntityIdentifierAtBackwards(filteredTestDataSortedByPrimaryKey,
				testFilter, null);
	}

	@Test
	public void testJoinFilter() throws Exception {
		// Save some testing data
		Random rnd = new Random();
		Map<Skill, Collection<Object>> skillPersonMap = new HashMap<Skill, Collection<Object>>();
		getEntityManager().getTransaction().begin();
		for (Skill s : skills) {
			Set<Object> persons = new HashSet<Object>();
			for (int i = 0; i < 10; i++) {
				Person p = testDataSortedByPrimaryKey.get(rnd.nextInt(testDataSortedByPrimaryKey.size()));
				System.out.println("Skill: " + s+ " Person: " + p);
				if (!persons.contains(p.getId())) {
					persons.add(p.getId());
					p.addSkill(s, i+1);
					getEntityManager().merge(p);
				}
			}
			skillPersonMap.put(s, persons);
		}
		getEntityManager().flush();
		getEntityManager().getTransaction().commit();

		// Now try out the filter
		for (Skill s : skills) {
			Filter filter = Filters.joinFilter("skills", Filters.eq("skill", s));
			Collection<Object> returnedIds = entityProvider.getAllEntityIdentifiers(filter, null);
			assertTrue(skillPersonMap.get(s).containsAll(returnedIds));
			assertEquals(skillPersonMap.get(s).size(), returnedIds.size());
		}
	}

	// TODO Add test for getAllEntityIdentifiers
}
