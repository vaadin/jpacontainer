package com.vaadin.addon.jpacontainer.integration;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.After;
import org.junit.Before;

import com.vaadin.Application;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.testdata.DataGenerator;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Paintable;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Window;

/**
 * Helper class to test JPAContainer in components that are connected to a
 * (fake) application. E.g. component painting etc.
 */
public abstract class AbstractIntegrationTest {

    public static final String INTEGRATION_TEST_PERSISTENCE_UNIT = "jpacontainer-itest";
    private static EntityManagerFactory emf;
    private List<EntityManager> managers = new LinkedList<EntityManager>();
    private boolean testdataReady = false;

    @Before
    public void setUp() throws Exception {
        if (!testdataReady) {
            DataGenerator.removeTestData(getEntityManager());
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

            Application application = new Application() {
                @Override
                public void init() {
                    Window window = new Window();
                    setMainWindow(window);
                    window.setContent(TestLayout.this);
                }
            };

            try {
                application.start(new URL("http://localhost/test"),
                        new Properties(), mockApplicationContext());
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        private ApplicationContext mockApplicationContext() {
            return createNiceMock(ApplicationContext.class);
        }
    }

    protected PaintTarget getFakePaintTarget() throws PaintException {
        PaintTarget mockTarget = createNiceMock(PaintTarget.class);
        expect(mockTarget.getTag(isA(Paintable.class))).andStubReturn("tag");
        replay(mockTarget);
        return mockTarget;
    }
}
