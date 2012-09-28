package com.vaadin.addon.jpacontainer.itest.addressbook;

import org.vaadin.addon.customfield.CustomField;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherUI;
import com.vaadin.addon.jpacontainer.itest.domain.Department;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;

/**
 * A custom field that allows selection of a department.
 */
public class DepartmentSelector extends CustomField {
    private ComboBox geographicalDepartment = new ComboBox();
    private ComboBox department = new ComboBox();

    private JPAContainer<Department> container;
    private JPAContainer<Department> geoContainer;

    public DepartmentSelector() {
        container = JPAContainerFactory.make(Department.class,
                TestLauncherUI.PERSISTENCE_UNIT);
        geoContainer = JPAContainerFactory.make(Department.class,
                TestLauncherUI.PERSISTENCE_UNIT);
        setCaption("Department");
        // Only list "roots" which are in our example geographical super
        // departments
        geoContainer.addContainerFilter(new IsNull("parent"));
        geographicalDepartment.setContainerDataSource(geoContainer);
        geographicalDepartment.setItemCaptionPropertyId("name");
        geographicalDepartment.setImmediate(true);

        container.setApplyFiltersImmediately(false);
        filterDepartments(null);
        department.setContainerDataSource(container);
        department.setItemCaptionPropertyId("name");

        geographicalDepartment
                .addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(
                            com.vaadin.data.Property.ValueChangeEvent event) {
                        /*
                         * Modify filtering of the department combobox
                         */
                        EntityItem<Department> item = geoContainer
                                .getItem(geographicalDepartment.getValue());
                        Department entity = item.getEntity();
                        filterDepartments(entity);
                    }
                });
        department.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                /*
                 * Modify the actual value of the custom field.
                 */
                if (department.getValue() == null) {
                    setValue(null, false);
                } else {
                    Department entity = container
                            .getItem(department.getValue()).getEntity();
                    setValue(entity, false);
                }
            }
        });

        CssLayout cssLayout = new CssLayout();
        cssLayout.addComponent(geographicalDepartment);
        cssLayout.addComponent(department);
        setCompositionRoot(cssLayout);
    }

    /**
     * Modify available options based on the "geo deparment" select.
     * 
     * @param currentGeoDepartment
     */
    private void filterDepartments(Department currentGeoDepartment) {
        if (currentGeoDepartment == null) {
            department.setValue(null);
            department.setEnabled(false);
        } else {
            container.removeAllContainerFilters();
            container.addContainerFilter(new Equal("parent",
                    currentGeoDepartment));
            container.applyFilters();
            department.setValue(null);
            department.setEnabled(true);
        }
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        setDepartment(newDataSource.getValue());
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        setDepartment(newValue);
    }

    private void setDepartment(Object newValue) {
        Department value = (Department) newValue;
        geographicalDepartment.setValue(value != null ? value.getParent()
                .getId() : null);
        department.setValue(value != null ? value.getId() : null);
    }

    @Override
    public Class<?> getType() {
        return Department.class;
    }

    @Override
    public void setBuffered(boolean buffered) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isBuffered() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeAllValidators() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addValueChangeListener(ValueChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeValueChangeListener(ValueChangeListener listener) {
        // TODO Auto-generated method stub

    }

}
