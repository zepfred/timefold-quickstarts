package org.acme.vehiclerouting.solver.justifications

import ai.timefold.solver.core.api.score.stream.ConstraintJustification

@JvmRecord
data class VehicleCapacityJustification(
    val vehicleId: String?, val capacity: Int, val demand: Int,
    val description: String
) : ConstraintJustification {
    constructor(vehicleId: String?, capacity: Int, demand: Int) : this(
        vehicleId, capacity, demand, "Vehicle '%s' exceeded its max capacity by %s."
            .formatted(vehicleId, demand - capacity)
    )
}