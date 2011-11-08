/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import java.util.Iterator;
import java.util.List;

import com.vaadin.addon.jpacontainer.Filter;

/**
 * A filter that groups other filters together in a single conjunction (A and B
 * and C...).
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class Conjunction extends AbstractJunction {

    private static final long serialVersionUID = -7762904141209202934L;

    protected Conjunction(Filter[] filters) {
        super(filters);
    }

    protected Conjunction(List<Filter> filters) {
        super(filters);
    }

    public String toQLString() {
        return toQLString(PropertyIdPreprocessor.DEFAULT);
    }

    public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (Iterator<Filter> it = getFilters().iterator(); it.hasNext();) {
            sb.append(it.next().toQLString(propertyIdPreprocessor));
            if (it.hasNext()) {
                sb.append(" and ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
