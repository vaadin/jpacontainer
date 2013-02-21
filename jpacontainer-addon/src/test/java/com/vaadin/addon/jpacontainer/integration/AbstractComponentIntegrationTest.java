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
package com.vaadin.addon.jpacontainer.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transaction;

import com.vaadin.addon.jpacontainer.BatchableEntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;
import com.vaadin.data.Buffered;
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.MultiSelectConverter;
import com.vaadin.addon.jpacontainer.fieldfactory.SingleSelectConverter;
import com.vaadin.addon.jpacontainer.provider.LocalEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.BeanWithLogic;
import com.vaadin.addon.jpacontainer.testdata.DataGenerator;
import com.vaadin.addon.jpacontainer.testdata.Department;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.server.PaintException;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;

public abstract class AbstractComponentIntegrationTest extends
        AbstractIntegrationTest {

    private EntityManagerFactory emf;

    public abstract EntityManagerFactory getTestFactory(String dburl);

    public AbstractComponentIntegrationTest() throws IOException {
        setEmf(getTestFactory(AbstractIntegrationTest.getDatabaseUrl()));
    }

    protected AbstractComponentIntegrationTest(EntityManagerFactory emf) {
        setEmf(emf);
    }

    private void setEmf(EntityManagerFactory testFactory) {
        emf = testFactory;
    }

    @Override
    protected EntityManagerFactory createEntityManagerFactory()
            throws IOException {
        return emf;
    }

    @Test
    public void testTableRendering() throws PaintException {
        JPAContainer<Person> personContainer = getPersonContainer();

        Table table = new Table("Person list", personContainer);

        Layout dl = createDummyLayout();
        dl.addComponent(table);
        // dl.getWindow().paint(getFakePaintTarget());

        Item item = table.getItem(table.firstItemId());
        Property fnp = item.getItemProperty("firstName");
        Person person = DataGenerator.getTestDataSortedByPrimaryKey().get(0);
        assertEquals(person.getFirstName(), fnp.getValue());

    }

    @Test
    public void testReferenceTypeInComboBox() {
        final JPAContainer<Person> personContainer = getPersonContainer();

        EntityItem<Person> item = personContainer.getItem(personContainer
                .firstItemId());

        /*
         * Prepeare a combobox to be used to edit manager (reference to another
         * Person)
         */
        final ComboBox comboBox = new ComboBox();
        comboBox.setContainerDataSource(personContainer);
        comboBox.setCaption("Manager");
        comboBox.setConverter(new SingleSelectConverter<Person>(comboBox));
        // comboBox.setPropertyDataSource(new SingleSelectTranslator(comboBox));

        Form form = new Form();

        form.setFormFieldFactory(new FormFieldFactory() {
            public Field createField(Item item, Object propertyId,
                    Component uiContext) {
                if (propertyId.equals("manager")) {
                    return comboBox;
                }
                return DefaultFieldFactory.get().createField(item, propertyId,
                        uiContext);
            }
        });

        form.setItemDataSource(item, Arrays.asList("manager", "firstName"));

        createDummyLayout().addComponent(form);

        Object value = comboBox.getValue();
        // by default the manager should not be unassigned
        assertNull(value);

        Object nextItemId = personContainer.nextItemId(item.getItemId());
        EntityItem<Person> item2 = personContainer.getItem(nextItemId);

        // now assign next person (id) as a value for the combobox
        comboBox.setValue(nextItemId);

        Object propetyValue = item.getItemProperty("manager").getValue();

        // ensure property value match
        assertEquals(item2.getEntity(), propetyValue);

        // ensure the first persons manager is the second person at bean level
        assertEquals(item2.getEntity(), item.getEntity().getManager());

    }

    @Test
    public void testCollectionTypeInListSelectWithMultiSelectTranslator()
            throws IOException {
        JPAContainer<Person> personContainer = getPersonContainer();
        Object personId = personContainer.firstItemId();
        Person person = personContainer.getItem(personId).getEntity();

        // TODO move creation of test data to DataGenerator
        EntityManager entityManager = getEntityManager();
        Department department = null;
        try {
            department = (Department) entityManager.createQuery(
                    "SELECT d from Department d where d.name='FOO'")
                    .getSingleResult();
        } catch (Exception e) {
        }
        if (department == null) {
            entityManager.getTransaction().begin();

            department = new Department();
            department.setName("FOO");

            Set<Person> persons = new HashSet<Person>();
            persons.add(person);
            department.setPersons(persons);

            entityManager.persist(department);

            Department emptyDepartment = new Department();
            emptyDepartment.setName("BAR");

            entityManager.persist(emptyDepartment);

            entityManager.getTransaction().commit();

        }
        JPAContainer<Department> departmentContainer = getDepartmentContainer();

        Object firstItemId = departmentContainer.firstItemId();
        EntityItem<Department> item = departmentContainer.getItem(firstItemId);

        ListSelect listSelect = new ListSelect("foo", personContainer);
        listSelect.setMultiSelect(true);
        listSelect.setPropertyDataSource(item.getItemProperty("persons"));
        listSelect.setConverter(new MultiSelectConverter(listSelect));

        Set<Object> value = (Set<Object>) listSelect.getValue();
        boolean containsPersonId = value.contains(personId);
        assertFalse(!containsPersonId);
        assertEquals(1, value.size());

        Object secondPersonId = personContainer.nextItemId(personId);
        Person secondPerson = personContainer.getItem(secondPersonId)
                .getEntity();
        listSelect.select(secondPersonId);

        value = (Set<Object>) listSelect.getValue();
        containsPersonId = value.contains(personId);
        assert (containsPersonId);
        containsPersonId = value.contains(secondPersonId);
        assert (containsPersonId);
        assertEquals(2, value.size());

        Set<Person> persons2 = item.getEntity().getPersons();
        assertEquals(2, persons2.size());
        assert (persons2.contains(person));
        assert (persons2.contains(secondPerson));

        // Now test with initially empty department
        Object nextItemId = departmentContainer.nextItemId(firstItemId);
        EntityItem<Department> item2 = departmentContainer.getItem(nextItemId);

        Department department2 = item2.getEntity();
        listSelect.setPropertyDataSource(item2.getItemProperty("persons"));

        listSelect.select(personId);

        assertNotNull(department2);
        assertNotNull(department2.getPersons());
        assertEquals(1, department2.getPersons().size());
        assertFalse(!department.getPersons().contains(person));

    }

    protected JPAContainer<Department> getDepartmentContainer()
            throws IOException {
        return JPAContainerFactory.make(Department.class, getEntityManager());
    }

    @Test
    public void testValueChangeEventsFromEntityProperty() {
        JPAContainer<Person> container = getPersonContainer();
        Object firstItemId = container.firstItemId();
        EntityItem<Person> item = container.getItem(firstItemId);
        Person entity = item.getEntity();

        String initialFirstNameValue = entity.getFirstName();

        EntityItemProperty property = item.getItemProperty("firstName");
        final int[] valueChangeCalls = new int[] { 0 };
        Label label = new Label(property) {
            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                valueChangeCalls[0] = valueChangeCalls[0] + 1;
                super.valueChange(event);
            }
        };

        createDummyLayout().addComponent(label);

        assertEquals(entity.getFirstName(), label.getValue());

        property.setValue("foo");

        // FIXME due to bug we get double value change events
        assertEquals(2, valueChangeCalls[0]);

        assertEquals(entity.getFirstName(), label.getValue());

        entity.setFirstName("bar");

        /*
         * Although event listener was not fired, the value should be equal as
         * we have read throught mode on
         */
        assertEquals(2, valueChangeCalls[0]);
        assertEquals(entity.getFirstName(), label.getValue());

        /*
         * Refresh the value from DB ("foo" is set to property)
         */
        item.refresh();
        assertEquals(3, valueChangeCalls[0]);
        assertEquals("foo", label.getValue());

        /*
         * Save changes to DB so that Container don't know about that. TODO this
         * should be done with jdbc
         */
        LocalEntityProvider<Person> entityProvider = (LocalEntityProvider<Person>) container
                .getEntityProvider();
        EntityManager em = entityProvider.getEntityManager();
        entity = em.find(Person.class, entity.getId());

        entity.setFirstName("bar");

        em.merge(entity);

        boolean shouldNotMatch = entity.getFirstName().equals(label.getValue());
        assertFalse(shouldNotMatch);
        assertEquals(3, valueChangeCalls[0]);

        item.refresh();
        assertEquals(4, valueChangeCalls[0]);
        assertEquals(entity.getFirstName(), label.getValue());

        /*
         * Then same test with container level api
         */
        entity = em.find(Person.class, entity.getId());

        entity.setFirstName("bar3");

        em.merge(entity);

        shouldNotMatch = entity.getFirstName().equals(label.getValue());
        assertFalse(shouldNotMatch);
        assertEquals(4, valueChangeCalls[0]);

        container.refreshItem(entity.getId());
        assertEquals(5, valueChangeCalls[0]);
        assertEquals(entity.getFirstName(), label.getValue());
    }

    @Ignore(value = "Enable when fixing the double value change event issue")
    @Test
    public void testShouldNotFireDoubleValueChangeEvents() {
        JPAContainer<Person> container = getPersonContainer();
        Object firstItemId = container.firstItemId();
        EntityItem<Person> item = container.getItem(firstItemId);

        EntityItemProperty property = item.getItemProperty("firstName");
        final int[] valueChangeCalls = new int[] { 0 };
        new Label(property) {
            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                valueChangeCalls[0] = valueChangeCalls[0] + 1;
                super.valueChange(event);
            }
        };

        property.setValue("foo");

        assertEquals(1, valueChangeCalls[0]);
    }

    @Test
    public void testNullFilter() {
        JPAContainer<Person> container = getPersonContainer();
        container.addContainerFilter(new Equal("firstName", null));
        Object firstItemId = container.firstItemId();
        assertNull(firstItemId);
    }

    @Test
    public void ensureSetterLogicIsUsed() throws IOException {
        JPAContainer<BeanWithLogic> container = JPAContainerFactory.make(
                BeanWithLogic.class, getEntityManager());
        BeanWithLogic beanWithLogic = new BeanWithLogic();
        Object id = container.addEntity(beanWithLogic);

        EntityItem<BeanWithLogic> item = container.getItem(id);

        EntityItemProperty itemProperty = item.getItemProperty("name");

        itemProperty.setValue("fin"); // setter method should uppercase the
                                      // string

        Object valueFromProperty = itemProperty.getValue();
        assertEquals("FIN", valueFromProperty);

        BeanWithLogic beanFromEntityManager = getEntityManager().find(
                BeanWithLogic.class, id);
        assertEquals("FIN", beanFromEntityManager.getName());

    }

    @Test
    public void testRefreshEntityWithCachingProvider() {

        JPAContainer<Person> personContainer = getPersonContainer();
        Object itemId = personContainer.firstItemId();
        EntityItem<Person> item = personContainer.getItem(itemId);

        EntityManager entityManager = personContainer.getEntityProvider()
                .getEntityManager();

        Person p = entityManager.find(Person.class, itemId);

        final String firstNameChangedValue = "Foo1";
        p.setFirstName(firstNameChangedValue);
        entityManager.getTransaction().begin();
        entityManager.persist(p);
        entityManager.getTransaction().commit();

        Object fn = item.getItemProperty("firstName").getValue();

        boolean same = fn.equals(firstNameChangedValue);
        assertFalse("New value although should still have cached", same);

        EntityItem<Person> item2 = personContainer.getItem(itemId);

        fn = item2.getItemProperty("firstName").getValue();
        boolean same2 = fn.equals(firstNameChangedValue);
        // This is still fine due to caching. If somebody caching logig further
        // it is ok to remove or invert this test.
        // commented out as Hbn actually returns new value here
        // assertFalse("New value although should still have cached", same2);

        personContainer.refreshItem(itemId);

        // now get a new item, should not hit chache. This used to be broken
        EntityItem<Person> item3 = personContainer.getItem(itemId);

        Object value = item.getItemProperty("firstName").getValue();
        Object value2 = item2.getItemProperty("firstName").getValue();
        Object value3 = item3.getItemProperty("firstName").getValue();

        // now all should have the new value
        assertEquals(firstNameChangedValue, value);
        assertEquals(firstNameChangedValue, value2);
        assertEquals(firstNameChangedValue, value3);

    }

    @Test
    public void testRemoveWithTwoRefreshCalls() {
        JPAContainer<Person> personContainer = getPersonContainer();

        Object firstItemId = personContainer.firstItemId();
        EntityItem<Person> item = personContainer.getItem(firstItemId);

        personContainer.removeItem(firstItemId);

        personContainer.refresh();
        personContainer.refresh();

    }

    @Test
    public void testConcurrentModification_buffered_shouldNotPersistSecondEdit() throws IOException {
        JPAContainer<Person> container1 = JPAContainerFactory.makeBatchable(Person.class, getEntityManager());
        JPAContainer<Person> container2 = JPAContainerFactory.makeBatchable(Person.class, getEntityManager());
        container1.setBuffered(true);
        container2.setBuffered(true);
        EntityItem<Person> person1 = container1.getItem(container1.firstItemId());
        EntityItem<Person> person2 = container2.getItem(container2.firstItemId());

        person1.getItemProperty("firstName").setValue("First edit");
        person1.getItemProperty("lastName").setValue("First edit");

        person2.getItemProperty("firstName").setValue("Second edit");
        person2.getItemProperty("lastName").setValue("Second edit");

        // First store
        container1.commit();
        // Second store
        try {
            container2.commit();
        } catch (Buffered.SourceException e) {
            // expected optimistic lock
            assertEquals(OptimisticLockException.class, e.getCause().getClass());
        }

        JPAContainer<Person> container3 = getPersonContainer();
        EntityItem<Person> person3 = container3.getItem(container3.firstItemId());

        assertEquals("First edit", person3.getItemProperty("firstName").getValue());
        assertEquals("First edit", person3.getItemProperty("lastName").getValue());
    }

    @Test
    public void testConcurrentModification_unbuffered_shouldPersistSecondEdit() throws IOException {
        JPAContainer<Person> container1 = JPAContainerFactory.makeBatchable(Person.class, getEntityManager());
        JPAContainer<Person> container2 = JPAContainerFactory.makeBatchable(Person.class, getEntityManager());
        container1.setBuffered(false);
        container2.setBuffered(false);

        EntityItem<Person> person1 = container1.getItem(container1.firstItemId());
        EntityItem<Person> person2 = container2.getItem(container2.firstItemId());

        person1.getItemProperty("firstName").setValue("First edit");
        person1.getItemProperty("lastName").setValue("First edit");

        person2.getItemProperty("firstName").setValue("Second edit");
        person2.getItemProperty("lastName").setValue("Second edit");

        JPAContainer<Person> container3 = getPersonContainer();
        EntityItem<Person> person3 = container3.getItem(container3.firstItemId());

        assertEquals("Second edit", person3.getItemProperty("firstName").getValue());
        assertEquals("Second edit", person3.getItemProperty("lastName").getValue());
    }

    private void runInTransaction(EntityManager em, Runnable runnable) {
        try {
            em.getTransaction().begin();
            runnable.run();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    /* This test simulates what happens if you create an entity provider that handles transactions with EJB annotations (overrides runInTransaction and adds @TransactionRequired) */
    @Test
    public void testConcurrentModification_unbufferedEntitiesNotDetachedManualTransactions_shouldPersistSecondEdit() throws IOException {
        JPAContainer<Person> container1 = JPAContainerFactory.makeBatchable(Person.class, getEntityManager());
        JPAContainer<Person> container2 = JPAContainerFactory.makeBatchable(Person.class, getEntityManager());
        container1.setBuffered(false);
        container2.setBuffered(false);

        container1.getEntityProvider().setEntitiesDetached(false);
        container2.getEntityProvider().setEntitiesDetached(false);
        ((MutableLocalEntityProvider<Person>)container1.getEntityProvider()).setTransactionsHandledByProvider(false);
        ((MutableLocalEntityProvider<Person>)container2.getEntityProvider()).setTransactionsHandledByProvider(false);

        final EntityItem<Person> person1 = container1.getItem(container1.firstItemId());
        final EntityItem<Person> person2 = container2.getItem(container2.firstItemId());

        runInTransaction(container1.getEntityProvider().getEntityManager(), new Runnable() {
            @Override
            public void run() {
                person1.getItemProperty("firstName").setValue("First edit");
            }
        });
        runInTransaction(container1.getEntityProvider().getEntityManager(), new Runnable() {
            @Override
            public void run() {
                person1.getItemProperty("lastName").setValue("First edit");
            }
        });

        runInTransaction(container2.getEntityProvider().getEntityManager(), new Runnable() {
            @Override
            public void run() {
                person2.getItemProperty("firstName").setValue("Second edit");
            }
        });
        runInTransaction(container2.getEntityProvider().getEntityManager(), new Runnable() {
            @Override
            public void run() {
                person2.getItemProperty("lastName").setValue("Second edit");
            }
        });

        JPAContainer<Person> container3 = getPersonContainer();
        EntityItem<Person> person3 = container3.getItem(container3.firstItemId());

        assertEquals("Second edit", person3.getItemProperty("firstName").getValue());
        assertEquals("Second edit", person3.getItemProperty("lastName").getValue());
    }

}
