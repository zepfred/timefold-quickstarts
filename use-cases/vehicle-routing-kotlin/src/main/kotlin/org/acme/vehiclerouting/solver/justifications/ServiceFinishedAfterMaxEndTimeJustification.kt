package org.acme.vehiclerouting.solver.justifications

import ai.timefold.solver.core.api.score.stream.ConstraintJustification

@JvmRecord
data class ServiceFinishedAfterMaxEndTimeJustification(
    val visitId: String?, val serviceFinishedDelayInMinutes: Long,
    val description: String
) : ConstraintJustification {
    constructor(visitId: String?, serviceFinishedDelayInMinutes: Long) : this(
        visitId, serviceFinishedDelayInMinutes, "Visit '%s' serviced with a %s-minute delay."
            .formatted(visitId, serviceFinishedDelayInMinutes)
    )
}