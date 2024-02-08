package org.acme.vehiclerouting.domain;

import java.time.LocalDateTime
import java.util.stream.Stream

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore
import ai.timefold.solver.core.api.solver.SolverStatus

import org.acme.vehiclerouting.domain.geo.DrivingTimeCalculator
import org.acme.vehiclerouting.domain.geo.HaversineDrivingTimeCalculator

@PlanningSolution
class VehicleRoutePlan {
    lateinit var name: String
    var southWestCorner: Location? = null
        private set
    var northEastCorner: Location? = null
        private set
    var startDateTime: LocalDateTime? = null
        private set
    var endDateTime: LocalDateTime? = null
        private set

    @PlanningEntityCollectionProperty
    var vehicles: List<Vehicle>? = null
        private set

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    var visits: List<Visit>? = null
        private set

    @PlanningScore
    var score: HardSoftLongScore? = null

    var solverStatus: SolverStatus? = null

    var scoreExplanation: String? = null

    constructor()

    constructor(name: String, score: HardSoftLongScore?, solverStatus: SolverStatus?) {
        this.name = name
        this.score = score
        this.solverStatus = solverStatus
    }

    constructor(
        name: String,
        southWestCorner: Location?,
        northEastCorner: Location?,
        startDateTime: LocalDateTime?,
        endDateTime: LocalDateTime?,
        vehicles: List<Vehicle>,
        visits: List<Visit>
    ) {
        this.name = name
        this.southWestCorner = southWestCorner
        this.northEastCorner = northEastCorner
        this.startDateTime = startDateTime
        this.endDateTime = endDateTime
        this.vehicles = vehicles
        this.visits = visits
        val locations = Stream.concat(
            vehicles.stream().map({ obj: Vehicle -> obj.homeLocation }),
            visits.stream().map({ obj: Visit -> obj.location })
        ).toList()

        val drivingTimeCalculator: DrivingTimeCalculator = HaversineDrivingTimeCalculator.INSTANCE
        drivingTimeCalculator.initDrivingTimeMaps(locations)
    }

    val totalDrivingTimeSeconds: Long
        get() = if (vehicles == null) 0 else vehicles!!.stream()
            .mapToLong({ obj: Vehicle -> obj.totalDrivingTimeSeconds }).sum()
}