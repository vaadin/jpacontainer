/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.metadata;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * Test classes used by different unit tests in this package.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@SuppressWarnings("serial")
abstract class TestClasses {

    /*
     * Test classes that use field annotations
     */
    @MappedSuperclass
    static abstract class BaseEntity_F implements Serializable {

        @Id
        Integer id;
        @Version
        Integer version;

        public Integer getTransientBaseField() {
            return null;
        }
    }

    @Entity
    static class Person_F extends BaseEntity_F {

        String firstName;
        String lastName;
        @Embedded
        Address_F address;
        transient String transientField;
        @Transient
        String transientField2;
        @OneToMany(mappedBy = "parent")
        Collection<Person_F> children;
        @ManyToOne
        Person_F parent;
        transient Address_M transientAddress;

        public String getTransientField3() {
            return null;
        }

        public String getTransientField4() {
            return null;
        }

        public void setTransientField4(String value) {
        }

        public Address_M getTransientAddress() {
            return transientAddress;
        }

        public void setTransientAddress(Address_M transientAddress) {
            this.transientAddress = transientAddress;
        }
    }

    @Embeddable
    static class Address_F implements Serializable {

        String street;
        String postalCode;
    }

    /*
     * Test classes that use method annotations
     */
    @MappedSuperclass
    static abstract class BaseEntity_M implements Serializable {

        private Integer id;
        private Integer version;

        @Id
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @Version
        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public Integer getTransientBaseField() {
            return null;
        }
    }

    @Entity
    static class Person_M extends BaseEntity_M {

        private String firstName;
        private String lastName;
        private Address_M address;
        private String transientField2;
        private Collection<Person_M> children;
        private Person_M parent;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Embedded
        public Address_M getAddress() {
            return address;
        }

        public void setAddress(Address_M address) {
            this.address = address;
        }

        public String getTransientField() {
            return "transient field";
        }

        @Transient
        public String getTransientField2() {
            return transientField2;
        }

        public void setTransientField2(String value) {
            transientField2 = value;
        }

        @OneToMany(mappedBy = "parent")
        public Collection<Person_M> getChildren() {
            return children;
        }

        public void setChildren(Collection<Person_M> children) {
            this.children = children;
        }

        @ManyToOne
        public Person_M getParent() {
            return parent;
        }

        public void setParent(Person_M parent) {
            this.parent = parent;
        }
    }

    @Embeddable
    static class Address_M implements Serializable {

        private String street;
        private String postalCode;

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
    }

    /*
     * Test classes for embedded IDs
     */
    @Entity
    static class EmbeddedIdEntity_F implements Serializable {

        @EmbeddedId
        Address_F address;
    }

    @Entity
    static class EmbeddedIdEntity_M implements Serializable {

        private Address_M address;

        @EmbeddedId
        public Address_M getAddress() {
            return address;
        }

        public void setAddress(Address_M address) {
            this.address = address;
        }
    }

    /**
     * Test for metadata parsing order - subclass should override superclass
     * (#4590).
     */
    @MappedSuperclass
    static abstract class BaseEntity_TransientId_M<ID extends Serializable>
            implements Serializable {

        protected ID id;

        @Transient
        public abstract ID getId();

        public abstract void setId(ID id);
    }

    /**
     * Test for metadata parsing order - subclass should override superclass
     * (#4590).
     */
    @Entity
    static class Integer_ConcreteId_M extends BaseEntity_TransientId_M<Integer> {
        @Override
        @Id
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
        }
    }

    interface EconomicObject_D {
        public void foo();
    }

    @Entity
    public abstract class AbstractEconomicObject_D implements EconomicObject_D {
    }

    @MappedSuperclass
    public abstract class AbstractData_D {
        @ManyToOne(targetEntity = AbstractEconomicObject_D.class)
        @JoinColumn(name = "EconomicObject_ID")
        private EconomicObject_D economicObject;

        public EconomicObject_D getEconomicObject() {
            return economicObject;
        }

        public void setEconomicObject(EconomicObject_D economicObject) {
            this.economicObject = economicObject;
        }
    }

    @Entity
    public class Data_D extends AbstractData_D {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
