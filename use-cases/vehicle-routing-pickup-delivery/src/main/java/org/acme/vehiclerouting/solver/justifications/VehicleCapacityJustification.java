package org.acme.vehiclerouting.solver.justifications;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record VehicleCapacityJustification(String vehicleId, String visitId, int capacity, int demand,
                                           String description) implements ConstraintJustification {

    public VehicleCapacityJustification(String vehicleId, String visitId, int capacity, int demand) {
        this(vehicleId, visitId, capacity, demand, "Vehicle '%s' exceeded its max capacity by %s for visit '%s'."
                .formatted(vehicleId, demand - capacity, visitId));
    }
}
