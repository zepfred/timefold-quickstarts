package org.acme.vehiclerouting.solver

import java.time.LocalDateTime

import ai.timefold.solver.core.api.domain.variable.VariableListener
import ai.timefold.solver.core.api.score.director.ScoreDirector

import org.acme.vehiclerouting.domain.Visit
import org.acme.vehiclerouting.domain.VehicleRoutePlan

class ArrivalTimeUpdatingVariableListener : VariableListener<VehicleRoutePlan?, Visit> {

    override fun beforeVariableChanged(scoreDirector: ScoreDirector<VehicleRoutePlan?>, visit: Visit) {
    }

    override fun afterVariableChanged(scoreDirector: ScoreDirector<VehicleRoutePlan?>, visit: Visit) {
        if (visit.getVehicle() == null) {
            if (visit.arrivalTime != null) {
                scoreDirector.beforeVariableChanged(visit, ARRIVAL_TIME_FIELD)
                visit.arrivalTime = null
                scoreDirector.afterVariableChanged(visit, ARRIVAL_TIME_FIELD)
            }
            return
        }

        val previousVisit: Visit? = visit.previousVisit
        var departureTime: LocalDateTime? =
            if (previousVisit == null) visit.getVehicle()!!.departureTime else previousVisit.departureTime

        var nextVisit: Visit? = visit
        var arrivalTime = calculateArrivalTime(nextVisit, departureTime)
        while (nextVisit != null && nextVisit.arrivalTime != arrivalTime) {
            scoreDirector.beforeVariableChanged(nextVisit, ARRIVAL_TIME_FIELD)
            nextVisit.arrivalTime = arrivalTime
            scoreDirector.afterVariableChanged(nextVisit, ARRIVAL_TIME_FIELD)
            departureTime = nextVisit.departureTime
            nextVisit = nextVisit.nextVisit
            arrivalTime = calculateArrivalTime(nextVisit, departureTime)
        }
    }

    override fun beforeEntityAdded(scoreDirector: ScoreDirector<VehicleRoutePlan?>?, visit: Visit?) {
    }

    override fun afterEntityAdded(scoreDirector: ScoreDirector<VehicleRoutePlan?>?, visit: Visit?) {
    }

    override fun beforeEntityRemoved(scoreDirector: ScoreDirector<VehicleRoutePlan?>?, visit: Visit?) {
    }

    override fun afterEntityRemoved(scoreDirector: ScoreDirector<VehicleRoutePlan?>?, visit: Visit?) {
    }

    private fun calculateArrivalTime(visit: Visit?, previousDepartureTime: LocalDateTime?): LocalDateTime? {
        if (visit == null || previousDepartureTime == null) {
            return null
        }
        return previousDepartureTime.plusSeconds(visit.drivingTimeSecondsFromPreviousStandstill)
    }


    companion object {
        private const val ARRIVAL_TIME_FIELD = "arrivalTime"
    }
}