package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.vehiclerouting.domain.Shipment;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.solver.justification.MinimizeTravelTimeJustification;
import org.acme.vehiclerouting.solver.justification.ServiceFinishedAfterMaxEndTimeJustification;
import org.acme.vehiclerouting.solver.justification.ShipmentOrderJustification;
import org.acme.vehiclerouting.solver.justification.ShipmentVehicleJustification;
import org.acme.vehiclerouting.solver.justification.VehicleCapacityJustification;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    public static final String VEHICLE_CAPACITY = "vehicleCapacity";
    public static final String SHIPMENT_ORDER = "shipmentOrder";
    public static final String SHIPMENT_VEHICLE = "shipmentVehicle";
    public static final String SERVICE_FINISHED_AFTER_MAX_END_TIME = "serviceFinishedAfterMaxEndTime";
    public static final String MINIMIZE_VEHICLE_TRAVEL_TIME = "minimizeVehicleTravelTime";
    public static final String MINIMIZE_SHIPMENT_TRAVEL_TIME = "minimizeShipmentTravelTime";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                sameVehicleShipment(factory),
                pickupDeliveryOrder(factory),
                vehicleCapacityAtVisit(factory),
                serviceFinishedAfterMaxEndTime(factory),
                minimizeVehicleTravelTime(factory),
                minimizeShipmentTravelTime(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    private Constraint sameVehicleShipment(ConstraintFactory factory) {
        return factory.forEach(Shipment.class)
                .filter(shipment -> !shipment.getPickupVisit().getVehicle().equals(shipment.getDeliveryVisit().getVehicle()))
                .penalizeLong(HardSoftLongScore.ONE_HARD, Shipment::getWeight)
                .justifyWith((shipment, score) -> new ShipmentVehicleJustification(shipment.getPickupVisit().getId(),
                        shipment.getPickupVisit().getVehicle().getId(), shipment.getDeliveryVisit().getId(),
                        shipment.getDeliveryVisit().getVehicle().getId()))
                .asConstraint(SHIPMENT_VEHICLE);
    }

    protected Constraint pickupDeliveryOrder(ConstraintFactory factory) {
        return factory.forEach(Shipment.class)
                .filter(shipment -> shipment.getPickupVisit().getVehicleIndex() > shipment.getDeliveryVisit().getVehicleIndex())
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        shipment -> shipment.getPickupVisit().getVehicleIndex() - shipment.getDeliveryVisit().getVehicleIndex())
                .justifyWith((shipment, score) -> new ShipmentOrderJustification(shipment.getPickupVisit().getId(),
                        shipment.getDeliveryVisit().getId()))
                .asConstraint(SHIPMENT_ORDER);
    }

    protected Constraint vehicleCapacityAtVisit(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(visit -> visit.getWeightAtVisit() > visit.getVehicle().getCapacity())
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        visit -> visit.getWeightAtVisit() - visit.getVehicle().getCapacity())
                .justifyWith((visit, score) -> new VehicleCapacityJustification(visit.getVehicle().getId(), visit.getId(),
                        visit.getWeightAtVisit(), visit.getVehicle().getCapacity()))
                .asConstraint(VEHICLE_CAPACITY);
    }

    protected Constraint serviceFinishedAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(Visit::isServiceFinishedAfterMaxEndTime)
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        Visit::getServiceFinishedDelayInMinutes)
                .justifyWith((visit, score) -> new ServiceFinishedAfterMaxEndTimeJustification(visit.getId(),
                        visit.getServiceFinishedDelayInMinutes()))
                .asConstraint(SERVICE_FINISHED_AFTER_MAX_END_TIME);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint minimizeVehicleTravelTime(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Vehicle::getTotalDrivingTimeSeconds)
                .justifyWith((vehicle, score) -> new MinimizeTravelTimeJustification("Vehicle", vehicle.getId(),
                        vehicle.getTotalDrivingTimeSeconds()))
                .asConstraint(MINIMIZE_VEHICLE_TRAVEL_TIME);
    }

    protected Constraint minimizeShipmentTravelTime(ConstraintFactory factory) {
        return factory.forEach(Shipment.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Shipment::getTotalDrivingTimeSeconds)
                .justifyWith((shipment, score) -> new MinimizeTravelTimeJustification("Shipment",
                        "[%s, %s]".formatted(shipment.getPickupVisit().getId(), shipment.getDeliveryVisit().getId()),
                        shipment.getTotalDrivingTimeSeconds()))
                .asConstraint(MINIMIZE_SHIPMENT_TRAVEL_TIME);
    }
}
