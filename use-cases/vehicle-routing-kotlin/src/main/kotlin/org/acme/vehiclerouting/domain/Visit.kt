package org.acme.vehiclerouting.domain;

import java.time.Duration
import java.time.LocalDateTime

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.lookup.PlanningId
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable
import ai.timefold.solver.core.api.domain.variable.ShadowVariable

import org.acme.vehiclerouting.solver.ArrivalTimeUpdatingVariableListener

@PlanningEntity
class Visit {
    @PlanningId
    lateinit var id: String
    lateinit var name: String
    lateinit var location: Location
    var demand: Int = 0
    lateinit var minStartTime: LocalDateTime
    lateinit var maxEndTime: LocalDateTime
    lateinit var serviceDuration: Duration

    private var vehicle: Vehicle? = null

    @get:PreviousElementShadowVariable(sourceVariableName = "visits")
    var previousVisit: Visit? = null

    @get:NextElementShadowVariable(sourceVariableName = "visits")
    var nextVisit: Visit? = null

    @get:ShadowVariable(
        variableListenerClass = ArrivalTimeUpdatingVariableListener::class,
        sourceVariableName = "previousVisit"
    )
    @get:ShadowVariable(
        variableListenerClass = ArrivalTimeUpdatingVariableListener::class,
        sourceVariableName = "vehicle"
    )
    var arrivalTime: LocalDateTime? = null

    constructor()

    constructor(
        id: String, name: String, location: Location, demand: Int,
        minStartTime: LocalDateTime, maxEndTime: LocalDateTime, serviceDuration: Duration
    ) {
        this.id = id
        this.name = name
        this.location = location
        this.demand = demand
        this.minStartTime = minStartTime
        this.maxEndTime = maxEndTime
        this.serviceDuration = serviceDuration
    }

    @InverseRelationShadowVariable(sourceVariableName = "visits")
    fun getVehicle(): Vehicle? {
        return vehicle
    }

    val departureTime: LocalDateTime?
        get() {
            if (arrivalTime == null) {
                return null
            }
            return startServiceTime!!.plus(serviceDuration)
        }

    val startServiceTime: LocalDateTime?
        get() {
            if (arrivalTime == null) {
                return null
            }
            return if (arrivalTime!!.isBefore(minStartTime)) minStartTime else arrivalTime
        }

    val isServiceFinishedAfterMaxEndTime: Boolean
        get() = (arrivalTime != null
                && arrivalTime!!.plus(serviceDuration).isAfter(maxEndTime))

    val serviceFinishedDelayInMinutes: Long
        get() {
            if (arrivalTime == null) {
                return 0
            }
            return Duration.between(maxEndTime, arrivalTime!!.plus(serviceDuration)).toMinutes()
        }

    val drivingTimeSecondsFromPreviousStandstill: Long
        get() {
            if (vehicle == null) {
                throw IllegalStateException(
                    "This method must not be called when the shadow variables are not initialized yet."
                )
            }
            if (previousVisit == null) {
                return vehicle!!.homeLocation.getDrivingTimeTo(location)
            }
            return previousVisit!!.location.getDrivingTimeTo((location))
        }

    override fun toString(): String {
        return id
    }
}