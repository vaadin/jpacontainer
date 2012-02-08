package com.vaadin.addon.jpacontainer.fieldfactory;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.ui.AbstractSelect;

/**
 * Wrapper property that translates entities to identifiers and visa versa.
 * Expects that the translator is used in a select backed by a jpacontainer.
 */
@SuppressWarnings("unchecked")
public class SingleSelectTranslator extends PropertyTranslator {

    private final AbstractSelect select;

    public SingleSelectTranslator(AbstractSelect select) {
        this.select = select;
    }

    @SuppressWarnings("rawtypes")
    private EntityContainer getContainer() {
        return (EntityContainer) select.getContainerDataSource();
    }

    @Override
    public Object translateFromDatasource(Object value) {
        // Value here is entity, should be transformed to identifier
        return getContainer().getEntityProvider().getIdentifier(value);
    }

    @Override
    public Object translateToDatasource(Object formattedValue) throws Exception {
        // formattedValue here is identifier, to be formatted to entity
        return getContainer().getEntityProvider().getEntity(getContainer(), formattedValue);
    }

}
