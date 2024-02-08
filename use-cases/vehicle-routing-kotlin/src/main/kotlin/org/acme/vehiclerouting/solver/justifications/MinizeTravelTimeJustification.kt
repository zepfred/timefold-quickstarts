package org.acme.vehiclerouting.solver.justifications

import ai.timefold.solver.core.api.score.stream.ConstraintJustification
import java.time.Duration

@JvmRecord
data class MinimizeTravelTimeJustification(
    val vehicleName: String?, val totalDrivingTimeSeconds: Long,
    val description: String
) : ConstraintJustification {
    constructor(vehicleName: String?, totalDrivingTimeSeconds: Long) : this(
        vehicleName, totalDrivingTimeSeconds, "Vehicle '%s' total travel time is %s."
            .formatted(vehicleName, formatDrivingTime(totalDrivingTimeSeconds))
    )

    companion object {
        private fun formatDrivingTime(drivingTimeSeconds: Long): String {
            val drivingTime = Duration.ofSeconds(drivingTimeSeconds)
            return "%s hours %s minutes".formatted(
                drivingTime.toHours(),
                if (drivingTime.toSecondsPart() >= 30) drivingTime.toMinutesPart() + 1 else drivingTime.toMinutesPart()
            )
        }
    }
}