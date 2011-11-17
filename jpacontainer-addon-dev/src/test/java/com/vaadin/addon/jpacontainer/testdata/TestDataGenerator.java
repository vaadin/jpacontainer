package com.vaadin.addon.jpacontainer.testdata;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;

import com.vaadin.addon.jpacontainer.SortBy;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Like;

/**
 * A helper class to generate some test data and persist it.
 */
public class TestDataGenerator {

    @SuppressWarnings("unchecked")
    public static void createTestData() throws Exception {
        // Create the test data
        TestDataGenerator.skills = new ArrayList<Skill>();
        for (String skillName : TestDataGenerator.skillNames) {
            Skill skill = new Skill();
            skill.setSkillName(skillName);
            TestDataGenerator.skills.add(skill);
        }

        Random rnd = new Random();
        TestDataGenerator.testDataSortedByPrimaryKey = new ArrayList<Person>();
        TestDataGenerator.filteredTestDataSortedByPrimaryKey = new ArrayList<Person>();
        TestDataGenerator.testDataEmbeddedIdSortedByName = new ArrayList<EmbeddedIdPerson>();
        for (int i = 0; i < 500; i++) {
            Person p = new Person();
            p.setFirstName(TestDataGenerator.firstNames[(i / 10) % 10] + " "
                    + i);
            p.setLastName(TestDataGenerator.lastNames[i % 10]);
            p.setDateOfBirth(new Date(rnd.nextLong()));
            p.setAddress(new Address());
            p.getAddress()
                    .setStreet(
                            rnd.nextInt(1000)
                                    + " "
                                    + TestDataGenerator.streets[rnd
                                            .nextInt(TestDataGenerator.streets.length)]);
            p.getAddress().setPostOffice(
                    TestDataGenerator.postOffices[rnd
                            .nextInt(TestDataGenerator.postOffices.length)]);
            StringBuffer pc = new StringBuffer();
            for (int j = 0; j < 5; j++) {
                pc.append(rnd.nextInt(10));
            }
            p.getAddress().setPostalCode(pc.toString());
            TestDataGenerator.testDataSortedByPrimaryKey.add(p);
            /*
             * Our filter only includes persons whose lastname begin with S
             */
            if (p.getLastName().startsWith("S")) {
                TestDataGenerator.filteredTestDataSortedByPrimaryKey.add(p);
            }

            // EmbeddedId test data
            EmbeddedIdPerson eip = new EmbeddedIdPerson();
            eip.setName(new Name());
            eip.getName().setFirstName(p.getFirstName());
            eip.getName().setLastName(p.getLastName());
            eip.setAddress(p.getAddress().clone());
            eip.setDateOfBirth(p.getDateOfBirth());
            TestDataGenerator.testDataEmbeddedIdSortedByName.add(eip);
        }

        TestDataGenerator.testDataSortedByName = (ArrayList<Person>) ((ArrayList<Person>) TestDataGenerator.testDataSortedByPrimaryKey)
                .clone();
        TestDataGenerator.testDataSortedByLastNameAndStreet = (ArrayList<Person>) ((ArrayList<Person>) TestDataGenerator.testDataSortedByPrimaryKey)
                .clone();
        TestDataGenerator.filteredTestDataSortedByName = (ArrayList<Person>) ((ArrayList<Person>) TestDataGenerator.filteredTestDataSortedByPrimaryKey)
                .clone();
        // Set up some helper fields

        TestDataGenerator.sortByName = new ArrayList<SortBy>();
        TestDataGenerator.sortByName.add(new SortBy("lastName", true));
        TestDataGenerator.sortByName.add(new SortBy("firstName", true));

        TestDataGenerator.sortByLastNameAndStreet = new ArrayList<SortBy>();
        TestDataGenerator.sortByLastNameAndStreet.add(new SortBy("lastName",
                true));
        TestDataGenerator.sortByLastNameAndStreet.add(new SortBy(
                "address.street", true));

        TestDataGenerator.testFilter = new Like("lastName", "S%", true);

        // Sort the test data lists

        Comparator<Person> nameComparator = new Comparator<Person>() {

            public int compare(Person o1, Person o2) {
                int result = o1.getLastName().compareTo(o2.getLastName());
                if (result == 0) {
                    result = o1.getFirstName().compareTo(o2.getFirstName());
                    /*
                     * if (result == 0) { result =
                     * o1.getId().compareTo(o2.getId()); }
                     */
                }
                return result;
            }
        };

        Comparator<EmbeddedIdPerson> nameComparatorEmbeddedId = new Comparator<EmbeddedIdPerson>() {

            public int compare(EmbeddedIdPerson o1, EmbeddedIdPerson o2) {
                int result = o1.getName().getFirstName()
                        .compareTo(o2.getName().getFirstName());
                if (result == 0) {
                    result = o1.getName().getLastName()
                            .compareTo(o2.getName().getLastName());
                }
                return result;
            }

        };

        Comparator<Person> nameStreetComparator = new Comparator<Person>() {

            public int compare(Person o1, Person o2) {
                int result = o1.getLastName().compareTo(o2.getLastName());
                if (result == 0) {
                    result = o1.getAddress().getStreet()
                            .compareTo(o2.getAddress().getStreet());
                    /*
                     * if (result == 0) { result =
                     * o1.getId().compareTo(o2.getId()); }
                     */
                }
                return result;
            }
        };

        Collections
                .sort(TestDataGenerator.testDataSortedByName, nameComparator);
        Collections.sort(TestDataGenerator.testDataSortedByLastNameAndStreet,
                nameStreetComparator);
        Collections.sort(TestDataGenerator.filteredTestDataSortedByName,
                nameComparator);
        Collections.sort(TestDataGenerator.testDataEmbeddedIdSortedByName,
                nameComparatorEmbeddedId);

        assertFalse(TestDataGenerator.testDataSortedByName
                .equals(TestDataGenerator.testDataSortedByPrimaryKey));
        assertFalse(TestDataGenerator.testDataSortedByLastNameAndStreet
                .equals(TestDataGenerator.testDataSortedByPrimaryKey));
        assertFalse(TestDataGenerator.filteredTestDataSortedByName
                .equals(TestDataGenerator.filteredTestDataSortedByPrimaryKey));

        // Make the collections unmodifiable
        TestDataGenerator.testDataEmbeddedIdSortedByName = Collections
                .unmodifiableList(TestDataGenerator.testDataEmbeddedIdSortedByName);
        TestDataGenerator.testDataSortedByLastNameAndStreet = Collections
                .unmodifiableList(TestDataGenerator.testDataSortedByLastNameAndStreet);
        TestDataGenerator.testDataSortedByName = Collections
                .unmodifiableList(TestDataGenerator.testDataSortedByName);
        TestDataGenerator.testDataSortedByPrimaryKey = Collections
                .unmodifiableList(TestDataGenerator.testDataSortedByPrimaryKey);
    }

    public static void persistTestData(EntityManager entityManager2)
            throws Exception {
        entityManager2.getTransaction().begin();
        for (Skill s : TestDataGenerator.skills) {
            s.setId(null);
            entityManager2.persist(s);
        }
        for (Person p : TestDataGenerator.testDataSortedByPrimaryKey) {
            p.setId(null);
            if (p.getManager() != null) {
                p.getManager().setId(null);
            }
            entityManager2.persist(p);
        }
        for (EmbeddedIdPerson p : TestDataGenerator.testDataEmbeddedIdSortedByName) {
            entityManager2.persist(p);
        }
        entityManager2.flush();
        entityManager2.getTransaction().commit();
    }

    private static List<Person> testDataSortedByPrimaryKey;
    private static List<Person> testDataSortedByName;
    private static List<EmbeddedIdPerson> testDataEmbeddedIdSortedByName;
    private static List<Person> testDataSortedByLastNameAndStreet;
    private static List<Person> filteredTestDataSortedByPrimaryKey;
    private static List<Person> filteredTestDataSortedByName;
    private static Filter testFilter;
    private static List<SortBy> sortByName;
    private static List<SortBy> sortByLastNameAndStreet;
    private static List<Skill> skills;
    private static String[] firstNames = { "John", "Maxwell", "Joe", "Bob",
            "Eve", "Alice", "Scrooge", "Donald", "Mick", "Zandra" };
    private static String[] lastNames = { "Smith", "Smart", "Cool", "Thornton",
            "McDuck", "Lee", "Anderson", "Zucker", "Jackson", "Gordon" };
    private static String[] streets = { "Magna Avenue", "Fringilla Street",
            "Aliquet St.", "Pharetra Avenue", "Gravida St.", "Risus Street",
            "Ultricies Street", "Mi Avenue", "Libero Av.", "Purus Avenue" };
    private static String[] postOffices = { "Stockholm", "Helsinki", "Paris",
            "London", "Luxemburg", "Duckburg", "New York", "Tokyo", "Athens",
            "Sydney" };
    private static String[] skillNames = { "Java", "C", "C++", "Delphi", "PHP",
            "Vaadin", "JavaScript", "SQL", "HTML", "SOA" };

    public static List<Person> getTestDataSortedByName() {
        return Collections.unmodifiableList(testDataSortedByName);
    }

    public static List<EmbeddedIdPerson> getTestDataEmbeddedIdSortedByName() {
        return Collections.unmodifiableList(testDataEmbeddedIdSortedByName);
    }

    public static List<Person> getTestDataSortedByLastNameAndStreet() {
        return Collections.unmodifiableList(testDataSortedByLastNameAndStreet);
    }

    public static List<SortBy> getSortByName() {
        return Collections.unmodifiableList(sortByName);
    }

    public static List<SortBy> getSortByLastNameAndStreet() {
        return Collections.unmodifiableList(sortByLastNameAndStreet);
    }

    public static List<Person> getTestDataSortedByPrimaryKey() {
        return Collections.unmodifiableList(testDataSortedByPrimaryKey);
    }

    public static Filter getTestFilter() {
        return testFilter;
    }

    public static List<Person> getFilteredTestDataSortedByName() {
        return Collections.unmodifiableList(filteredTestDataSortedByName);
    }

    public static List<Person> getFilteredTestDataSortedByPrimaryKey() {
        return Collections.unmodifiableList(filteredTestDataSortedByPrimaryKey);
    }

    public static List<Skill> getSkills() {
        return Collections.unmodifiableList(skills);
    }

    static {
        try {
            createTestData();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
