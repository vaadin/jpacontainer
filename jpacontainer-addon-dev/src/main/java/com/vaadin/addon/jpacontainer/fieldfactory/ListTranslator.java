package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Indexed;
import com.vaadin.ui.AbstractSelect;

/**
 * Generic wrapper property that translates items (Set from Vaadin selects) to a
 * property of type List. The list order is taken from the selects order of
 * items.
 */
public class ListTranslator extends PropertyTranslator {
    
    private AbstractSelect select;

    public ListTranslator(AbstractSelect select) {
        this.select = select;
    }

    @Override
    public Object translateFromDatasource(Object value) {
        // Vaadin selects support all types of collections, just pass it forward
        return value;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object translateToDatasource(Object formattedValue) throws Exception {

        // NOTE, this currently works properly only if equals and hashcode
        // methods have been implemented correctly (both depending on identifier
        // of the entity)
        // TODO create a filter that has a workaround for invalid
        // equals/hashCode

        // formattedValue here is a set of identifiers.
        // We will modify the existing collection of entities to contain
        // corresponding entities
        Collection idset = (Collection) formattedValue;

        List list = (List) getPropertyDataSource().getValue();

        if (list == null) {
            list = createNewCollectionForType(getPropertyDataSource().getType());
        }

        HashSet orphaned = new HashSet(list);

        // Add those that did not exist do not exist already + remove them from
        // orphaned collection
        for (Object id : idset) {
            if (!list.contains(id)) {
                list.add(id);
            }
            orphaned.remove(id);
        }

        // remove orphanded
        for (Object entity : orphaned) {
            list.remove(entity);
        }

        try {
            sort(list);
        } catch (Exception e) {
        }

        return list;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void sort(List list) {
        Container containerDataSource = select.getContainerDataSource();
        final Indexed idx;
        final List ids;
        if (containerDataSource instanceof Indexed) {
            idx = (Indexed) containerDataSource;
            ids = null;
        } else {
            idx = null;
            ids = new ArrayList(containerDataSource.getItemIds()); 
        }
        
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                if(idx != null) {
                    return idx.indexOfId(arg0) - idx.indexOfId(arg1);
                }
                return ids.indexOf(arg0) - ids.indexOf(arg1);
            }
        });
    }

    @SuppressWarnings("rawtypes")
    static List createNewCollectionForType(Class<?> type)
            throws InstantiationException, IllegalAccessException {
        if (type.isInterface()) {
            if (type == List.class) {
                return new ArrayList();
            } else {
                throw new RuntimeException(
                        "Couldn't instantiate a collection for property.");
            }
        } else {
            return (List) type.newInstance();
        }
    }

}
