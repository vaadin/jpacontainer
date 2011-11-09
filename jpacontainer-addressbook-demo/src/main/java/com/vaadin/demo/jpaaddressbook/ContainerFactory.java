package com.vaadin.demo.jpaaddressbook;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider;
import com.vaadin.demo.jpaaddressbook.domain.Department;
import com.vaadin.demo.jpaaddressbook.domain.Person;

public class ContainerFactory {

    private static EntityManagerFactory emf = Persistence
            .createEntityManagerFactory("addressbook");

    private JPAContainer<Person> personContainer;
    private EntityManager em;

    public ContainerFactory() {
        em = emf.createEntityManager();
    }

    /**
     * @return a cached container for Person entities.
     */
    public JPAContainer<Person> getPersonContainer() {
        if (personContainer == null) {
            personContainer = JPAContainerFactory.make(Person.class, em);
        }
        return personContainer;
    }

    /**
     * @return a cached read-only container for Department entities.
     */
    public JPAContainer<Department> getDepartmentReadOnlyContainer() {
        JPAContainer<Department> departmentContainer = new JPAContainer<Department>(
                Department.class) {
            @Override
            public boolean areChildrenAllowed(Object itemId) {
                return super.areChildrenAllowed(itemId)
                        && getItem(itemId).getEntity().isSuperDepartment();
            }
        };
        departmentContainer
                .setEntityProvider(new CachingLocalEntityProvider<Department>(
                        Department.class, em));
        departmentContainer.setParentProperty("parent");
        return departmentContainer;
    }
}
