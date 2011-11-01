package com.vaadin.demo.jpaaddressbook;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.demo.jpaaddressbook.domain.Group;
import com.vaadin.demo.jpaaddressbook.domain.Person;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

public class AddressBookMainView extends HorizontalSplitPanel implements
		ComponentContainer {

	private Tree groupTree;

	private Table personTable;

	private TextField searchField;

	private Button newButton;
	private Button deleteButton;

	private JPAContainer<Group> groups;
	private JPAContainer<Person> persons;

	static EntityManagerFactory emf = Persistence
			.createEntityManagerFactory("addressbook");

	public AddressBookMainView() {

		EntityManager em = emf.createEntityManager();

		buildTree(em);

		buildMainAray(em);

		setSplitPosition(30);

	}

	private void buildMainAray(EntityManager em) {
		VerticalLayout verticalLayout = new VerticalLayout();
		setSecondComponent(verticalLayout);

		persons = new JPAContainer<Person>(Person.class);
		persons.setEntityProvider(new CachingMutableLocalEntityProvider<Person>(
				Person.class, em));
		personTable = new Table(null, persons);

		personTable.setSizeFull();

		personTable.setVisibleColumns(new Object[] { "firstName", "lastName",
				"phoneNumber", "street", "city", "zipCode" });
		verticalLayout.addComponent(personTable);
		verticalLayout.setSizeFull();
	}

	private void buildTree(EntityManager em) {
		groups = new JPAContainer<Group>(Group.class);
		EntityProvider<Group> entityProvider = new CachingMutableLocalEntityProvider<Group>(
				Group.class, em);
		groups.setEntityProvider(entityProvider);
		groups.setParentProperty("parent");
		groupTree = new Tree(null, groups);
		groupTree.setItemCaptionPropertyId("name");

		setFirstComponent(groupTree);
	}

}
