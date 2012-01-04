/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.testdata;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity Java bean for testing.
 * 
 * @since 2.0
 */
@SuppressWarnings("serial")
@Entity
public class BeanWithLogic implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanWithLogic) {
            BeanWithLogic p = (BeanWithLogic) obj;
            if (this == p) {
                return true;
            }
            if (id == null || p.id == null) {
                return false;
            }
            return id.equals(p.id);
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    private String name;

    public String getName() {
        return name;
    }

    /**
     * Sets the name field. Converts the given value to uppercase, null values are replaced with "NOT SET". 
     */
    public void setName(String name) {
        if(name == null || name.isEmpty()) {
            this.name = "NOT SET";
        } else {
            this.name = name.toUpperCase();
        }
    }

}
