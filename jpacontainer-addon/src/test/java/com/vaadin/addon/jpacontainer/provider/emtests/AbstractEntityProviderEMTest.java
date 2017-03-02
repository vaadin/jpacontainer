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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.SortBy;
import com.vaadin.addon.jpacontainer.filter.JoinFilter;
import com.vaadin.addon.jpacontainer.testdata.DataGenerator;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.Skill;
import com.vaadin.addon.jpacontainer.util.DefaultQueryModifierDelegate;
import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.util.filter.Compare.Equal;

/**
 * Abstract test case for {@link EntityProvider} that should work with any
 * entity manager that follows the specifications. Subclasses should provide a
 * concrete entity manager implementation to test. If the test passes, the
 * entity manager implementation should work with JPAContainer.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractEntityProviderEMTest {
    
    protected static EntityContainer container = EasyMock.createNiceMock(EntityContainer.class);
    static {
        EasyMock.replay(container);
    }

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
        DataGenerator.createTestData();
        System.out.println("Setting up " + getClass());
        entityManager = createEntityManager();
        entityProvider = createEntityProvider();
        entityProvider_EmbeddedId = createEntityProvider_EmbeddedId();
        DataGenerator.persistTestData(entityManager);
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
            Person returned = entityProvider.getEntity(null, p.getId());
            assertEquals(p, returned);
            // Make sure the entities are detached
            returned.setFirstName("Different firstname");
            assertFalse(returned.getFirstName().equals(
                    entityProvider.getEntity(null, p.getId()).getFirstName()));
        }
    }

    protected void doTestGetEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData) {
        for (EmbeddedIdPerson p : testData) {
            EmbeddedIdPerson returned = entityProvider_EmbeddedId.getEntity(null, p
                    .getName());
            assertEquals(p, returned);
            // Make sure the entities are detached
            returned.getAddress().setStreet("another street");
            assertFalse(returned.equals(entityProvider_EmbeddedId.getEntity(null, p
                    .getName())));
        }
    }

    protected void doTestGetEntityCount(final List<Person> testData,
            final Filter filter) {
        assertEquals(testData.size(), entityProvider.getEntityCount(container, filter));
    }

    protected void doTestGetEntityCount_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter) {
        assertEquals(testData.size(),
                entityProvider_EmbeddedId.getEntityCount(container, filter));
    }

    protected void doTestContainsEntity(final List<Person> testData,
            final Filter filter) {
        long maxKey = 0;
        for (Person p : testData) {
            if (maxKey < p.getId()) {
                maxKey = p.getId();
            }
            assertTrue(entityProvider.containsEntity(container, p.getId(), filter));
        }
        assertFalse(entityProvider.containsEntity(container, maxKey + 1, filter));
    }

    protected void doTestContainsEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter) {
        for (EmbeddedIdPerson p : testData) {
            assertTrue(entityProvider_EmbeddedId.containsEntity(container, p.getName(),
                    filter));
        }
    }

    protected void doTestGetFirstEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        assertEquals(testData.get(0).getId(),
                entityProvider.getFirstEntityIdentifier(container, filter, sortBy));
    }

    protected void doTestGetFirstEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        assertEquals(testData.get(0).getName(),
                entityProvider_EmbeddedId.getFirstEntityIdentifier(container, filter,
                        sortBy));
    }

    protected void doTestGetNextEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        for (int i = 0; i < testData.size() - 1; i++) {
            assertEquals(testData.get(i + 1).getId(),
                    entityProvider.getNextEntityIdentifier(container, testData.get(i)
                            .getId(), filter, sortBy));
        }
        assertNull(entityProvider.getNextEntityIdentifier(
                container, testData.get(testData.size() - 1).getId(), filter, sortBy));
    }

    protected void doTestGetNextEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        for (int i = 0; i < testData.size() - 1; i++) {
            assertEquals(testData.get(i + 1).getName(),
                    entityProvider_EmbeddedId.getNextEntityIdentifier(container, testData
                            .get(i).getName(), filter, sortBy));
        }
        assertNull(entityProvider_EmbeddedId.getNextEntityIdentifier(container, testData
                .get(testData.size() - 1).getName(), filter, sortBy));
    }

    protected void doTestGetLastEntity(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        assertEquals(testData.get(testData.size() - 1).getId(),
                entityProvider.getLastEntityIdentifier(container, filter, sortBy));
    }

    protected void doTestGetLastEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        assertEquals(testData.get(testData.size() - 1).getName(),
                entityProvider_EmbeddedId.getLastEntityIdentifier(container, filter,
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
                    entityProvider.getPreviousEntityIdentifier(container, testData.get(i)
                            .getId(), filter, sortBy));
        }
        assertNull(entityProvider.getPreviousEntityIdentifier(container, testData.get(0)
                .getId(), filter, sortBy));
    }

    protected void doTestGetPreviousEntity_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        for (int i = testData.size() - 1; i > 0; i--) {
            assertEquals(testData.get(i - 1).getName(),
                    entityProvider_EmbeddedId.getPreviousEntityIdentifier(
                            container, testData.get(i).getName(), filter, sortBy));
        }
        assertNull(entityProvider_EmbeddedId.getPreviousEntityIdentifier(
                container, testData.get(0).getName(), filter, sortBy));
    }

    protected void doTestGetEntityIdentifierAt(final List<Person> testData,
            final Filter filter, final List<SortBy> sortBy) {
        for (int i = 0; i < testData.size(); i++) {
            assertEquals(testData.get(i).getId(),
                    entityProvider.getEntityIdentifierAt(container, filter, sortBy, i));
        }
        assertNull(entityProvider.getEntityIdentifierAt(container, filter, sortBy,
                testData.size()));
    }

    protected void doTestGetEntityIdentifierAtBackwards(
            final List<Person> testData, final Filter filter,
            final List<SortBy> sortBy) {
        assertNull(entityProvider.getEntityIdentifierAt(container, filter, sortBy,
                testData.size()));
        for (int i = testData.size() - 1; i >= 0; i--) {
            // System.out.println("testData[" + i + "] = " +
            // testData.get(i).getId());
            // System.out.println("  result = " +
            // entityProvider.getEntityIdentifierAt(filter, sortBy, i));
            assertEquals(testData.get(i).getId(),
                    entityProvider.getEntityIdentifierAt(container, filter, sortBy, i));
        }
    }

    protected void doTestGetEntityIdentifierAt_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        for (int i = 0; i < testData.size(); i++) {
            assertEquals(testData.get(i).getName(),
                    entityProvider_EmbeddedId.getEntityIdentifierAt(container, filter,
                            sortBy, i));
        }
        assertNull(entityProvider_EmbeddedId.getEntityIdentifierAt(container, filter,
                sortBy, testData.size()));
    }

    protected void doTestGetEntityIdentifierAtBackwards_EmbeddedId(
            final List<EmbeddedIdPerson> testData, final Filter filter,
            final List<SortBy> sortBy) {
        for (int i = testData.size() - 1; i >= 0; i--) {
            assertEquals(testData.get(i).getName(),
                    entityProvider_EmbeddedId.getEntityIdentifierAt(container, filter,
                            sortBy, i));
        }
        assertNull(entityProvider_EmbeddedId.getEntityIdentifierAt(container, filter,
                sortBy, testData.size()));
    }

    @Test
    public void testGetEntity() {
        System.out.println("testGetEntity");
        doTestGetEntity(DataGenerator.getTestDataSortedByName());
    }

    @Test
    public void testGetEntity_EmbeddedId() {
        System.out.println("testGetEntity_EmbeddedId");
        doTestGetEntity_EmbeddedId(DataGenerator
                .getTestDataEmbeddedIdSortedByName());
    }

    @Test
    public void testGetEntityCount() {
        System.out.println("testGetEntityCount");
        doTestGetEntityCount(DataGenerator.getTestDataSortedByName(), null);
    }

    @Test
    public void testGetEntityCount_EmbeddedId() {
        System.out.println("testGetEntityCount_EmbeddedId");
        doTestGetEntityCount_EmbeddedId(
                DataGenerator.getTestDataEmbeddedIdSortedByName(), null);
    }

    @Test
    public void testContainsEntity() {
        System.out.println("testContainsEntity");
        doTestContainsEntity(DataGenerator.getTestDataSortedByName(), null);
    }

    @Test
    public void testContainsEntity_EmbeddedId() {
        System.out.println("testContainsEntity_EmbeddedId");
        doTestContainsEntity_EmbeddedId(
                DataGenerator.getTestDataEmbeddedIdSortedByName(), null);
    }

    @Test
    public void testGetFirstEntity() {
        System.out.println("testGetFirstEntity");
        doTestGetFirstEntity(DataGenerator.getTestDataSortedByName(), null,
                DataGenerator.getSortByName());
    }

    @Test
    public void testGetFirstEntity_EmbeddedId() {
        System.out.println("testGetFirstEntity_EmbeddedId");
        doTestGetFirstEntity_EmbeddedId(
                DataGenerator.getTestDataEmbeddedIdSortedByName(), null, null);
    }

    @Test
    public void testGetNextEntity() {
        System.out.println("testGetNextEntity");
        doTestGetNextEntity(DataGenerator.getTestDataSortedByName(), null,
                DataGenerator.getSortByName());
    }

    @Test
    public void testGetNextEntity_EmbeddedId() {
        System.out.println("testGetNextEntity_EmbeddedId");
        doTestGetNextEntity_EmbeddedId(
                DataGenerator.getTestDataEmbeddedIdSortedByName(), null, null);
    }

    @Test
    public void testGetLastEntity() {
        System.out.println("testGetLastEntity");
        doTestGetLastEntity(DataGenerator.getTestDataSortedByName(), null,
                DataGenerator.getSortByName());
    }

    @Test
    public void testGetLastEntity_EmbeddedId() {
        System.out.println("testGetLastEntity_EmbeddedID");
        doTestGetLastEntity_EmbeddedId(
                DataGenerator.getTestDataEmbeddedIdSortedByName(), null, null);
    }

    @Test
    public void testGetPreviousEntity() {
        System.out.println("testGetPreviousEntity");
        doTestGetPreviousEntity(DataGenerator.getTestDataSortedByName(), null,
                DataGenerator.getSortByName());
    }

    @Test
    public void testGetPreviousEntity_EmbeddedId() {
        System.out.println("testGetPreviousEntity_EmbeddedId");
        doTestGetPreviousEntity_EmbeddedId(
                DataGenerator.getTestDataEmbeddedIdSortedByName(), null, null);
    }

    @Test
    public void testGetEntityIdentifierAt() {
        System.out.println("testGetEntityIdentifierAt");
        doTestGetEntityIdentifierAt(DataGenerator.getTestDataSortedByName(),
                null, DataGenerator.getSortByName());
    }

    @Test
    public void testGetEntityIdentifierAtBackwards() {
        System.out.println("testGetEntityIdentifierAtBackwards");
        doTestGetEntityIdentifierAtBackwards(
                DataGenerator.getTestDataSortedByName(), null,
                DataGenerator.getSortByName());
    }

    @Test
    public void testGetEntityIdentifierAt_EmbeddedId() {
        System.out.println("testGetEntityIdentifierAt_EmbeddedId");
        doTestGetEntityIdentifierAt_EmbeddedId(
                DataGenerator.getTestDataEmbeddedIdSortedByName(), null, null);
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_EmbeddedId() {
        System.out.println("testGetEntityIdentifierAtBackwards_EmbeddedId");
        doTestGetEntityIdentifierAtBackwards_EmbeddedId(
                DataGenerator.getTestDataEmbeddedIdSortedByName(), null, null);
    }

    // TODO Add tests for container with duplicate sorted values

    @Test
    public void testGetFirstEntity_SortedByLastNameAndStreet() {
        System.out.println("testGetFirstEntity_SortedByLastNameAndStreet");
        doTestGetFirstEntity(
                DataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                DataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetNextEntity_SortedByLastNameAndStreet() {
        System.out.println("testGetNextEntity_SortedByLastNameAndStreet");
        doTestGetNextEntity(
                DataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                DataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetLastEntity_SortedByLastNameAndStreet() {
        System.out.println("testGetLastEntity_SortedByLastNameAndStreet");
        doTestGetLastEntity(
                DataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                DataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetPreviousEntity_SortedByLastNameAndStreet() {
        System.out.println("testGetPreviousEntity_SortedByLastNameAndStreet");
        doTestGetPreviousEntity(
                DataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                DataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetEntityIdentifierAt_SortedByLastNameAndStreet() {
        System.out
                .println("testGetEntityIdentifierAt_SortedByLastNameAndStreet");
        doTestGetEntityIdentifierAt(
                DataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                DataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_SortedByLastNameAndStreet() {
        System.out
                .println("testGetEntityIdentifierAtBackwards_SortedByLastNameAndStreet");
        doTestGetEntityIdentifierAtBackwards(
                DataGenerator.getTestDataSortedByLastNameAndStreet(), null,
                DataGenerator.getSortByLastNameAndStreet());
    }

    @Test
    public void testGetFirstEntity_SortedByPrimaryKey() {
        System.out.println("testGetFirstEntity_SortedByPrimaryKey");
        doTestGetFirstEntity(DataGenerator.getTestDataSortedByPrimaryKey(),
                null, null);
    }

    @Test
    public void testGetNextEntity_SortedByPrimaryKey() {
        System.out.println("testGetNextEntity_SortedByPrimaryKey");
        doTestGetNextEntity(DataGenerator.getTestDataSortedByPrimaryKey(),
                null, null);
    }

    @Test
    public void testGetLastEntity_SortedByPrimaryKey() {
        System.out.println("testGetLastEntity_SortedByPrimaryKey");
        doTestGetLastEntity(DataGenerator.getTestDataSortedByPrimaryKey(),
                null, null);
    }

    @Test
    public void testGetPreviousEntity_SortedByPrimaryKey() {
        System.out.println("testGetPreviousEntity_SortedByPrimaryKey");
        doTestGetPreviousEntity(DataGenerator.getTestDataSortedByPrimaryKey(),
                null, null);
    }

    @Test
    public void testGetEntityIdentifierAt_SortedByPrimaryKey() {
        System.out.println("testGetEntityIdentifierAt_SortedByPrimaryKey");
        doTestGetEntityIdentifierAt(
                DataGenerator.getTestDataSortedByPrimaryKey(), null, null);
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_SortedByPrimaryKey() {
        System.out
                .println("testGetEntityIdentifierAtBackwards_SortedByPrimaryKey");
        doTestGetEntityIdentifierAtBackwards(
                DataGenerator.getTestDataSortedByPrimaryKey(), null, null);
    }

    @Test
    public void testGetEntityCount_Filtered() {
        System.out.println("testGetEntityCount_Filtered");
        doTestGetEntityCount(DataGenerator.getFilteredTestDataSortedByName(),
                DataGenerator.getTestFilter());
    }

    @Test
    public void testGetContainsEntity_Filtered() {
        System.out.println("testGetContainsEntity_Filtered");
        doTestContainsEntity(DataGenerator.getFilteredTestDataSortedByName(),
                DataGenerator.getTestFilter());
    }

    @Test
    public void testGetFirstEntity_Filtered() {
        System.out.println("testGetFirstEntity_Filtered");
        doTestGetFirstEntity(DataGenerator.getFilteredTestDataSortedByName(),
                DataGenerator.getTestFilter(), DataGenerator.getSortByName());
    }

    @Test
    public void testGetNextEntity_Filtered() {
        System.out.println("testGetNextEntity_Filtered");
        doTestGetNextEntity(DataGenerator.getFilteredTestDataSortedByName(),
                DataGenerator.getTestFilter(), DataGenerator.getSortByName());
    }

    @Test
    public void testGetLastEntity_Filtered() {
        System.out.println("testGetLastEntity_Filtered");
        doTestGetLastEntity(DataGenerator.getFilteredTestDataSortedByName(),
                DataGenerator.getTestFilter(), DataGenerator.getSortByName());
    }

    @Test
    public void testGetPreviousEntity_Filtered() {
        System.out.println("testGetPreviousEntity_Filtered");
        doTestGetPreviousEntity(
                DataGenerator.getFilteredTestDataSortedByName(),
                DataGenerator.getTestFilter(), DataGenerator.getSortByName());
    }

    @Test
    public void testGetEntityIdentifierAt_Filtered() {
        System.out.println("testGetEntityIdentifierAt_Filtered");
        doTestGetEntityIdentifierAt(
                DataGenerator.getFilteredTestDataSortedByName(),
                DataGenerator.getTestFilter(), DataGenerator.getSortByName());
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_Filtered() {
        System.out.println("testGetEntityIdentifierAtBackwards_Filtered");
        doTestGetEntityIdentifierAtBackwards(
                DataGenerator.getFilteredTestDataSortedByName(),
                DataGenerator.getTestFilter(), DataGenerator.getSortByName());
    }

    @Test
    public void testGetFirstEntity_Filtered_SortedByPrimaryKey() {
        System.out.println("testGetFirstEntity_Filtered_SortedByPrimaryKey");
        doTestGetFirstEntity(
                DataGenerator.getFilteredTestDataSortedByPrimaryKey(),
                DataGenerator.getTestFilter(), null);
    }

    @Test
    public void testGetNextEntity_Filtered_SortedByPrimaryKey() {
        System.out.println("testGetNextEntity_Filtered_SortedByPrimaryKey");
        doTestGetNextEntity(
                DataGenerator.getFilteredTestDataSortedByPrimaryKey(),
                DataGenerator.getTestFilter(), null);
    }

    @Test
    public void testGetLastEntity_Filtered_SortedByPrimaryKey() {
        System.out.println("testGetLastEntity_Filtered_SortedByPrimaryKey");
        doTestGetLastEntity(
                DataGenerator.getFilteredTestDataSortedByPrimaryKey(),
                DataGenerator.getTestFilter(), null);
    }

    @Test
    public void testGetPreviousEntity_Filtered_SortedByPrimaryKey() {
        System.out.println("testGetPreviousEntity_Filtered_SortedByPrimaryKey");
        doTestGetPreviousEntity(
                DataGenerator.getFilteredTestDataSortedByPrimaryKey(),
                DataGenerator.getTestFilter(), null);
    }

    @Test
    public void testGetEntityIdentifierAt_Filtered_SortedByPrimaryKey() {
        System.out
                .println("testGetEntityIdentifierAt_Filtered_SortedByPrimaryKey");
        doTestGetEntityIdentifierAt(
                DataGenerator.getFilteredTestDataSortedByPrimaryKey(),
                DataGenerator.getTestFilter(), null);
    }

    @Test
    public void testGetEntityIdentifierAtBackwards_Filtered_SortedByPrimaryKey() {
        System.out
                .println("testGetEntityIdentifierAtBackwards_Filtered_SortedByPrimaryKey");
        doTestGetEntityIdentifierAtBackwards(
                DataGenerator.getFilteredTestDataSortedByPrimaryKey(),
                DataGenerator.getTestFilter(), null);
    }

    @Test
    public void testJoin() throws Exception {
        // Save some testing data
        Random rnd = new Random();
        Map<Skill, Collection<Object>> skillPersonMap = new HashMap<Skill, Collection<Object>>();
        getEntityManager().getTransaction().begin();
        for (Skill s : DataGenerator.getSkills()) {
            Set<Object> persons = new HashSet<Object>();
            for (int i = 0; i < 10; i++) {
                Person p = DataGenerator.getTestDataSortedByPrimaryKey().get(
                        rnd.nextInt(DataGenerator
                                .getTestDataSortedByPrimaryKey().size()));
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
        for (final Skill s : DataGenerator.getSkills()) {
            entityProvider
                    .setQueryModifierDelegate(new DefaultQueryModifierDelegate() {
                        private Join<Object, Object> skillsJoin;

                        @Override
                        public void queryWillBeBuilt(
                                CriteriaBuilder criteriaBuilder,
                                CriteriaQuery<?> query) {
                            Root<?> root = query.getRoots().iterator().next();
                            skillsJoin = root.join("skills", JoinType.LEFT);
                        }

                        @Override
                        public void filtersWillBeAdded(
                                CriteriaBuilder criteriaBuilder,
                                CriteriaQuery<?> query,
                                List<Predicate> predicates) {
                            System.out.println(s.getSkillName());
                            predicates.add(criteriaBuilder.equal(
                                    skillsJoin.get("skill"),
                                    criteriaBuilder.literal(s)));
                        }

                    });
            Collection<Object> returnedIds = entityProvider
                    .getAllEntityIdentifiers(null, null, null);
            System.out.println(returnedIds);
            assertTrue(skillPersonMap.get(s).containsAll(returnedIds));
            assertEquals(skillPersonMap.get(s).size(), returnedIds.size());
            entityProvider.setQueryModifierDelegate(null);
        }

        entityProvider.setQueryModifierDelegate(null);
    }

    @Test
    public void testJoinFilter() throws Exception {
        // Save some testing data
        Random rnd = new Random();
        Map<Skill, Collection<Object>> skillPersonMap = new HashMap<Skill, Collection<Object>>();
        getEntityManager().getTransaction().begin();
        for (Skill s : DataGenerator.getSkills()) {
            Set<Object> persons = new HashSet<Object>();
            for (int i = 0; i < 10; i++) {
                Person p = DataGenerator.getTestDataSortedByPrimaryKey().get(
                        rnd.nextInt(DataGenerator
                                .getTestDataSortedByPrimaryKey().size()));
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
        for (Skill s : DataGenerator.getSkills()) {
            Collection<Object> returnedIds = entityProvider
                    .getAllEntityIdentifiers(container, new JoinFilter("skills",
                            new Equal("skill", s)), null);
            System.out.println(returnedIds);
            assertTrue(skillPersonMap.get(s).containsAll(returnedIds));
            assertEquals(skillPersonMap.get(s).size(), returnedIds.size());
            entityProvider.setQueryModifierDelegate(null);
        }

        entityProvider.setQueryModifierDelegate(null);
    }

    // TODO Add test for getAllEntityIdentifiers
}
