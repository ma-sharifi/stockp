package com.example.stockp.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * save STOCK data
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "T_STOCK"
        , uniqueConstraints = {@UniqueConstraint(name = "UNQ_STO_NAME", columnNames = "NAME")}
)
public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", length = 60, nullable = false)
    private String name;

    @Column(name = "CURRENT_PRICE") // it can be null in my scenario
    private Long currentPrice;

    @Column(name = "LAST_UPDATE")
    private Date lastUpdate;

    public Stock(String name, Long currentPrice) {
        this.name = name;
        this.currentPrice = currentPrice;
    }

    public Long getId() {
        return this.id;
    }

    public Stock name(String name) {
        this.setName(name);
        return this;
    }

    public Stock currentPrice(Long currentPrice) {
        this.setCurrentPrice(currentPrice);
        return this;
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    @PreUpdate
    @PrePersist
    public void onUpdate() {
        lastUpdate = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stock)) {
            return false;
        }
        return id != null && id.equals(((Stock) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + getId() +
                ", name='" + getName() + "'" +
                ", currentPrice=" + getCurrentPrice() +
                ", lastUpdate='" + getLastUpdate() + "'" +
                "}";
    }
}
