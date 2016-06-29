package com.vaadin.addon.jpacontainer.filter;

import java.util.Collection;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class In implements Filter {

    private static final long serialVersionUID = 1L;

    private final Object propertyId;
    private final Collection<?> values;

    public In(Object propertyId, Collection<?> values) {
        this.propertyId = propertyId;
        this.values = values;
    }

    /**
     * For in-memory filtering
     */
    @Override
    public boolean passesFilter(Object itemId, Item item) {
        final Property<?> p = item.getItemProperty(getPropertyId());
        if (null == p) {
            return false;
        }
        Object value = p.getValue();
        if (values.contains(value)) {
            return true;
        }
        
        return false;
    }

    @Override
    public boolean appliesToProperty(Object propertyId) {
        return getPropertyId().equals(propertyId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        // Only objects of the same class can be equal
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        final In o = (In) obj;

        // Checks the properties one by one
        if (getPropertyId() != o.getPropertyId() && null != o.getPropertyId()
                && !o.getPropertyId().equals(getPropertyId())) {
            return false;
        }

        return (null == getValue()) ? null == o.getValue() : getValue().equals(
                o.getValue());
    }

    @Override
    public int hashCode() {
        return (null != getPropertyId() ? getPropertyId().hashCode() : 0)
                ^ (null != getValue() ? getValue().hashCode() : 0);
    }

    /**
     * Returns the property id of the property to compare against the values.
     * 
     * @return property id (not null)
     */
    public Object getPropertyId() {
        return propertyId;
    }

    /**
     * Returns the values to compare the property against.
     * 
     * @return comparison reference values
     */
    public Collection<?> getValue() {
        return values;
    }
}
