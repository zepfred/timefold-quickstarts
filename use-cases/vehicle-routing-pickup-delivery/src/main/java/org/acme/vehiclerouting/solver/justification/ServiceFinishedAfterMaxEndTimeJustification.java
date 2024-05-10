package org.acme.vehiclerouting.solver.justification;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record ServiceFinishedAfterMaxEndTimeJustification(String visitId, long serviceFinishedDelayInMinutes,
        String description) implements ConstraintJustification {

    public ServiceFinishedAfterMaxEndTimeJustification(String visitId, long serviceFinishedDelayInMinutes) {
        this(visitId, serviceFinishedDelayInMinutes, "Visit '%s' serviced with a %s-minute delay."
                .formatted(visitId, serviceFinishedDelayInMinutes));
    }
}
