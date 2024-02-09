package org.acme.vehiclerouting.domain

import java.time.LocalDateTime
import java.util.ArrayList

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.lookup.PlanningId
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable

@PlanningEntity
class Vehicle {
    @PlanningId
    lateinit var id: String
    var capacity: Int = 0
    lateinit var homeLocation: Location
    lateinit var departureTime: LocalDateTime

    @PlanningListVariable
    var visits: MutableList<Visit>? = null

    constructor()

    constructor(id: String, capacity: Int, homeLocation: Location, departureTime: LocalDateTime) {
        this.id = id
        this.capacity = capacity
        this.homeLocation = homeLocation
        this.departureTime = departureTime
        this.visits = ArrayList()
    }

    val totalDemand: Long
        get() {
            var totalDemand = 0L
            for (visit in visits!!) {
                totalDemand += visit.demand
            }
            return totalDemand
        }

    val totalDrivingTimeSeconds: Long
        get() {
            if (visits!!.isEmpty()) {
                return 0
            }

            var totalDrivingTime: Long = 0
            var previousLocation = homeLocation

            for (visit in visits!!) {
                totalDrivingTime += previousLocation.getDrivingTimeTo(visit.location!!)
                previousLocation = visit.location!!
            }
            totalDrivingTime += previousLocation.getDrivingTimeTo(homeLocation)

            return totalDrivingTime
        }

    override fun toString(): String {
        return id
    }
}