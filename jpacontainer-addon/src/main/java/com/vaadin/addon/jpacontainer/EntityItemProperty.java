/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import com.vaadin.data.Property;

/**
 * Interface defining the Properties that are contained in a {@link EntityItem}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface EntityItemProperty extends Property,
		Property.ValueChangeNotifier {

	/**
	 * Gets the EntityItem that owns this property.
	 * 
	 * @return the item (never null).
	 */
	public EntityItem<?> getItem();
}
