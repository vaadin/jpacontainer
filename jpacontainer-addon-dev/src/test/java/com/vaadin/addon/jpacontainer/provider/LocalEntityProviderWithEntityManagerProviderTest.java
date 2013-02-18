package com.vaadin.addon.jpacontainer.provider;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertSame;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.jpacontainer.EntityManagerProvider;
import com.vaadin.addon.jpacontainer.testdata.Person;

public class LocalEntityProviderWithEntityManagerProviderTest {

    private EntityManager em;

    @Before
    public void setUp() {
        em = createNiceMock(EntityManager.class);
    }

    @Test
    public void testGetEntityManager_setNewEM_returnsTheSame() {
        LocalEntityProvider ep = new LocalEntityProvider(Person.class);
        ep.setEntityManager(em);

        assertSame(em, ep.getEntityManager());
    }

    @Test
    public void testGetEntityManager_EMPSetInConstructor_returnsEMGivenByEMP() {
        LocalEntityProvider ep = new LocalEntityProvider(Person.class,
                makeCustomEntityManagerProvider(em));
        assertSame(em, ep.getEntityManager());
    }

    @Test
    public void testGetEntityManager_EMPSetInConstructorEMSetLater_returnsEMSetLater() {
        LocalEntityProvider ep = new LocalEntityProvider(Person.class,
                makeCustomEntityManagerProvider(em));
        EntityManager em2 = createNiceMock(EntityManager.class);
        ep.setEntityManager(em2);
        assertSame(em2, ep.getEntityManager());
    }

    @Test
    public void testGetEntityManager_EMSetInConstructorEMPSetLater_returnsEMSetInConstructor() {
        LocalEntityProvider ep = new LocalEntityProvider(Person.class, em);
        ep.setEntityManagerProvider(makeCustomEntityManagerProvider(createNiceMock(EntityManager.class)));
        assertSame(em, ep.getEntityManager());
    }

    private EntityManagerProvider makeCustomEntityManagerProvider(
            final EntityManager emToProvide) {
        return new EntityManagerProvider() {
            @Override
            public EntityManager getEntityManager() {
                return emToProvide;
            }
        };
    }
}
