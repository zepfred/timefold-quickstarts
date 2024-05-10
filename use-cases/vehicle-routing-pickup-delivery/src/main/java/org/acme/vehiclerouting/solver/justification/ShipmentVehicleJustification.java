package org.acme.vehiclerouting.solver.justification;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record ShipmentVehicleJustification(String pickupVisitId, String pickupVehicleId, String deliveryVisitId,
        String deliveryVehicleId, String description) implements ConstraintJustification {

    public ShipmentVehicleJustification(String pickupVisitId, String pickupVehicleId, String deliveryVisitId,
            String deliveryVehicleId) {
        this(pickupVisitId, pickupVehicleId, deliveryVisitId, deliveryVehicleId,
                "The vehicle for visit '%s' is %s, and for visit '%s' is %s."
                        .formatted(pickupVisitId, pickupVehicleId, deliveryVisitId, deliveryVehicleId));
    }
}
