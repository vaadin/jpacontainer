package com.vaadin.demo.jpaaddressbook;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.filter.ValueFilter;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.demo.jpaaddressbook.domain.Department;
import com.vaadin.demo.jpaaddressbook.domain.Person;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalSplitPanel;
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

	private JPAContainer<Department> groups;
	private JPAContainer<Person> persons;
	
	private Department groupFilter;


	static EntityManagerFactory emf = Persistence
			.createEntityManagerFactory("addressbook");

	public AddressBookMainView() {

		EntityManager em = emf.createEntityManager();

		buildTree(em);

		buildMainArea(em);

		setSplitPosition(30);

	}

	private void buildMainArea(EntityManager em) {
		VerticalLayout verticalLayout = new VerticalLayout();
		setSecondComponent(verticalLayout);

		persons = new JPAContainer<Person>(Person.class);
		persons.setEntityProvider(new CachingMutableLocalEntityProvider<Person>(
				Person.class, em));
		personTable = new Table(null, persons);
		
		personTable.addListener(new Property.ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				
			}
		});

		personTable.setSizeFull();

		personTable.setVisibleColumns(new Object[] { "firstName", "lastName", "department",
				"phoneNumber", "street", "city", "zipCode" });
		verticalLayout.addComponent(personTable);
		verticalLayout.setSizeFull();
		
	}

	private void buildTree(EntityManager em) {
		groups = new JPAContainer<Department>(Department.class) {
			@Override
			public boolean areChildrenAllowed(Object itemId) {
				return super.areChildrenAllowed(itemId) && getItem(itemId).getEntity().getPersons().isEmpty();
			}
		};
		EntityProvider<Department> entityProvider = new CachingMutableLocalEntityProvider<Department>(
				Department.class, em);
		groups.setEntityProvider(entityProvider);
		groups.setParentProperty("parent");
		groupTree = new Tree(null, groups);
		groupTree.setItemCaptionPropertyId("name");
		
		groupTree.setImmediate(true);
		groupTree.setSelectable(true);
		groupTree.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Department entity = groups.getItem(event.getProperty().getValue()).getEntity();
				if(entity != null) {
					groupFilter = entity;
				} else if (groupFilter != null) {
					groupFilter = null;
				}
				updateFilters();
			}

		});


		setFirstComponent(groupTree);
	}

	private void updateFilters() {
		persons.setApplyFiltersImmediately(false);
		persons.removeAllFilters();
		if(groupFilter != null) {
			// two level hierarchy at max in our demo
			if(groupFilter.getParent() == null) {
				persons.addFilter(Filters.joinFilter("department", Filters.eq("parent", groupFilter)));
			} else {
				persons.addFilter(Filters.eq("department", groupFilter));
			}
		}
		persons.applyFilters();
	}
}
