package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import org.acme.vehiclerouting.domain.Driver;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    public static final String VEHICLE_CAPACITY = "vehicleCapacity";
    public static final String SERVICE_FINISHED_AFTER_MAX_END_TIME = "serviceFinishedAfterMaxEndTime";
    public static final String REQUIRED_LICENSE_TYPE = "requiredLicenseType";
    public static final String DRIVER_PER_VEHICLE = "driverPerVehicle";
    public static final String MINIMIZE_TRAVEL_TIME = "minimizeTravelTime";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                vehicleCapacity(factory),
                serviceFinishedAfterMaxEndTime(factory),
                requiredLicenseType(factory),
                driverPerVehicle(factory),
                minimizeTravelTime(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getTotalDemand() > vehicle.getCapacity())
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        vehicle -> vehicle.getTotalDemand() - vehicle.getCapacity())
                .asConstraint(VEHICLE_CAPACITY);
    }

    protected Constraint serviceFinishedAfterMaxEndTime(ConstraintFactory factory) {
        return factory.forEach(Visit.class)
                .filter(Visit::isServiceFinishedAfterMaxEndTime)
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        Visit::getServiceFinishedDelayInMinutes)
                .asConstraint(SERVICE_FINISHED_AFTER_MAX_END_TIME);
    }

    protected Constraint requiredLicenseType(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .filter(Vehicle::isLicenseIncompatible)
                .penalizeLong(HardSoftLongScore.ONE_HARD)
                .asConstraint(REQUIRED_LICENSE_TYPE);
    }

    protected Constraint driverPerVehicle(ConstraintFactory factory) {
        return factory.forEach(Driver.class)
                .penalizeLong(HardSoftLongScore.ONE_HARD, Driver::getVehicleCount)
                .asConstraint(DRIVER_PER_VEHICLE);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint minimizeTravelTime(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Vehicle::getTotalDrivingTimeSeconds)
                .asConstraint(MINIMIZE_TRAVEL_TIME);
    }
}
