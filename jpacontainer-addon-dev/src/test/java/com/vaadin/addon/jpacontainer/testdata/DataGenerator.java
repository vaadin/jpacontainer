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
public class DataGenerator {

    @SuppressWarnings("unchecked")
    public static void createTestData() throws Exception {
        // Create the test data
        DataGenerator.skills = new ArrayList<Skill>();
        for (String skillName : DataGenerator.skillNames) {
            Skill skill = new Skill();
            skill.setSkillName(skillName);
            DataGenerator.skills.add(skill);
        }

        Random rnd = new Random(1);
        DataGenerator.testDataSortedByPrimaryKey = new ArrayList<Person>();
        DataGenerator.filteredTestDataSortedByPrimaryKey = new ArrayList<Person>();
        DataGenerator.testDataEmbeddedIdSortedByName = new ArrayList<EmbeddedIdPerson>();
        for (int i = 0; i < 500; i++) {
            Person p = new Person();
            p.setFirstName(DataGenerator.firstNames[(i / 10) % 10] + " " + i);
            p.setLastName(DataGenerator.lastNames[i % 10]);
            p.setDateOfBirth(new Date(rnd.nextLong()));
            p.setAddress(new Address());
            p.getAddress().setStreet(
                    rnd.nextInt(1000)
                            + " "
                            + DataGenerator.streets[rnd
                                    .nextInt(DataGenerator.streets.length)]);
            p.getAddress().setPostOffice(
                    DataGenerator.postOffices[rnd
                            .nextInt(DataGenerator.postOffices.length)]);
            StringBuffer pc = new StringBuffer();
            for (int j = 0; j < 5; j++) {
                pc.append(rnd.nextInt(10));
            }
            p.getAddress().setPostalCode(pc.toString());
            DataGenerator.testDataSortedByPrimaryKey.add(p);
            /*
             * Our filter only includes persons whose lastname begin with S
             */
            if (p.getLastName().startsWith("S")) {
                DataGenerator.filteredTestDataSortedByPrimaryKey.add(p);
            }

            // EmbeddedId test data
            EmbeddedIdPerson eip = new EmbeddedIdPerson();
            eip.setName(new Name());
            eip.getName().setFirstName(p.getFirstName());
            eip.getName().setLastName(p.getLastName());
            eip.setAddress(p.getAddress().clone());
            eip.setDateOfBirth(p.getDateOfBirth());
            DataGenerator.testDataEmbeddedIdSortedByName.add(eip);
        }

        DataGenerator.testDataSortedByName = (ArrayList<Person>) ((ArrayList<Person>) DataGenerator.testDataSortedByPrimaryKey)
                .clone();
        DataGenerator.testDataSortedByLastNameAndStreet = (ArrayList<Person>) ((ArrayList<Person>) DataGenerator.testDataSortedByPrimaryKey)
                .clone();
        DataGenerator.filteredTestDataSortedByName = (ArrayList<Person>) ((ArrayList<Person>) DataGenerator.filteredTestDataSortedByPrimaryKey)
                .clone();
        // Set up some helper fields

        DataGenerator.sortByName = new ArrayList<SortBy>();
        DataGenerator.sortByName.add(new SortBy("lastName", true));
        DataGenerator.sortByName.add(new SortBy("firstName", true));

        DataGenerator.sortByLastNameAndStreet = new ArrayList<SortBy>();
        DataGenerator.sortByLastNameAndStreet.add(new SortBy("lastName", true));
        DataGenerator.sortByLastNameAndStreet.add(new SortBy("address.street",
                true));

        DataGenerator.testFilter = new Like("lastName", "S%", true);

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

        Collections.sort(DataGenerator.testDataSortedByName, nameComparator);
        Collections.sort(DataGenerator.testDataSortedByLastNameAndStreet,
                nameStreetComparator);
        Collections.sort(DataGenerator.filteredTestDataSortedByName,
                nameComparator);
        Collections.sort(DataGenerator.testDataEmbeddedIdSortedByName,
                nameComparatorEmbeddedId);

        assertFalse(DataGenerator.testDataSortedByName
                .equals(DataGenerator.testDataSortedByPrimaryKey));
        assertFalse(DataGenerator.testDataSortedByLastNameAndStreet
                .equals(DataGenerator.testDataSortedByPrimaryKey));
        assertFalse(DataGenerator.filteredTestDataSortedByName
                .equals(DataGenerator.filteredTestDataSortedByPrimaryKey));

        // Make the collections unmodifiable
        DataGenerator.testDataEmbeddedIdSortedByName = Collections
                .unmodifiableList(DataGenerator.testDataEmbeddedIdSortedByName);
        DataGenerator.testDataSortedByLastNameAndStreet = Collections
                .unmodifiableList(DataGenerator.testDataSortedByLastNameAndStreet);
        DataGenerator.testDataSortedByName = Collections
                .unmodifiableList(DataGenerator.testDataSortedByName);
        DataGenerator.testDataSortedByPrimaryKey = Collections
                .unmodifiableList(DataGenerator.testDataSortedByPrimaryKey);
    }

    public static void persistTestData(EntityManager entityManager2)
            throws Exception {
        entityManager2.getTransaction().begin();
        for (Skill s : DataGenerator.skills) {
            s.setId(null);
            entityManager2.persist(s);
        }
        for (Person p : DataGenerator.testDataSortedByPrimaryKey) {
            p.setId(null);
            if (p.getManager() != null) {
                p.getManager().setId(null);
            }
            entityManager2.persist(p);
        }
        for (EmbeddedIdPerson p : DataGenerator.testDataEmbeddedIdSortedByName) {
            entityManager2.persist(p);
        }
        entityManager2.flush();
        entityManager2.getTransaction().commit();
    }

    public static void removeTestData(EntityManager entityManager2) {
        entityManager2.getTransaction().begin();
        entityManager2.createQuery("DELETE FROM PersonSkill ps")
                .executeUpdate();
        entityManager2.createQuery("DELETE FROM Skill s").executeUpdate();
        entityManager2.createQuery("UPDATE Person p SET p.manager = null")
                .executeUpdate();
        entityManager2.createQuery("DELETE FROM Person p").executeUpdate();
        entityManager2.createQuery("DELETE FROM EmbeddedIdPerson ep")
                .executeUpdate();
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
