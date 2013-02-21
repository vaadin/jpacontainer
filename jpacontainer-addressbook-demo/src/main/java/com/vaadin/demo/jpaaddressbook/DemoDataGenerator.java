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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import com.vaadin.demo.jpaaddressbook.domain.Department;
import com.vaadin.demo.jpaaddressbook.domain.Person;

public class DemoDataGenerator {

	final static String[] groupsNames = { "Corporate Development",
			"Human Resources", "Legal", "Environment", "Quality Assurance",
			"Research and Development", "Production", "Sales", "Marketing" };
	final static String[] officeNames = { "London",
		"New York", "Tokyo", "Turku"};
	final static String[] fnames = { "Peter", "Alice", "Joshua", "Mike",
			"Olivia", "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik",
			"Rene", "Lisa", "Marge" };
	final static String[] lnames = { "Smith", "Gordon", "Simpson", "Brown",
			"Clavel", "Simons", "Verne", "Scott", "Allison", "Gates",
			"Rowling", "Barks", "Ross", "Schneider", "Tate" };
	final static String cities[] = { "Amsterdam", "Berlin", "Helsinki",
			"Hong Kong", "London", "Luxemburg", "New York", "Oslo", "Paris",
			"Rome", "Stockholm", "Tokyo", "Turku" };
	final static String streets[] = { "4215 Blandit Av.", "452-8121 Sem Ave",
			"279-4475 Tellus Road", "4062 Libero. Av.", "7081 Pede. Ave",
			"6800 Aliquet St.", "P.O. Box 298, 9401 Mauris St.",
			"161-7279 Augue Ave", "P.O. Box 496, 1390 Sagittis. Rd.",
			"448-8295 Mi Avenue", "6419 Non Av.", "659-2538 Elementum Street",
			"2205 Quis St.", "252-5213 Tincidunt St.",
			"P.O. Box 175, 4049 Adipiscing Rd.", "3217 Nam Ave",
			"P.O. Box 859, 7661 Auctor St.", "2873 Nonummy Av.",
			"7342 Mi, Avenue", "539-3914 Dignissim. Rd.",
			"539-3675 Magna Avenue", "Ap #357-5640 Pharetra Avenue",
			"416-2983 Posuere Rd.", "141-1287 Adipiscing Avenue",
			"Ap #781-3145 Gravida St.", "6897 Suscipit Rd.",
			"8336 Purus Avenue", "2603 Bibendum. Av.", "2870 Vestibulum St.",
			"Ap #722 Aenean Avenue", "446-968 Augue Ave",
			"1141 Ultricies Street", "Ap #992-5769 Nunc Street",
			"6690 Porttitor Avenue", "Ap #105-1700 Risus Street",
			"P.O. Box 532, 3225 Lacus. Avenue", "736 Metus Street",
			"414-1417 Fringilla Street", "Ap #183-928 Scelerisque Road",
			"561-9262 Iaculis Avenue" };

	public static void create() {

		EntityManager em = Persistence
				.createEntityManagerFactory("addressbook")
				.createEntityManager();

		em.getTransaction().begin();
		Random r = new Random(0);
		for (String o : officeNames) {
			Department geoGroup = new Department();
			geoGroup.setName(o);
			for (String g : groupsNames) {
				Department group = new Department();
				group.setName(g);
				Set<Person> gPersons = new HashSet<Person>();
				
				int amount = r.nextInt(15) + 1;
				for (int i = 0; i < amount; i++) {
					Person p = new Person();
					p.setFirstName(fnames[r.nextInt(fnames.length)]);
					p.setLastName(lnames[r.nextInt(lnames.length)]);
					p.setCity(cities[r.nextInt(cities.length)]);
					p.setPhoneNumber("+358 02 555 " + r.nextInt(10) + r.nextInt(10)
							+ r.nextInt(10) + r.nextInt(10));
					int n = r.nextInt(100000);
					if (n < 10000) {
						n += 10000;
					}
					p.setZipCode("" + n);
					p.setStreet(streets[r.nextInt(streets.length)]);
					p.setDepartment(group);
					gPersons.add(p);
					em.persist(p);
				}
				group.setParent(geoGroup);
				group.setPersons(gPersons);
				em.persist(group);
			}
			em.persist(geoGroup);
		}

		em.getTransaction().commit();
	}

}
