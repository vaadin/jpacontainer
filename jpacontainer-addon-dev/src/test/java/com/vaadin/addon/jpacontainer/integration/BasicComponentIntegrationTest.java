package com.vaadin.addon.jpacontainer.integration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.MutableEntityProvider;
import com.vaadin.addon.jpacontainer.integration.eclipselink.EclipselinkTestHelper;
import com.vaadin.addon.jpacontainer.provider.LocalEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.DataGenerator;
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
import com.vaadin.ui.Table;

/**
 * TODO run these tests also on Hibernate
 * 
 */
public class BasicComponentIntegrationTest extends AbstractIntegrationTest {

    private EntityManagerFactory emf2;

    public BasicComponentIntegrationTest() throws IOException {
        setEmf(EclipselinkTestHelper.getTestFactory(AbstractIntegrationTest
                .getDatabaseUrl()));
    }

    protected BasicComponentIntegrationTest(EntityManagerFactory emf) {
        setEmf(emf);
    }

    private void setEmf(EntityManagerFactory testFactory) {
        this.emf2 = testFactory;
    }

    @Override
    protected EntityManagerFactory createEntityManagerFactory()
            throws IOException {
        return emf2;
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
        Person person = DataGenerator.getTestDataSortedByPrimaryKey()
                .get(0);
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
    public void testValueChangeEventsFromEntityProperty() {
        JPAContainer<Person> container = getPersonContainer();
        Object firstItemId = container.firstItemId();
        EntityItem<Person> item = container.getItem(firstItemId);
        Person entity = item.getEntity();
        
        String initialFirstNameValue = entity.getFirstName();

        EntityItemProperty property = item.getItemProperty("firstName");
        final int[] valueChangeCalls = new int[] {0};
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
         * Although event listener was not fired, the value should be equal as we have read throught mode on
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
         * Save changes to DB so that Container don't know about that.
         * TODO this should be done with jdbc
         */
        LocalEntityProvider<Person> entityProvider = (LocalEntityProvider<Person>) container.getEntityProvider();
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

}
