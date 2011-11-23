package com.vaadin.addon.jpacontainer.itest.addressbook;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherApplication;
import com.vaadin.addon.jpacontainer.itest.domain.Department;
import com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider;

public class HierarchicalDepartmentContainer extends JPAContainer<Department> {

    public HierarchicalDepartmentContainer() {
        super(Department.class);
        setEntityProvider(new CachingLocalEntityProvider<Department>(
                Department.class,
                JPAContainerFactory
                        .createEntityManagerForPersistenceUnit(TestLauncherApplication.PERSISTENCY_UNIT)));
        setParentProperty("parent");
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        return super.areChildrenAllowed(itemId)
                && getItem(itemId).getEntity().isSuperDepartment();
    }

}
