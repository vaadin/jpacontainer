/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.provider;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.Filter;
import com.vaadin.addon.jpacontainer.Filter.PropertyIdPreprocessor;
import com.vaadin.addon.jpacontainer.SortBy;
import com.vaadin.addon.jpacontainer.filter.CompositeFilter;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.addon.jpacontainer.filter.IntervalFilter;
import com.vaadin.addon.jpacontainer.filter.Junction;
import com.vaadin.addon.jpacontainer.filter.ValueFilter;
import com.vaadin.addon.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addon.jpacontainer.metadata.MetadataFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

/**
 * A read-only entity provider that works with a local {@link EntityManager}.
 * Most important features and limitations:
 * <ul>
 * <li>Does not do any internal caching, all information is always accessed
 * directly from the EntityManager</li>
 * <li>Explicitly detaches entities by default (see
 * {@link #isEntitiesDetached() })
 * <ul>
 * <li>Performs a serialize-deserialize cycle to clone entities in order to
 * explicitly detach them from the persistence context (<b>This is ugly!</b<)</li>
 * </ul>
 * </li>
 * <li>Uses lazy-loading of entities (when using detached entities, references
 * and collections within the entities should be configured to be fetched
 * eagerly, though)</li>
 * <li><strong>Does NOT currently support embedded identifiers!</strong></li>
 * </ul>
 * 
 * TODO Improve documentation!
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class LocalEntityProvider<T> implements EntityProvider<T>, Serializable {

	private static final long serialVersionUID = 1601796410565144708L;
	private transient EntityManager entityManager;
	private EntityClassMetadata<T> entityClassMetadata;
	private boolean entitiesDetached = true;

	/**
	 * Creates a new <code>LocalEntityProvider</code>.
	 * 
	 * @param entityClass
	 *            the entity class (must not be null).
	 * @param entityManager
	 *            the entity manager to use (must not be null).
	 */
	public LocalEntityProvider(Class<T> entityClass, EntityManager entityManager) {
		this(entityClass);
		assert entityManager != null : "entityManager must not be null";
		this.entityManager = entityManager;
	}

	/**
	 * Creates a new <code>LocalEntityProvider</code>. The entity manager must
	 * be set using {@link #setEntityManager(javax.persistence.EntityManager) }.
	 * 
	 * @param entityClass
	 *            the entity class (must not be null).
	 */
	public LocalEntityProvider(Class<T> entityClass) {
		assert entityClass != null : "entityClass must not be null";
		this.entityClassMetadata = MetadataFactory.getInstance().
				getEntityClassMetadata(entityClass);
	}
	private Serializable serializableEntityManager;

	// TODO Test serialization of entity manager
	protected Object writeReplace() throws ObjectStreamException {
		if (entityManager != null && entityManager instanceof Serializable) {
			serializableEntityManager = (Serializable) entityManager;
		}
		return this;
	}

	protected Object readResolve() throws ObjectStreamException {
		if (serializableEntityManager != null) {
			this.entityManager = (EntityManager) serializableEntityManager;
		}
		return this;
	}

	/**
	 * Sets the entity manager.
	 * 
	 * @param entityManager
	 *            the entity manager to set.
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Gets the metadata for the entity class.
	 * 
	 * @return the metadata (never null).
	 */
	protected EntityClassMetadata<T> getEntityClassMetadata() {
		return this.entityClassMetadata;
	}

	/**
	 * Gets the entity manager.
	 * 
	 * @return the entity manager, or null if none has been specified.
	 */
	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	/**
	 * Gets the entity manager.
	 * 
	 * @return the entity manager (never null).
	 * @throws IllegalStateException
	 *             if no entity manager is set.
	 */
	protected EntityManager doGetEntityManager() throws IllegalStateException {
		if (getEntityManager() == null) {
			throw new IllegalStateException("No entity manager specified");
		}
		return getEntityManager();
	}

	/**
	 * Creates a copy of <code>original</code> and adds an entry for the primary
	 * key to the end of the list.
	 * 
	 * @param original
	 *            the original list of sorting instructions (must not be null,
	 *            but may be empty).
	 * @return a new list with the added entry for the primary key.
	 */
	protected List<SortBy> addPrimaryKeyToSortList(List<SortBy> original) {
		ArrayList<SortBy> newList = new ArrayList<SortBy>();
		newList.addAll(original);
		if (getEntityClassMetadata().hasEmbeddedIdentifier()) {
			for (String p : getEntityClassMetadata().getIdentifierProperty().getTypeMetadata().getPersistentPropertyNames()) {
				newList.add(new SortBy(getEntityClassMetadata().getIdentifierProperty().getName() + "." + p, true));
			}
		} else {
			newList.add(new SortBy(getEntityClassMetadata().
					getIdentifierProperty().getName(), true));
		}
		return Collections.unmodifiableList(newList);
	}

	/**
	 * Creates a filtered query that does not do any sorting.
	 * 
	 * @see #createFilteredQuery(java.lang.String, java.lang.String, com.vaadin.addon.jpacontainer.Filter, java.util.List, boolean, com.vaadin.addon.jpacontainer.Filter.PropertyIdPreprocessor)
	 * @param fieldsToSelect
	 *            the fields to select (must not be null).
	 * @param entityAlias
	 *            the alias of the entity (must not be null).
	 * @param filter
	 *            the filter to apply, or null if no filters should be applied.
	 * @param propertyIdPreprocessor
	 *            the property ID preprocessor (may be null).
	 * @return the query (never null).
	 */
	protected Query createUnsortedFilteredQuery(String fieldsToSelect,
			String entityAlias, Filter filter,
			PropertyIdPreprocessor propertyIdPreprocessor) {
		return createFilteredQuery(fieldsToSelect, entityAlias, filter, null,
				false, propertyIdPreprocessor);
	}

	/**
	 * Creates a filtered, optionally sorted, query.
	 * 
	 * @param fieldsToSelect
	 *            the fields to select (must not be null).
	 * @param entityAlias
	 *            the alias of the entity (must not be null).
	 * @param filter
	 *            the filter to apply, or null if no filters should be applied.
	 * @param sortBy
	 *            the fields to sort by (must include at least one field), or
	 *            null if the result should not be sorted at all.
	 * @param swapSortOrder
	 *            true to swap the sort order, false to use the sort order
	 *            specified in <code>sortBy</code>. Only applies if
	 *            <code>sortBy</code> is not null.
	 * @param propertyIdPreprocessor
	 *            the property ID preprocessor to pass to
	 *            {@link Filter#toQLString(com.vaadin.addon.jpacontainer.Filter.PropertyIdPreprocessor)  }
	 *            , or null to use a default preprocessor (should be sufficient
	 *            in most cases).
	 * @return the query (never null).
	 */
	protected Query createFilteredQuery(String fieldsToSelect,
			final String entityAlias, Filter filter, List<SortBy> sortBy,
			boolean swapSortOrder, PropertyIdPreprocessor propertyIdPreprocessor) {
		assert fieldsToSelect != null : "fieldsToSelect must not be null";
		assert entityAlias != null : "entityAlias must not be null";
		assert sortBy == null || !sortBy.isEmpty() : "sortBy must be either null or non-empty";

		StringBuffer sb = new StringBuffer();
		sb.append("select ");
		sb.append(fieldsToSelect);
		sb.append(" from ");
		sb.append(getEntityClassMetadata().getEntityName());
		sb.append(" as ");
		sb.append(entityAlias);

		if (filter != null) {
			sb.append(" where ");

			if (propertyIdPreprocessor == null) {
				sb.append(filter.toQLString(new PropertyIdPreprocessor() {

					public String process(Object propertyId) {
						return entityAlias + "." + propertyId;
					}
				}));
			} else {
				sb.append(filter.toQLString(propertyIdPreprocessor));
			}
		}

		if (sortBy != null && sortBy.size() > 0) {
			sb.append(" order by ");
			for (Iterator<SortBy> it = sortBy.iterator(); it.hasNext();) {
				SortBy sortedProperty = it.next();
				sb.append(entityAlias);
				sb.append(".");
				sb.append(sortedProperty.propertyId);
				if (sortedProperty.ascending != swapSortOrder) {
					sb.append(" asc");
				} else {
					sb.append(" desc");
				}
				if (it.hasNext()) {
					sb.append(", ");
				}
			}
		}

		String queryString = sb.toString();
		Query query = doGetEntityManager().createQuery(queryString);
		if (filter != null) {
			setQueryParameters(query, filter);
		}
		return query;
	}

	private void setQueryParameters(Query query, Filter filter) {
		// TODO Add test that detects if any specific filter type is missing!
		if (filter instanceof ValueFilter) {
			ValueFilter vf = (ValueFilter) filter;
			query.setParameter(vf.getQLParameterName(), vf.getValue());
		} else if (filter instanceof IntervalFilter) {
			IntervalFilter intf = (IntervalFilter) filter;
			query.setParameter(intf.getEndingPointQLParameterName(), intf.getEndingPoint());
			query.setParameter(intf.getStartingPointQLParameterName(), intf.getStartingPoint());
		} else if (filter instanceof CompositeFilter) {
			for (Filter f : ((CompositeFilter) filter).getFilters()) {
				setQueryParameters(query, f);
			}
		}
	}

	protected boolean doContainsEntity(Object entityId, Filter filter) {
		assert entityId != null : "entityId must not be null";
		Filter entityIdFilter = Filters.eq(getEntityClassMetadata().
				getIdentifierProperty().getName(), entityId);
		Filter f;
		if (filter == null) {
			f = entityIdFilter;
		} else {
			f = Filters.and(entityIdFilter, filter);
		}
		Query query;
		if (getEntityClassMetadata().hasEmbeddedIdentifier()) {
			/*
			 * Hibernate will generate SQL for "count(obj)" that does not
			 * run on HSQLDB. "count(*)" works fine, but then EclipseLink
			 * won't work. With this hack, this method should work with
			 * both Hibernate and EclipseLink.
			 */
			query = createUnsortedFilteredQuery(String.format("count(obj.%s.%s)",
					getEntityClassMetadata().getIdentifierProperty().getName(),
					getEntityClassMetadata().getIdentifierProperty().getTypeMetadata().getPersistentPropertyNames().iterator().next()),
					"obj", f, null);
		} else {
			query = createUnsortedFilteredQuery("count(obj)", "obj", f,
					null);
		}
		Object result = query.getSingleResult();
		if (result instanceof Integer) {
			return ((Integer) result).intValue() == 1;
		} else {
			return ((Long) result).longValue() == 1;
		}
	}

	public boolean containsEntity(Object entityId, Filter filter) {
		return doContainsEntity(entityId, filter);
	}

	protected T doGetEntity(Object entityId) {
		assert entityId != null : "entityId must not be null";
		T entity = doGetEntityManager().find(
				getEntityClassMetadata().getMappedClass(), entityId);
		return detachEntity(entity);
	}

	public T getEntity(Object entityId) {
		return doGetEntity(entityId);
	}

	protected Object doGetEntityIdentifierAt(Filter filter, List<SortBy> sortBy, int index) {
		assert sortBy != null : "sortBy must not be null";
		Query query = createFilteredQuery("obj."
				+ getEntityClassMetadata().getIdentifierProperty().getName(),
				"obj", filter, addPrimaryKeyToSortList(sortBy), false, null);
		query.setMaxResults(1);
		query.setFirstResult(index);
		List<?> result = query.getResultList();
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

	public Object getEntityIdentifierAt(Filter filter, List<SortBy> sortBy,
			int index) {
		return doGetEntityIdentifierAt(filter, sortBy, index);
	}

	protected int doGetEntityCount(Filter filter) {
		Query query;
		if (getEntityClassMetadata().hasEmbeddedIdentifier()) {
			/*
			 * Hibernate will generate SQL for "count(obj)" that does not
			 * run on HSQLDB. "count(*)" works fine, but then EclipseLink
			 * won't work. With this hack, this method should work with
			 * both Hibernate and EclipseLink.
			 */
			query = createUnsortedFilteredQuery(String.format("count(obj.%s.%s)",
					getEntityClassMetadata().getIdentifierProperty().getName(),
					getEntityClassMetadata().getIdentifierProperty().getTypeMetadata().getPersistentPropertyNames().iterator().next()),
					"obj", filter, null);
		} else {
			query = createUnsortedFilteredQuery("count(obj)", "obj", filter,
					null);
		}
		Object ret = query.getSingleResult();
		if (ret instanceof Integer) {
			return ((Integer) ret).intValue();
		} else {
			return ((Long) ret).intValue();
		}
	}

	public int getEntityCount(Filter filter) {
		return doGetEntityCount(filter);
	}

	protected Object doGetFirstEntityIdentifier(Filter filter, List<SortBy> sortBy) {
		assert sortBy != null : "sortBy must not be null";
		Query query = createFilteredQuery("obj."
				+ getEntityClassMetadata().getIdentifierProperty().getName(),
				"obj", filter, addPrimaryKeyToSortList(sortBy), false, null);
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

	public Object getFirstEntityIdentifier(Filter filter, List<SortBy> sortBy) {
		return doGetFirstEntityIdentifier(filter, sortBy);
	}

	protected Object doGetLastEntityIdentifier(Filter filter, List<SortBy> sortBy) {
		assert sortBy != null : "sortBy must not be null";
		Query query = createFilteredQuery("obj."
				+ getEntityClassMetadata().getIdentifierProperty().getName(),
				"obj", filter, addPrimaryKeyToSortList(sortBy), true, null);
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

	public Object getLastEntityIdentifier(Filter filter, List<SortBy> sortBy) {
		return doGetLastEntityIdentifier(filter, sortBy);
	}

	/**
	 * If <code>backwards</code> is false, this method will return the
	 * identifier of the entity next to the entity identified by
	 * <code>entityId</code>. If true, this method will return the identifier of
	 * the entity previous to the entity identified by <code>entityId</code>.
	 * <code>filter</code> and <code>sortBy</code> is used to define and limit
	 * the list of entities to be used for determining the sibling.
	 * 
	 * @param entityId
	 *            the identifier of the entity whose sibling to retrieve (must
	 *            not be null).
	 * @param filter
	 *            an optional filter to limit the entities (may be null).
	 * @param sortBy
	 *            the order in which the list should be sorted (must not be
	 *            null).
	 * @param backwards
	 *            true to fetch the previous sibling, false to fetch the next
	 *            sibling.
	 * @return the identifier of the "sibling".
	 */
	protected Object getSibling(Object entityId, Filter filter,
			List<SortBy> sortBy, boolean backwards) {
		Query query = createSiblingQuery(entityId, filter, sortBy, backwards);
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (result.size() != 1) {
			return null;
		} else {
			return result.get(0);
		}
	}

	/**
	 * This method creates a query that can be used to fetch the siblings of
	 * a specific entity. If <code>backwards</code> is false, the query will
	 * begin with the entity next to the entity identified by <code>entityId</code>.
	 * If <code>backwards</code> is false, the query will begin with the entity
	 * prior to the entity identified by <code>entityId</code>.
	 *
	 * @param entityId
	 *            the identifier of the entity whose sibling to retrieve (must
	 *            not be null).
	 * @param filter
	 *            an optional filter to limit the entities (may be null).
	 * @param sortBy
	 *            the order in which the list should be sorted (must not be
	 *            null).
	 * @param backwards
	 *            true to fetch the previous sibling, false to fetch the next
	 *            sibling.
	 * @return the query that will return the sibling and all the subsequent entities unless limited.
	 */
	protected Query createSiblingQuery(Object entityId, Filter filter,
			List<SortBy> sortBy, boolean backwards) {
		assert entityId != null : "entityId must not be null";
		assert sortBy != null : "sortBy must not be null";
		Filter limitingFilter;
		sortBy = addPrimaryKeyToSortList(sortBy);
		if (sortBy.size() == 1) {
			// The list is sorted by primary key
			if (backwards) {
				limitingFilter = Filters.lt(getEntityClassMetadata().
						getIdentifierProperty().getName(), entityId);
			} else {
				limitingFilter = Filters.gt(getEntityClassMetadata().
						getIdentifierProperty().getName(), entityId);
			}
		} else {
			// We have to fetch the values of the sorted fields
			T currentEntity = getEntity(entityId);
			if (currentEntity == null) {
				throw new EntityNotFoundException(
						"No entity found with the ID " + entityId);
			}
			// Collect the values into a map for easy access
			Map<Object, Object> filterValues = new HashMap<Object, Object>();
			for (SortBy sb : sortBy) {
				filterValues.put(sb.propertyId, getEntityClassMetadata().
						getPropertyValue(currentEntity,
						sb.propertyId.toString()));
			}
			// Now we can build a filter that limits the query to the entities
			// below entityId
			limitingFilter = Filters.or();
			for (int i = sortBy.size() - 1; i >= 0; i--) {
				// TODO Document this code snippet once it works
				// TODO What happens with null values?
				Junction caseFilter = Filters.and();
				SortBy sb;
				for (int j = 0; j < i; j++) {
					sb = sortBy.get(j);
					caseFilter.add(Filters.eq(sb.propertyId, filterValues.get(
							sb.propertyId)));
				}
				sb = sortBy.get(i);
				if (sb.ascending ^ backwards) {
					caseFilter.add(Filters.gt(sb.propertyId, filterValues.get(
							sb.propertyId)));
				} else {
					caseFilter.add(Filters.lt(sb.propertyId, filterValues.get(
							sb.propertyId)));
				}
				((Junction) limitingFilter).add(caseFilter);
			}
		}
		// Now, we can create the query
		Filter queryFilter;
		if (filter == null) {
			queryFilter = limitingFilter;
		} else {
			queryFilter = Filters.and(filter, limitingFilter);
		}
		Query query = createFilteredQuery("obj."
				+ getEntityClassMetadata().getIdentifierProperty().getName(),
				"obj", queryFilter, sortBy, backwards, null);
		return query;
	}

	protected Object doGetNextEntityIdentifier(Object entityId, Filter filter,
			List<SortBy> sortBy) {
		return getSibling(entityId, filter, sortBy, false);
	}

	public Object getNextEntityIdentifier(Object entityId, Filter filter,
			List<SortBy> sortBy) {
		return doGetNextEntityIdentifier(entityId, filter, sortBy);
	}

	protected Object doGetPreviousEntityIdentifier(Object entityId, Filter filter,
			List<SortBy> sortBy) {
		return getSibling(entityId, filter, sortBy, true);
	}

	public Object getPreviousEntityIdentifier(Object entityId, Filter filter,
			List<SortBy> sortBy) {
		return doGetPreviousEntityIdentifier(entityId, filter, sortBy);
	}

	/**
	 * Detaches <code>entity</code> from the entity manager (until JPA 2.0
	 * arrives). If <code>entity</code> is null, then null is returned. If
	 * {@link #isEntitiesDetached() } is false, <code>entity</code> is returned
	 * directly.
	 * 
	 * @param entity
	 *            the entity to detach.
	 * @return the detached entity.
	 */
	protected T detachEntity(T entity) {
		if (entity == null) {
			return null;
		}
		if (!isEntitiesDetached()) {
			return entity;
		}
		// TODO Replace with more efficient code, or a call to JPA 2.0
		if (entity instanceof Serializable) {
			/*
			 * What we do here is basically a clone, but we are using the Java
			 * serialization API. Thus, the entity parameter will be managed,
			 * but the returned entity will be a detached exact (well, more or
			 * less) copy of the entity.
			 * 
			 * As of JPA 2.0, we can remove this code and just ask JPA to detach
			 * the object for us.
			 */
			try {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);
				oos.writeObject(entity);
				ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
				ObjectInputStream ois = new ObjectInputStream(is);
				return getEntityClassMetadata().getMappedClass().cast(
						ois.readObject());
			} catch (Exception e) {
				// Do nothing, entity manager will be cleared
			}
		}
		System.out.println(
				"WARNING: Clearing EntityManager in order to detach the entities in it");
		doGetEntityManager().clear();
		return entity;
	}

	public boolean isEntitiesDetached() {
		return entitiesDetached;
	}

	public void setEntitiesDetached(boolean detached)
			throws UnsupportedOperationException {
		this.entitiesDetached = detached;
	}

	@SuppressWarnings("unchecked")
	protected List<Object> doGetAllEntityIdentifiers(Filter filter,
			List<SortBy> sortBy) {
		Query query = createFilteredQuery("obj."
				+ getEntityClassMetadata().getIdentifierProperty().getName(),
				"obj", filter, sortBy, false, null);
		return Collections.unmodifiableList(query.getResultList());
	}

	public List<Object> getAllEntityIdentifiers(Filter filter,
			List<SortBy> sortBy) {
		return doGetAllEntityIdentifiers(filter, sortBy);
	}
}
