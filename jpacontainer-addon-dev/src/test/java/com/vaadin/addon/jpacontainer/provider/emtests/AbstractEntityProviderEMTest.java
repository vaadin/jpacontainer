/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.provider.emtests;

import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.SortBy;
import com.vaadin.addon.jpacontainer.Filter;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Skill;
import com.vaadin.addon.jpacontainer.testdata.TestDataGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
        /*
         * if (entityManager == null) { entityManager = createEntityManager(); }
         */
        return entityManager;
    }

    protected EntityProvider<Person> entityProvider;
    protected EntityProvider<EmbeddedIdPerson> entityProvider_EmbeddedId;
    

    /*
     * @BeforeClass public static void setUpClass() throws Exception {
     * createTestData(); }
     */

    /*
     * The original idea was to create the test data once, persist it every time
     * into a new, clean database and then run the test. Unfortunately, some
     * changes made to the database seem to be reflected in the test data.
     * Therefore, the test data is currently created in the beginning of each
     * test.
     */

    @Before
    public void setUp() throws Exception {
        TestDataGenerator.createTestData();
        System.out.println("Setting up " + getClass());
        entityManager = createEntityManager();
        entityProvider = createEntityProvider();
        entityProvider_EmbeddedId = createEntityProvider_EmbeddedId();
        TestDataGenerator.persistTestData(entityManager);
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

    protected void doTestGetEntity(final List<Person> testData) {
        for (Person p : testData) {
            Person returned = entityProvider.getEntity(p.getId());
            assertEquals(p, returned);
            // Make sure the entities are detached
            returned.setFirstName("Different firstname");
            assertFalse(returned.equals(entityProvider.getEntity(p.getId())));
        }
    }

    protected void doTestGetEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData) {
        for (EmbeddedIdPerson p : testData) {
            EmbeddedIdPerson returned = entityProvider_EmbeddedId.getEntity(p
                    .getName());
            assertEquals(p, returned);
            // Make sure the entities are detached
            returned.getAddress().setStreet("another street");
            assertFalse(returned.equals(entityProvider_EmbeddedId.getEntity(p
                    .getName())));
        }
    }

    protected void doTestGetEntityCount(final List<Person> testData,
            final Filter filter) {
        assertEquals(testData.size(), entityProvider.getEntityCount(filter));
    }

    protected void doTestGetEntityCount_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter) {
        assertEquals(testData.size(),
                entityProvider_EmbeddedId.getEntityCount(filter));
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

    protected void doTestContainsEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter) {
        for (EmbeddedIdPerson p : testData) {
            assertTrue(entityProvider_EmbeddedId.containsEntity(p.getName(),
                    filter));
        }
    }

    protected void doTestGetFirstEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        assertEquals(testData.get(0).getId(),
                entityProvider.getFirstEntityIdentifier(filter, sortBy));
    }

    protected void doTestGetFirstEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        assertEquals(testData.get(0).getName(),
                entityProvider_EmbeddedId.getFirstEntityIdentifier(filter,
                        sortBy));
    }

    protected void doTestGetNextEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        for (int i = 0; i < testData.size() - 1; i++) {
            assertEquals(testData.get(i + 1).getId(),
                    entityProvider.getNextEntityIdentifier(testData.get(i)
                            .getId(), filter, sortBy));
        }
        assertNull(entityProvider.getNextEntityIdentifier(
                testData.get(testData.size() - 1).getId(), filter, sortBy));
    }

    protected void doTestGetNextEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        for (int i = 0; i < testData.size() - 1; i++) {
            assertEquals(testData.get(i + 1).getName(),
                    entityProvider_EmbeddedId.getNextEntityIdentifier(testData
                            .get(i).getName(), filter, sortBy));
        }
        assertNull(entityProvider_EmbeddedId.getNextEntityIdentifier(testData
                .get(testData.size() - 1).getName(), filter, sortBy));
    }

    protected void doTestGetLastEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        assertEquals(testData.get(testData.size() - 1).getId(),
                entityProvider.getLastEntityIdentifier(filter, sortBy));
    }

    protected void doTestGetLastEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        assertEquals(testData.get(testData.size() - 1).getName(),
                entityProvider_EmbeddedId.getLastEntityIdentifier(filter,
                        sortBy));
    }

    protected void doTestGetPreviousEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        for (int i = testData.size() - 1; i > 0; i--) {
            // System.out.println("testData[" + (i-1) + "] = " +
            // testData.get(i-1).getId());
            // System.out.println(" actual = " + entityProvider
            // .getPreviousEntityIdentifier(testData.get(i).getId(),
            // filter, sortBy));
            assertEquals(testData.get(i - 1).getId(),
                    entityProvider.getPreviousEntityIdentifier(testData.get(i)
                            .getId(), filter, sortBy));
        }
        assertNull(entityProvider.getPreviousEntityIdentifier(testData.get(0)
                .getId(), filter, sortBy));
    }

    protected void doTestGetPreviousEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        for (int i = testData.size() - 1; i > 0; i--) {
            assertEquals(testData.get(i - 1).getName(),
                    entityProvider_EmbeddedId.getPreviousEntityIdentifier(
                            testData.get(i).getName(), filter, sortBy));
        }
        assertNull(entityProvider_EmbeddedId.getPreviousEntityIdentifier(
                testData.get(0).getName(), filter, sortBy));
    }

    protected void doTestGetEntityIdentifierAt(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        for (int i = 0; i < testData.size(); i++) {
            assertEquals(testData.get(i).getId(),
                    entityProvider.getEntityIdentifierAt(filter, sortBy, i));
        }
        assertNull(entityProvider.getEntityIdentifierAt(filter, sortBy,
                testData.size()));
    }

    protected void doTestGetEntityIdentifierAtBackwards(
            final List<Person> testData, final Filter filter,
            final List<SortBy> sortBy) {
        assertNull(entityProvider.getEntityIdentifierAt(filter, sortBy,
                testData.size()));
        for (int i = testData.size() - 1; i >= 0; i--) {
            // System.out.println("testData[" + i + "] = " +
            // testData.get(i).getId());
            // System.out.println("  result = " +
            // entityProvider.getEntityIdentifierAt(filter, sortBy, i));
            assertEquals(testData.get(i).getId(),
                    entityProvider.getEntityIdentifierAt(filter, sortBy, i));
        }
    }

    protected void doTestGetEntityIdentifierAt_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        for (int i = 0; i < testData.size(); i++) {
            assertEquals(testData.get(i).getName(),
                    entityProvider_EmbeddedId.getEntityIdentifierAt(filter,
                            sortBy, i));
        }
        assertNull(entityProvider_EmbeddedId.getEntityIdentifierAt(filter,
                sortBy, testData.size()));
    }

    protected void doTestGetEntityIdentifierAtBackwards_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        for (int i = testData.size() - 1; i >= 0; i--) {
            assertEquals(testData.get(i).getName(),
                    entityProvider_EmbeddedId.getEntityIdentifierAt(filter,
                            sortBy, i));
        }
        assertNull(entityProvider_EmbeddedId.getEntityIdentifierAt(filter,
                sortBy, testData.size()));
    }

    @Test
    public void testGetEntity() {
        System.out.println("testGetEntity");
        doTestGetEntity(TestDataGenerator.getTestDataSortedByName());
    }

    @Test
    public void testGetEntity_EmbeddedId() {
        System.out.println("testGetEntity_EmbeddedId");
        doTestGetEntity_EmbeddedId(TestDataGenerator.getTestDataEmbeddedIdSortedByName());
    }

    @Test
    public void testGetEntityCount() {
        System.out.println("testGetEntityCount");
        doTestGetEntityCount(TestDataGenerator.getTestDataSortedByName(), null);
    }

    @Test
    public void testGetEntityCount_EmbeddedId() {
        System.out.println("testGetEntityCount_EmbeddedId");
        doTestGetEntityCount_EmbeddedId(TestDataGenerator.getTestDataEmbeddedIdSortedByName(), null);
    }

    @Test
    public void testContainsEntity() {
        System.out.println("testContainsEntity");
        doTestContainsEntity(TestDataGenerator.getTestDataSortedByName(), null);
    }

    @Test
    public void testContainsEntity_EmbeddedId() {
        System.out.println("testContainsEntity_EmbeddedId");
        doTestContainsEntity_EmbeddedId(TestDataGenerator.getTestDataEmbeddedIdSortedByName(), null);
    }

    @Test
    public void testGetFirstEntity() {
        System.out.println("testGetFirstEntity");
        doTestGetFirstEntity(TestDataGenerator.getTestDataSortedByName(), null, TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetFirstEntity_EmbeddedId() {
        System.out.println("testGetFirstEntity_EmbeddedId");
        doTestGetFirstEntity_EmbeddedId(TestDataGenerator.getTestDataEmbeddedIdSortedByName(), null,
                null);
    }

    @Test
    public void testGetNextEntity() {
        System.out.println("testGetNextEntity");
        doTestGetNextEntity(TestDataGenerator.getTestDataSortedByName(), null, TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetNextEntity_EmbeddedId() {
        System.out.println("testGetNextEntity_EmbeddedId");
        doTestGetNextEntity_EmbeddedId(TestDataGenerator.getTestDataEmbeddedIdSortedByName(), null,
                null);
    }

    @Test
    public void testGetLastEntity() {
        System.out.println("testGetLastEntity");
        doTestGetLastEntity(TestDataGenerator.getTestDataSortedByName(), null, TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetLastEntity_EmbeddedId() {
        System.out.println("testGetLastEntity_EmbeddedID");
        doTestGetLastEntity_EmbeddedId(TestDataGenerator.getTestDataEmbeddedIdSortedByName(), null,
                null);
    }

    @Test
    public void testGetPreviousEntity() {
        System.out.println("testGetPreviousEntity");
        doTestGetPreviousEntity(TestDataGenerator.getTestDataSortedByName(), null, TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetPreviousEntity_EmbeddedId() {
        System.out.println("testGetPreviousEntity_EmbeddedId");
        doTestGetPreviousEntity_EmbeddedId(TestDataGenerator.getTestDataEmbeddedIdSortedByName(),
                null, null);
    }

    @Test
    public void testGetEntityIdentifierAt() {
        System.out.println("testGetEntityIdentifierAt");
        doTestGetEntityIdentifierAt(TestDataGenerator.getTestDataSortedByName(), null, TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetEntityIdentifierAtBackwards() {
        System.out.println("testGetEntityIdentifierAtBackwards");
        doTestGetEntityIdentifierAtBackwards(TestDataGenerator.getTestDataSortedByName(), null,
                TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetEntityIdentifierAt_EmbeddedId() {
        System.out.println("testGetEntityIdentifierAt_EmbeddedId");
        doTestGetEntityIdentifierAt_EmbeddedId(TestDataGenerator.getTestDataEmbeddedIdSortedByName(),
                null, null);
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_EmbeddedId() {
        System.out.println("testGetEntityIdentifierAtBackwards_EmbeddedId");
        doTestGetEntityIdentifierAtBackwards_EmbeddedId(
                TestDataGenerator.getTestDataEmbeddedIdSortedByName(), null, null);
    }

    // TODO Add tests for container with duplicate sorted values

    @Test
    public void testGetFirstEntity_SortedByLastNameAndStreet() {
        System.out.println("testGetFirstEntity_SortedByLastNameAndStreet");
        doTestGetFirstEntity(TestDataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                TestDataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetNextEntity_SortedByLastNameAndStreet() {
        System.out.println("testGetNextEntity_SortedByLastNameAndStreet");
        doTestGetNextEntity(TestDataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                TestDataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetLastEntity_SortedByLastNameAndStreet() {
        System.out.println("testGetLastEntity_SortedByLastNameAndStreet");
        doTestGetLastEntity(TestDataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                TestDataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetPreviousEntity_SortedByLastNameAndStreet() {
        System.out.println("testGetPreviousEntity_SortedByLastNameAndStreet");
        doTestGetPreviousEntity(TestDataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                TestDataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetEntityIdentifierAt_SortedByLastNameAndStreet() {
        System.out
                .println("testGetEntityIdentifierAt_SortedByLastNameAndStreet");
        doTestGetEntityIdentifierAt(TestDataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                TestDataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_SortedByLastNameAndStreet() {
        System.out
                .println("testGetEntityIdentifierAtBackwards_SortedByLastNameAndStreet");
        doTestGetEntityIdentifierAtBackwards(TestDataGenerator.getTestDataSortedByLastNameAndStreet(),
                null, TestDataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetFirstEntity_SortedByPrimaryKey() {
        System.out.println("testGetFirstEntity_SortedByPrimaryKey");
        doTestGetFirstEntity(TestDataGenerator.getTestDataSortedByPrimaryKey(), null, null);
    }

    @Test
    public void testGetNextEntity_SortedByPrimaryKey() {
        System.out.println("testGetNextEntity_SortedByPrimaryKey");
        doTestGetNextEntity(TestDataGenerator.getTestDataSortedByPrimaryKey(), null, null);
    }

    @Test
    public void testGetLastEntity_SortedByPrimaryKey() {
        System.out.println("testGetLastEntity_SortedByPrimaryKey");
        doTestGetLastEntity(TestDataGenerator.getTestDataSortedByPrimaryKey(), null, null);
    }

    @Test
    public void testGetPreviousEntity_SortedByPrimaryKey() {
        System.out.println("testGetPreviousEntity_SortedByPrimaryKey");
        doTestGetPreviousEntity(TestDataGenerator.getTestDataSortedByPrimaryKey(), null, null);
    }

    @Test
    public void testGetEntityIdentifierAt_SortedByPrimaryKey() {
        System.out.println("testGetEntityIdentifierAt_SortedByPrimaryKey");
        doTestGetEntityIdentifierAt(TestDataGenerator.getTestDataSortedByPrimaryKey(), null, null);
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_SortedByPrimaryKey() {
        System.out
                .println("testGetEntityIdentifierAtBackwards_SortedByPrimaryKey");
        doTestGetEntityIdentifierAtBackwards(TestDataGenerator.getTestDataSortedByPrimaryKey(), null,
                null);
    }

    @Test
    public void testGetEntityCount_Filtered() {
        System.out.println("testGetEntityCount_Filtered");
        doTestGetEntityCount(TestDataGenerator.getFilteredTestDataSortedByName(), TestDataGenerator.getTestFilter());
    }

    @Test
    public void testGetContainsEntity_Filtered() {
        System.out.println("testGetContainsEntity_Filtered");
        doTestContainsEntity(TestDataGenerator.getFilteredTestDataSortedByName(), TestDataGenerator.getTestFilter());
    }

    @Test
    public void testGetFirstEntity_Filtered() {
        System.out.println("testGetFirstEntity_Filtered");
        doTestGetFirstEntity(TestDataGenerator.getFilteredTestDataSortedByName(), TestDataGenerator.getTestFilter(),
                TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetNextEntity_Filtered() {
        System.out.println("testGetNextEntity_Filtered");
        doTestGetNextEntity(TestDataGenerator.getFilteredTestDataSortedByName(), TestDataGenerator.getTestFilter(),
                TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetLastEntity_Filtered() {
        System.out.println("testGetLastEntity_Filtered");
        doTestGetLastEntity(TestDataGenerator.getFilteredTestDataSortedByName(), TestDataGenerator.getTestFilter(),
                TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetPreviousEntity_Filtered() {
        System.out.println("testGetPreviousEntity_Filtered");
        doTestGetPreviousEntity(TestDataGenerator.getFilteredTestDataSortedByName(), TestDataGenerator.getTestFilter(),
                TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetEntityIdentifierAt_Filtered() {
        System.out.println("testGetEntityIdentifierAt_Filtered");
        doTestGetEntityIdentifierAt(TestDataGenerator.getFilteredTestDataSortedByName(), TestDataGenerator.getTestFilter(),
                TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_Filtered() {
        System.out.println("testGetEntityIdentifierAtBackwards_Filtered");
        doTestGetEntityIdentifierAtBackwards(TestDataGenerator.getFilteredTestDataSortedByName(),
                TestDataGenerator.getTestFilter(), TestDataGenerator.getSortByName());
    }

    @Test
    public void testGetFirstEntity_Filtered_SortedByPrimaryKey() {
        System.out.println("testGetFirstEntity_Filtered_SortedByPrimaryKey");
        doTestGetFirstEntity(TestDataGenerator.getFilteredTestDataSortedByPrimaryKey(), TestDataGenerator.getTestFilter(),
                null);
    }

    @Test
    public void testGetNextEntity_Filtered_SortedByPrimaryKey() {
        System.out.println("testGetNextEntity_Filtered_SortedByPrimaryKey");
        doTestGetNextEntity(TestDataGenerator.getFilteredTestDataSortedByPrimaryKey(), TestDataGenerator.getTestFilter(),
                null);
    }

    @Test
    public void testGetLastEntity_Filtered_SortedByPrimaryKey() {
        System.out.println("testGetLastEntity_Filtered_SortedByPrimaryKey");
        doTestGetLastEntity(TestDataGenerator.getFilteredTestDataSortedByPrimaryKey(), TestDataGenerator.getTestFilter(),
                null);
    }

    @Test
    public void testGetPreviousEntity_Filtered_SortedByPrimaryKey() {
        System.out.println("testGetPreviousEntity_Filtered_SortedByPrimaryKey");
        doTestGetPreviousEntity(TestDataGenerator.getFilteredTestDataSortedByPrimaryKey(), TestDataGenerator.getTestFilter(),
                null);
    }

    @Test
    public void testGetEntityIdentifierAt_Filtered_SortedByPrimaryKey() {
        System.out
                .println("testGetEntityIdentifierAt_Filtered_SortedByPrimaryKey");
        doTestGetEntityIdentifierAt(TestDataGenerator.getFilteredTestDataSortedByPrimaryKey(),
                TestDataGenerator.getTestFilter(), null);
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_Filtered_SortedByPrimaryKey() {
        System.out
                .println("testGetEntityIdentifierAtBackwards_Filtered_SortedByPrimaryKey");
        doTestGetEntityIdentifierAtBackwards(
                TestDataGenerator.getFilteredTestDataSortedByPrimaryKey(), TestDataGenerator.getTestFilter(), null);
    }

    @Test
    public void testJoinFilter() throws Exception {
        // Save some testing data
        Random rnd = new Random();
        Map<Skill, Collection<Object>> skillPersonMap = new HashMap<Skill, Collection<Object>>();
        getEntityManager().getTransaction().begin();
        for (Skill s : TestDataGenerator.getSkills()) {
            Set<Object> persons = new HashSet<Object>();
            for (int i = 0; i < 10; i++) {
                Person p = TestDataGenerator.getTestDataSortedByPrimaryKey().get(rnd
                        .nextInt(TestDataGenerator.getTestDataSortedByPrimaryKey().size()));
                System.out.println("Skill: " + s + " Person: " + p);
                if (!persons.contains(p.getId())) {
                    persons.add(p.getId());
                    p.addSkill(s, i + 1);
                    getEntityManager().merge(p);
                }
            }
            skillPersonMap.put(s, persons);
        }
        getEntityManager().flush();
        getEntityManager().getTransaction().commit();

        // Now try out the filter
        for (Skill s : TestDataGenerator.getSkills()) {
            Filter filter = Filters
                    .joinFilter("skills", Filters.eq("skill", s));
            Collection<Object> returnedIds = entityProvider
                    .getAllEntityIdentifiers(filter, null);
            assertTrue(skillPersonMap.get(s).containsAll(returnedIds));
            assertEquals(skillPersonMap.get(s).size(), returnedIds.size());
        }
    }

    // TODO Add test for getAllEntityIdentifiers
}
