package com.vaadin.addon.jpacontainer.integration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.After;
import org.junit.Before;

import com.vaadin.Application;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.testdata.Person;
import com.vaadin.addon.jpacontainer.testdata.DataGenerator;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Paintable;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamVariable;
import com.vaadin.terminal.VariableOwner;
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
    private static boolean testdataReady = false;

    @Before
    public void setUp() throws Exception {
        if(!testdataReady) {
            DataGenerator.persistTestData(getEntityManager());
            testdataReady  = true;
        }
    }

    protected EntityManager getEntityManager() throws IOException {
        EntityManager createEntityManager = getEMF().createEntityManager();
        managers.add(createEntityManager);
        return createEntityManager;
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
                        new Properties(), new ApplicationContext() {

                            public File getBaseDirectory() {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            public Collection<Application> getApplications() {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            public void addTransactionListener(
                                    TransactionListener listener) {
                                // TODO Auto-generated method stub

                            }

                            public void removeTransactionListener(
                                    TransactionListener listener) {
                                // TODO Auto-generated method stub

                            }

                            public String generateApplicationResourceURL(
                                    ApplicationResource resource, String urlKey) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            public boolean isApplicationResourceURL(
                                    URL context, String relativeUri) {
                                // TODO Auto-generated method stub
                                return false;
                            }

                            public String getURLKey(URL context,
                                    String relativeUri) {
                                // TODO Auto-generated method stub
                                return null;
                            }
                        });
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    protected PaintTarget getFakePaintTarget() {
        return new PaintTarget() {

            public boolean startTag(Paintable paintable, String tag)
                    throws PaintException {
                return false;
            }

            public void startTag(String tagName) throws PaintException {

            }

            public void paintReference(Paintable paintable, String referenceName)
                    throws PaintException {

            }

            public boolean isFullRepaint() {
                return false;
            }

            public String getTag(Paintable paintable) {
                return "tag";
            }

            public void endTag(String tagName) throws PaintException {
            }

            public void addXMLSection(String sectionTagName,
                    String sectionData, String namespace) throws PaintException {

            }

            public void addVariable(VariableOwner owner, String name,
                    Paintable value) throws PaintException {
            }

            public void addVariable(VariableOwner owner, String name,
                    String[] value) throws PaintException {
            }

            public void addVariable(VariableOwner owner, String name,
                    boolean value) throws PaintException {
            }

            public void addVariable(VariableOwner owner, String name,
                    double value) throws PaintException {
            }

            public void addVariable(VariableOwner owner, String name,
                    float value) throws PaintException {
            }

            public void addVariable(VariableOwner owner, String name, long value)
                    throws PaintException {
            }

            public void addVariable(VariableOwner owner, String name, int value)
                    throws PaintException {
            }

            public void addVariable(VariableOwner owner, String name,
                    String value) throws PaintException {
            }

            public void addVariable(VariableOwner owner, String name,
                    StreamVariable value) throws PaintException {
            }

            public void addUploadStreamVariable(VariableOwner owner, String name)
                    throws PaintException {
            }

            public void addUIDL(String uidl) throws PaintException {
            }

            public void addText(String text) throws PaintException {
            }

            public void addSection(String sectionTagName, String sectionData)
                    throws PaintException {
            }

            public void addCharacterData(String text) throws PaintException {
            }

            public void addAttribute(String string, Object[] keys) {
            }

            public void addAttribute(String name, Paintable value)
                    throws PaintException {
            }

            public void addAttribute(String name, Map<?, ?> value)
                    throws PaintException {
            }

            public void addAttribute(String name, String value)
                    throws PaintException {
            }

            public void addAttribute(String name, double value)
                    throws PaintException {
            }

            public void addAttribute(String name, float value)
                    throws PaintException {
            }

            public void addAttribute(String name, long value)
                    throws PaintException {
            }

            public void addAttribute(String name, Resource value)
                    throws PaintException {
            }

            public void addAttribute(String name, int value)
                    throws PaintException {
            }

            public void addAttribute(String name, boolean value)
                    throws PaintException {
            }
        };
    }

}
