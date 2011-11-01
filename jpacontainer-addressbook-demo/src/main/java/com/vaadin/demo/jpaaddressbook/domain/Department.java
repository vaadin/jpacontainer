package com.vaadin.demo.jpaaddressbook.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class Department {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private String name;
	
	@OneToMany(mappedBy="department")
	private Set<Person> persons;
	
	@Transient
	private Boolean superDepartment;
	
	@ManyToOne
	private Department parent;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Person> getPersons() {
		return persons;
	}

	public void setPersons(Set<Person> persons) {
		this.persons = persons;
	}

	public Department getParent() {
		return parent;
	}

	public void setParent(Department parent) {
		this.parent = parent;
	}
	
	public boolean isSuperDepartment() {
		if(superDepartment == null) {
			superDepartment = getPersons().size() == 0;
		}
		return superDepartment;
	}
	
	@Override
	public String toString() {
		if(parent != null) {
			return parent.toString() + ":" +name;
		}
		return name;
	}
}
