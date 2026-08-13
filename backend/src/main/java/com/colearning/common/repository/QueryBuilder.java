package com.colearning.common.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic query builder for dynamic queries.
 * Provides a fluent API for building JPA Criteria queries.
 *
 * @param <T> Entity type
 */
public class QueryBuilder<T> {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final Class<T> entityClass;
    private final List<Predicate> predicates = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private Root<T> root;
    private CriteriaBuilder cb;
    private CriteriaQuery<T> query;
    
    public QueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    /**
     * Initialize the query builder.
     *
     * @return this builder
     */
    public QueryBuilder<T> init() {
        this.cb = entityManager.getCriteriaBuilder();
        this.query = cb.createQuery(entityClass);
        this.root = query.from(entityClass);
        return this;
    }
    
    /**
     * Add an equality predicate.
     *
     * @param field Field name
     * @param value Field value
     * @return this builder
     */
    public QueryBuilder<T> eq(String field, Object value) {
        if (value != null) {
            predicates.add(cb.equal(root.get(field), value));
        }
        return this;
    }
    
    /**
     * Add a not-equal predicate.
     *
     * @param field Field name
     * @param value Field value
     * @return this builder
     */
    public QueryBuilder<T> ne(String field, Object value) {
        if (value != null) {
            predicates.add(cb.notEqual(root.get(field), value));
        }
        return this;
    }
    
    /**
     * Add a like predicate.
     *
     * @param field Field name
     * @param value Field value
     * @return this builder
     */
    public QueryBuilder<T> like(String field, String value) {
        if (value != null && !value.isEmpty()) {
            predicates.add(cb.like(root.get(field), "%" + value + "%"));
        }
        return this;
    }
    
    /**
     * Add a greater-than predicate.
     *
     * @param field Field name
     * @param value Field value
     * @return this builder
     */
    public QueryBuilder<T> gt(String field, Number value) {
        if (value != null) {
            predicates.add(cb.greaterThan(root.get(field), value));
        }
        return this;
    }
    
    /**
     * Add a less-than predicate.
     *
     * @param field Field name
     * @param value Field value
     * @return this builder
     */
    public QueryBuilder<T> lt(String field, Number value) {
        if (value != null) {
            predicates.add(cb.lessThan(root.get(field), value));
        }
        return this;
    }
    
    /**
     * Add a between predicate.
     *
     * @param field Field name
     * @param start Start value
     * @param end   End value
     * @return this builder
     */
    public QueryBuilder<T> between(String field, Number start, Number end) {
        if (start != null && end != null) {
            predicates.add(cb.between(root.get(field), start, end));
        }
        return this;
    }
    
    /**
     * Add an in predicate.
     *
     * @param field  Field name
     * @param values Field values
     * @return this builder
     */
    public QueryBuilder<T> in(String field, List<?> values) {
        if (values != null && !values.isEmpty()) {
            predicates.add(root.get(field).in(values));
        }
        return this;
    }
    
    /**
     * Add an is-null predicate.
     *
     * @param field Field name
     * @return this builder
     */
    public QueryBuilder<T> isNull(String field) {
        predicates.add(cb.isNull(root.get(field)));
        return this;
    }
    
    /**
     * Add an is-not-null predicate.
     *
     * @param field Field name
     * @return this builder
     */
    public QueryBuilder<T> isNotNull(String field) {
        predicates.add(cb.isNotNull(root.get(field)));
        return this;
    }
    
    /**
     * Add an ascending order.
     *
     * @param field Field name
     * @return this builder
     */
    public QueryBuilder<T> orderByAsc(String field) {
        orders.add(cb.asc(root.get(field)));
        return this;
    }
    
    /**
     * Add a descending order.
     *
     * @param field Field name
     * @return this builder
     */
    public QueryBuilder<T> orderByDesc(String field) {
        orders.add(cb.desc(root.get(field)));
        return this;
    }
    
    /**
     * Add filters from a map.
     *
     * @param filters Map of field-value pairs
     * @return this builder
     */
    public QueryBuilder<T> withFilters(Map<String, Object> filters) {
        filters.forEach((field, value) -> {
            if (value instanceof String) {
                like(field, (String) value);
            } else {
                eq(field, value);
            }
        });
        return this;
    }
    
    /**
     * Build and execute the query.
     *
     * @return List of results
     */
    public List<T> build() {
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(orders);
        return entityManager.createQuery(query).getResultList();
    }
    
    /**
     * Build and execute the query with pagination.
     *
     * @param offset Offset
     * @param limit  Limit
     * @return List of results
     */
    public List<T> build(int offset, int limit) {
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(orders);
        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
    
    /**
     * Count the results.
     *
     * @return Count
     */
    public long count() {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}