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
package com.vaadin.demo.jpaaddressbook;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider;
import com.vaadin.demo.jpaaddressbook.domain.Department;

public class HierarchicalDepartmentContainer extends JPAContainer<Department> {

    public HierarchicalDepartmentContainer() {
        super(Department.class);
        setEntityProvider(new CachingLocalEntityProvider<Department>(
                Department.class,
                JPAContainerFactory
                        .createEntityManagerForPersistenceUnit(JpaAddressbookUI.PERSISTENCE_UNIT)));
        setParentProperty("parent");
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        return super.areChildrenAllowed(itemId)
                && getItem(itemId).getEntity().isSuperDepartment();
    }
}
