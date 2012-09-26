package com.vaadin.addon.jpacontainer.integration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.After;
import org.junit.Before;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.testdata.DataGenerator;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;

/**
 * Helper class to test JPAContainer in components that are connected to a
 * (fake) application. E.g. component painting etc.
 */
public abstract class AbstractIntegrationTest {

    public static final String INTEGRATION_TEST_PERSISTENCE_UNIT = "jpacontainer-itest";
    private EntityManagerFactory emf;
    private List<EntityManager> managers = new LinkedList<EntityManager>();
    private boolean testdataReady = false;

    @Before
    public void setUp() throws Exception {
        if (!testdataReady) {
            DataGenerator.removeTestData(getEntityManager());
            DataGenerator.createTestData();
            DataGenerator.persistTestData(getEntityManager());
            testdataReady = true;
        }
    }

    protected EntityManager getEntityManager() throws IOException {
        EntityManager em = getEMF().createEntityManager();
        managers.add(em);
        return em;
    }

    private EntityManagerFactory getEMF() throws IOException {
        if (emf == null) {
            emf = createEntityManagerFactory();
        }
        return emf;
    }

    protected static String getDatabaseFileName() throws IOException {
        File f = File.createTempFile("jpacontainer_integration_test", "");
        return f.getAbsolutePath();
    }

    protected static String getDatabaseUrl() throws IOException {
        return "jdbc:hsqldb:file:" + getDatabaseFileName();
    }

    protected abstract EntityManagerFactory createEntityManagerFactory()
            throws IOException;

    @After
    public void tearDown() {
        closeEntityManager();
    }

    protected void closeEntityManager() {
        for (EntityManager m : managers) {
            m.close();
        }
    }

    protected JPAContainer<Person> getPersonContainer() {
        JPAContainer<Person> make = null;
        try {
            make = JPAContainerFactory.make(Person.class, getEntityManager());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return make;
    }

    /**
     * @return a layout connected to a fake app
     */
    protected Layout createDummyLayout() {
        return new TestLayout();
    }

    public static class TestLayout extends CssLayout {

        public TestLayout() {

            UI application = new UI() {
                @Override
                public void init(VaadinRequest req) {
                    setContent(TestLayout.this);
                }
            };

            // try {
            // application.start(new URL("http://localhost/test"),
            // new Properties(), mockApplicationContext());
            // } catch (MalformedURLException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

        }

        // private ApplicationContext mockApplicationContext() {
        // return createNiceMock(ApplicationContext.class);
        // }
    }

    // protected PaintTarget getFakePaintTarget() throws PaintException {
    // PaintTarget mockTarget = createNiceMock(PaintTarget.class);
    // expect(mockTarget.getTag(isA(Paintable.class))).andStubReturn("tag");
    // replay(mockTarget);
    // return mockTarget;
    // }
}
