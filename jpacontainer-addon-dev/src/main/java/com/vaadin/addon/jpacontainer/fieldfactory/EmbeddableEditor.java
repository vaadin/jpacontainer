package com.vaadin.addon.jpacontainer.fieldfactory;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.ui.Field;

/**
 * A field that edits {@link Embeddable} or {@link ElementCollection} should
 * implement this interface. {@link FieldFactory} can then provide a rudimentary
 * support for relations from the {@link Embedded} object.
 * 
 */
public interface EmbeddableEditor extends Field {
    
    @SuppressWarnings("rawtypes")
    EntityContainer getMasterEntityContainer();
    
    Class<?> getEmbeddedClassType();

}
