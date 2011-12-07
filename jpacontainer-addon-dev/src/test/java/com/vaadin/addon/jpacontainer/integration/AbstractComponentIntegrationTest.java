package com.vaadin.addon.jpacontainer.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.provider.LocalEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.DataGenerator;
import com.vaadin.addon.jpacontainer.testdata.Department;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.util.MultiSelectTranslator;
import com.vaadin.addon.jpacontainer.util.SingleSelectTranslator;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.PaintException;
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
        dl.getWindow().paint(getFakePaintTarget());

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
        comboBox.setPropertyDataSource(new SingleSelectTranslator(comboBox));

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

        JPAContainer<Department> departmentContainer = JPAContainerFactory
                .make(Department.class, entityManager);

        Object firstItemId = departmentContainer.firstItemId();
        EntityItem<Department> item = departmentContainer.getItem(firstItemId);

        ListSelect listSelect = new ListSelect("foo", personContainer);
        listSelect.setMultiSelect(true);
        MultiSelectTranslator wrapperProperty = new MultiSelectTranslator(
                listSelect);
        wrapperProperty.setPropertyDataSource(item.getItemProperty("persons"));
        listSelect.setPropertyDataSource(wrapperProperty);

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
        wrapperProperty.setPropertyDataSource(item2.getItemProperty("persons"));

        listSelect.select(personId);

        assertEquals(1, department2.getPersons().size());
        assertFalse(!department.getPersons().contains(person));

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
        item.refreshEntity();
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

        item.refreshEntity();
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

        container.refreshEntity(entity.getId());
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
}
