/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.addon.jpacontainer.Filter;

/**
 * Abstract base class for {@link Junction}-implementations.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class AbstractJunction implements Junction {

    private static final long serialVersionUID = 4635505131153450999L;
    private List<Filter> filters = new ArrayList<Filter>();

    protected AbstractJunction(Filter... filters) {
        assert filters != null : "filters must not be null";

        for (Filter f : filters) {
            this.filters.add(f);
        }
    }

    protected AbstractJunction(List<Filter> filters) {
        assert filters != null : "filters must not be null";
        // We do not copy the filters instance directly, as we have
        // to make sure the internal list instance is writable, etc.
        for (Filter f : filters) {
            this.filters.add(f);
        }
    }

    public Junction add(Filter filter) {
        assert filter != null : "filter must not be null";
        filters.add(filter);
        return this;
    }

    public Junction remove(Filter filter) {
        assert filter != null : "filter must not be null";
        filters.remove(filter);
        return this;
    }

    public List<Filter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass() == getClass()
                && ((AbstractJunction) obj).filters.equals(filters);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 7 * filters.hashCode();
    }
}
