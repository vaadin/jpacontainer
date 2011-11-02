package com.vaadin.demo.jpaaddressbook;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.demo.jpaaddressbook.domain.Department;
import com.vaadin.demo.jpaaddressbook.domain.Person;

public class ContainerFactory {

    private static ThreadLocal<ContainerFactory> factory = new ThreadLocal<ContainerFactory>();

    private static EntityManagerFactory emf = Persistence
            .createEntityManagerFactory("addressbook");

    private JPAContainer<Person> personContainer;
    private JPAContainer<Department> departmentContainer;
    private EntityManager em;

    private ContainerFactory() {
        em = emf.createEntityManager();
    }

    private static ContainerFactory get() {
        if (factory.get() == null) {
            factory.set(new ContainerFactory());
        }
        return factory.get();
    }

    /**
     * @return a cached container for Person entities.
     */
    public static JPAContainer<Person> getPersonContainer() {
        ContainerFactory cf = get();
        if (cf.personContainer == null) {
            cf.personContainer = new JPAContainer<Person>(Person.class);
            cf.personContainer
                    .setEntityProvider(new CachingMutableLocalEntityProvider<Person>(
                            Person.class, cf.em));
        }
        return cf.personContainer;
    }

    /**
     * @return a cached read-only container for Department entities.
     */
    public static JPAContainer<Department> getDepartmentReadOnlyContainer() {
        ContainerFactory cf = get();
        if (cf.departmentContainer == null) {
            cf.departmentContainer = new JPAContainer<Department>(
                    Department.class) {
                @Override
                public boolean areChildrenAllowed(Object itemId) {
                    return super.areChildrenAllowed(itemId)
                            && getItem(itemId).getEntity().isSuperDepartment();
                }
            };
            cf.departmentContainer
                    .setEntityProvider(new CachingLocalEntityProvider<Department>(
                            Department.class, cf.em));
            cf.departmentContainer.setParentProperty("parent");
        }
        return cf.departmentContainer;
    }
}
