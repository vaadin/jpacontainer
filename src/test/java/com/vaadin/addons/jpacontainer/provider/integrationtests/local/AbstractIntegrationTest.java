/*
 * JPAContainer
 * Copyright (C) 2009 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.provider.integrationtests.local;

import com.vaadin.addons.jpacontainer.testdata.Address;
import com.vaadin.addons.jpacontainer.testdata.Person;
import com.vaadin.addons.jpacontainer.EntityProvider;
import com.vaadin.addons.jpacontainer.SortBy;
import com.vaadin.addons.jpacontainer.Filter;
import com.vaadin.addons.jpacontainer.filter.Filters;
import com.vaadin.addons.jpacontainer.provider.LocalEntityProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Abstract test case for {@link LocalEntityProvider} that should work
 * with any entity manager that follows the specifications. Subclasses should
 * provide a concrete entity manager implementation to test. If the test passes,
 * the entity manager implementation should work with JPAContainer.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractIntegrationTest {

    protected abstract EntityManager getEntityManager() throws Exception;
    protected EntityProvider<Person> entityProvider;
    protected List<Person> testDataSortedByPrimaryKey;
    protected List<Person> testDataSortedByName;
    protected List<Person> testDataSortedByLastNameAndStreet;
    protected List<Person> filteredTestDataSortedByPrimaryKey;
    protected List<Person> filteredTestDataSortedByName;
    protected Filter testFilter;
    protected List<SortBy> sortByName;
    protected List<SortBy> sortByLastNameAndStreet;
    protected static String[] firstNames = {"John", "Maxwell", "Joe", "Bob",
        "Eve", "Alice", "Scrooge", "Donald", "Mick", "Zandra"};
    protected static String[] lastNames = {"Smith", "Smart", "Cool", "Thornton",
        "McDuck", "Lee", "Anderson", "Zucker", "Jackson", "Gordon"};
    protected static String[] streets = {"Magna Avenue", "Fringilla Street",
        "Aliquet St.", "Pharetra Avenue", "Gravida St.", "Risus Street",
        "Ultricies Street", "Mi Avenue", "Libero Av.", "Purus Avenue"};
    protected static String[] postOffices = {"Stockholm", "Helsinki", "Paris",
        "London", "Luxemburg", "Duckburg", "New York", "Tokyo", "Athens",
        "Sydney"};

    @Before
    public void setUp() throws Exception {
        entityProvider = new LocalEntityProvider<Person>(
                Person.class, getEntityManager());
        createTestData();
    }

    protected void createTestData() throws Exception {
        // Create the test data
        getEntityManager().getTransaction().begin();
        Random rnd = new Random();
        testDataSortedByPrimaryKey = new ArrayList<Person>();
        filteredTestDataSortedByPrimaryKey = new ArrayList<Person>();
        for (int i = 0; i < 100; i++) {
            Person p = new Person();
            p.setFirstName(firstNames[i / 10]);
            p.setLastName(lastNames[i % 10]);
            p.setDateOfBirth(new Date(rnd.nextLong()));
            p.setAddress(new Address());
            p.getAddress().setStreet(rnd.nextInt(1000) + " " + streets[rnd.
                    nextInt(streets.length)]);
            p.getAddress().setPostOffice(postOffices[rnd.nextInt(
                    postOffices.length)]);
            StringBuffer pc = new StringBuffer();
            for (int j = 0; j < 5; j++) {
                pc.append(rnd.nextInt(10));
            }
            p.getAddress().setPostalCode(pc.toString());
            getEntityManager().persist(p);
            testDataSortedByPrimaryKey.add(p);
            /*
             * Our filter only includes persons whose lastname begin with S
             */
            if (p.getLastName().startsWith("S")) {
                filteredTestDataSortedByPrimaryKey.add(p);
            }
        }
        getEntityManager().flush();
        getEntityManager().getTransaction().commit();

        testDataSortedByName = (ArrayList<Person>) ((ArrayList<Person>) testDataSortedByPrimaryKey).
                clone();
        testDataSortedByLastNameAndStreet = (ArrayList<Person>) ((ArrayList<Person>) testDataSortedByPrimaryKey).
                clone();
        filteredTestDataSortedByName = (ArrayList<Person>) ((ArrayList<Person>) filteredTestDataSortedByPrimaryKey).
                clone();

        // Sort the test data lists

        Comparator<Person> nameComparator = new Comparator<Person>() {

            @Override
            public int compare(Person o1, Person o2) {
                int result = o1.getLastName().compareTo(o2.getLastName());
                if (result == 0) {
                    result = o1.getFirstName().compareTo(o2.getFirstName());
                    if (result == 0) {
                        result = o1.getId().compareTo(o2.getId());
                    }
                }
                return result;
            }
        };

        Comparator<Person> nameStreetComparator = new Comparator<Person>() {

            @Override
            public int compare(Person o1, Person o2) {
                int result = o1.getLastName().compareTo(o2.getLastName());
                if (result == 0) {
                    result = o1.getAddress().getStreet().compareTo(o2.getAddress().
                            getStreet());
                    if (result == 0) {
                        result = o1.getId().compareTo(o2.getId());
                    }
                }
                return result;
            }
        };

        Collections.sort(testDataSortedByName, nameComparator);
        Collections.sort(testDataSortedByLastNameAndStreet, nameStreetComparator);
        Collections.sort(filteredTestDataSortedByName, nameComparator);

        assertFalse(testDataSortedByName.equals(testDataSortedByPrimaryKey));
        assertFalse(testDataSortedByLastNameAndStreet.equals(
                testDataSortedByPrimaryKey));
        assertFalse(filteredTestDataSortedByName.equals(
                filteredTestDataSortedByPrimaryKey));

        // Set up some helper fields

        sortByName = new ArrayList<SortBy>();
        sortByName.add(new SortBy("lastName", true));
        sortByName.add(new SortBy("firstName", true));

        sortByLastNameAndStreet = new ArrayList<SortBy>();
        sortByLastNameAndStreet.add(new SortBy("lastName", true));
        sortByLastNameAndStreet.add(new SortBy("address.street", true));

        testFilter = Filters.like("lastName", "S%", true);
    }

    // TODO Add test for getEntityAt

    protected void doTestGetEntity(final List<Person> testData) {
        for (Person p : testData) {
            assertEquals(p, entityProvider.getEntity(p.getId()));
        }
    }

    protected void doTestGetEntityCount(final List<Person> testData,
            final Filter filter) {
        assertEquals(testData.size(), entityProvider.getEntityCount(filter));
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

    protected void doTestGetFirstEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        assertEquals(testData.get(0).getId(), entityProvider.
                getFirstEntityIdentifier(filter,
                sortBy));
    }

    protected void doTestGetNextEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        for (int i = 0; i < testData.size() - 1; i++) {
            assertEquals(testData.get(i + 1).getId(), entityProvider.
                    getNextEntityIdentifier(testData.get(i).getId(), filter,
                    sortBy));
        }
        assertNull(entityProvider.getNextEntityIdentifier(testData.get(testData.
                size()
                - 1).getId(), filter, sortBy));
    }

    protected void doTestGetLastEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        assertEquals(testData.get(testData.size() - 1).getId(), entityProvider.
                getLastEntityIdentifier(filter, sortBy));
    }

    protected void doTestGetPreviousEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        for (int i = testData.size() - 1; i > 0; i--) {
            assertEquals(testData.get(i - 1).getId(), entityProvider.
                    getPreviousEntityIdentifier(testData.get(i).getId(), filter,
                    sortBy));
        }
        assertNull(entityProvider.getPreviousEntityIdentifier(
                testData.get(0).getId(),
                filter, sortBy));

    }

    @Test
    public void testGetEntity() {
        doTestGetEntity(testDataSortedByName);
    }

    @Test
    public void testGetEntityCount() {
        doTestGetEntityCount(testDataSortedByName, null);
    }

    @Test
    public void testContainsEntity() {
        doTestContainsEntity(testDataSortedByName, null);
    }

    @Test
    public void testGetFirstEntity() {
        doTestGetFirstEntity(testDataSortedByName, null, sortByName);
    }

    @Test
    public void testGetNextEntity() {
        doTestGetNextEntity(testDataSortedByName, null, sortByName);
    }

    @Test
    public void testGetLastEntity() {
        doTestGetLastEntity(testDataSortedByName, null, sortByName);
    }

    @Test
    public void testGetPreviousEntity() {
        doTestGetPreviousEntity(testDataSortedByName, null, sortByName);
    }

    // TODO Add tests for container with duplicate sorted values
    @Test
    public void testGetFirstEntity_SortedByLastNameAndStreet() {
        doTestGetFirstEntity(testDataSortedByLastNameAndStreet, null,
                sortByLastNameAndStreet);
    }

    @Test
    public void testGetNextEntity_SortedByLastNameAndStreet() {
        doTestGetNextEntity(testDataSortedByLastNameAndStreet, null,
                sortByLastNameAndStreet);
    }

    @Test
    public void testGetLastEntity_SortedByLastNameAndStreet() {
        doTestGetLastEntity(testDataSortedByLastNameAndStreet, null,
                sortByLastNameAndStreet);
    }

    @Test
    public void testGetPreviousEntity_SortedByLastNameAndStreet() {
        doTestGetPreviousEntity(testDataSortedByLastNameAndStreet, null,
                sortByLastNameAndStreet);
    }

    @Test
    public void testGetFirstEntity_SortedByPrimaryKey() {
        List<SortBy> emptyList = Collections.emptyList();
        doTestGetFirstEntity(testDataSortedByPrimaryKey, null, emptyList);
    }

    @Test
    public void testGetNextEntity_SortedByPrimaryKey() {
        List<SortBy> emptyList = Collections.emptyList();
        doTestGetNextEntity(testDataSortedByPrimaryKey, null, emptyList);
    }

    @Test
    public void testGetLastEntity_SortedByPrimaryKey() {
        List<SortBy> emptyList = Collections.emptyList();
        doTestGetLastEntity(testDataSortedByPrimaryKey, null, emptyList);
    }

    @Test
    public void testGetPreviousEntity_SortedByPrimaryKey() {
        List<SortBy> emptyList = Collections.emptyList();
        doTestGetPreviousEntity(testDataSortedByPrimaryKey, null, emptyList);
    }

    @Test
    public void testGetEntityCount_Filtered() {
        doTestGetEntityCount(filteredTestDataSortedByName, testFilter);
    }

    @Test
    public void testGetContainsEntity_Filtered() {
        doTestContainsEntity(filteredTestDataSortedByName, testFilter);
    }

    @Test
    public void testGetFirstEntity_Filtered() {
        doTestGetFirstEntity(filteredTestDataSortedByName, testFilter,
                sortByName);
    }

    @Test
    public void testGetNextEntity_Filtered() {
        doTestGetNextEntity(filteredTestDataSortedByName, testFilter, sortByName);
    }

    @Test
    public void testGetLastEntity_Filtered() {
        doTestGetLastEntity(filteredTestDataSortedByName, testFilter, sortByName);
    }

    @Test
    public void testGetPreviousEntity_Filtered() {
        doTestGetPreviousEntity(filteredTestDataSortedByName, testFilter,
                sortByName);
    }

    @Test
    public void testGetFirstEntity_Filtered_SortedByPrimaryKey() {
        List<SortBy> emptyList = Collections.emptyList();
        doTestGetFirstEntity(filteredTestDataSortedByPrimaryKey, testFilter,
                emptyList);
    }

    @Test
    public void testGetNextEntity_Filtered_SortedByPrimaryKey() {
        List<SortBy> emptyList = Collections.emptyList();
        doTestGetNextEntity(filteredTestDataSortedByPrimaryKey, testFilter,
                emptyList);
    }

    @Test
    public void testGetLastEntity_Filtered_SortedByPrimaryKey() {
        List<SortBy> emptyList = Collections.emptyList();
        doTestGetLastEntity(filteredTestDataSortedByPrimaryKey, testFilter,
                emptyList);
    }

    @Test
    public void testGetPreviousEntity_Filtered_SortedByPrimaryKey() {
        List<SortBy> emptyList = Collections.emptyList();
        doTestGetPreviousEntity(filteredTestDataSortedByPrimaryKey, testFilter,
                emptyList);
    }
}
