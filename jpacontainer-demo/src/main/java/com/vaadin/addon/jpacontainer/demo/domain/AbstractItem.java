/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.demo.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import org.apache.commons.lang.ObjectUtils;

/**
 * Example domain object for the JPAContainer demo application.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@MappedSuperclass
public abstract class AbstractItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Long version;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private Integer quantity = 0;
    @Column(nullable = false)
    private Integer price = 0;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getVersion() {
        return version;
    }

    public Integer getTotal() {
        return getQuantity() * getPrice();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            AbstractItem o = (AbstractItem) obj;
            return ObjectUtils.equals(description, o.description)
                    && ObjectUtils.equals(quantity, o.quantity)
                    && ObjectUtils.equals(price, o.price);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + ObjectUtils.hashCode(description);
        hash = hash * 31 + ObjectUtils.hashCode(quantity);
        hash = hash * 31 + ObjectUtils.hashCode(price);
        return hash;
    }
}
