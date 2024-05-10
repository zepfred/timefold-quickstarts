package org.acme.vehiclerouting.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Shipment.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Shipment {

    @PlanningId
    private String id;
    @JsonIdentityReference(alwaysAsId = true)
    private Visit pickupVisit;
    @JsonIdentityReference(alwaysAsId = true)
    private Visit deliveryVisit;
    private Integer weight;

    public Shipment() {
    }

    public Shipment(String id, Integer weight) {
        this.id = id;
        this.weight = weight;
    }

    public Shipment(String id, Visit pickupVisit, Visit deliveryVisit, Integer weight) {
        this.id = id;
        this.pickupVisit = pickupVisit;
        this.deliveryVisit = deliveryVisit;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Visit getPickupVisit() {
        return pickupVisit;
    }

    public void setPickupVisit(Visit pickupVisit) {
        this.pickupVisit = pickupVisit;
    }

    public Visit getDeliveryVisit() {
        return deliveryVisit;
    }

    public void setDeliveryVisit(Visit deliveryVisit) {
        this.deliveryVisit = deliveryVisit;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shipment shipment)) return false;
        return Objects.equals(getId(), shipment.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
