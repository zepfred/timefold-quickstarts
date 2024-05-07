package org.acme.vehiclerouting.domain;

public class Shipment {

    private Visit pickupVisit;
    private Visit deliveryVisit;
    private Integer weight;

    public Shipment(Visit pickupVisit, Visit deliveryVisit, Integer weight) {
        this.pickupVisit = pickupVisit;
        this.deliveryVisit = deliveryVisit;
        this.weight = weight;
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
}
