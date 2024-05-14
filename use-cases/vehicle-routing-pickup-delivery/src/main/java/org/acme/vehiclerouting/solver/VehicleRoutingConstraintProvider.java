package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

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
                minimizeVehicleTravelTime(factory)
                //minimizeShipmentTravelTime(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint sameVehicleShipment(ConstraintFactory factory) {
        return factory.forEachUniquePair(Visit.class)
                .filter(Visit::isSameShipment)
                .filter((visit, visit2) -> !visit.isSameVehicle(visit2))
                .penalizeLong(HardSoftLongScore.ONE_HARD, (visit, ignore) -> visit.getShipment().getWeight() * 1000)
                .justifyWith((visit, visit2, score) -> new ShipmentVehicleJustification(visit.getId(),
                        visit.getVehicle().getId(), visit2.getId(), visit2.getVehicle().getId()))
                .asConstraint(SHIPMENT_VEHICLE);
    }

    protected Constraint pickupDeliveryOrder(ConstraintFactory factory) {
        return factory.forEachUniquePair(Visit.class)
                .filter(Visit::isSameShipment)
                .filter((visit, visit2) -> visit.isPickup())
                .filter((visit, visit2) -> visit.getVehicleIndex() > visit2.getVehicleIndex())
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        (visit, visit2) -> visit.getVehicleIndex() - visit2.getVehicleIndex())
                .justifyWith((visit, visit2, score) -> new ShipmentOrderJustification(visit.getId(),
                        visit2.getId()))
                .asConstraint(SHIPMENT_ORDER);
    }

    protected Constraint vehicleCapacityAtVisit(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(visit -> visit.getWeightAtVisit() != null)
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
        return factory.forEachUniquePair(Visit.class)
                .filter(Visit::isSameShipment)
                .filter((visit, visit2) -> visit.isPickup())
                .penalizeLong(HardSoftLongScore.ONE_SOFT, Visit::getTotalDrivingTimeSeconds)
                .justifyWith((visit, visit2, score) -> new MinimizeTravelTimeJustification("Shipment",
                        "[%s, %s]".formatted(visit.getId(), visit2.getId()), visit.getTotalDrivingTimeSeconds(visit2)))
                .asConstraint(MINIMIZE_SHIPMENT_TRAVEL_TIME);
    }
}
