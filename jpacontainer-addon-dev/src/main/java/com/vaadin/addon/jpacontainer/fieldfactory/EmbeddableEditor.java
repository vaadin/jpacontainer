package com.vaadin.addon.jpacontainer.fieldfactory;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.vaadin.addon.jpacontainer.EntityContainer;

/**
 * A field that edits {@link Embeddable} or {@link ElementCollection} should
 * implement this interface. {@link FieldFactory} can then provide a rudimentary
 * support for relations from the {@link Embedded} object.
 * 
 */
public interface EmbeddableEditor {
    
    @SuppressWarnings("rawtypes")
    EntityContainer getMasterEntityContainer();
    
    Class<?> getEmbeddedClassType();

}
