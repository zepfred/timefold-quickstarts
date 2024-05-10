package org.acme.vehiclerouting.solver.justification;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record ShipmentOrderJustification(String pickupVisitId, String deliveryVisitId,
                                         String description) implements ConstraintJustification {

    public ShipmentOrderJustification(String pickupVisitId, String deliveryVisitId) {
        this(pickupVisitId, deliveryVisitId, "The pickup visit '%s' comes after the delivery visit '%s'."
                .formatted(pickupVisitId, deliveryVisitId));
    }
}
