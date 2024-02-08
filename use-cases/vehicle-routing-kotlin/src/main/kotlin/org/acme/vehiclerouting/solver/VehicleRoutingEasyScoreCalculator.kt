package org.acme.vehiclerouting.solver;

import java.util.function.Consumer

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator
import ai.timefold.solver.core.impl.util.MutableInt

import org.acme.vehiclerouting.domain.Vehicle
import org.acme.vehiclerouting.domain.VehicleRoutePlan
import org.acme.vehiclerouting.domain.Visit

class VehicleRoutingEasyScoreCalculator :
    EasyScoreCalculator<VehicleRoutePlan, HardSoftLongScore> {
    override fun calculateScore(vehicleRoutePlan: VehicleRoutePlan): HardSoftLongScore {
        val vehicleList: List<Vehicle>? = vehicleRoutePlan.vehicles

        val hardScore = MutableInt(0)
        var softScore = 0
        for (vehicle in vehicleList!!) {
            // The demand exceeds the capacity

            if (vehicle.visits != null && vehicle.totalDemand > vehicle.capacity) {
                hardScore.setValue((hardScore.toInt() - (vehicle.totalDemand - vehicle.capacity)).toInt())
            }

            // Max end-time not meted
            vehicle.visits!!.forEach(Consumer { visit: Visit ->
                if (visit.isServiceFinishedAfterMaxEndTime) {
                    hardScore.setValue((hardScore.toInt() - visit.serviceFinishedDelayInMinutes).toInt())
                }
            })

            softScore -= vehicle.totalDrivingTimeSeconds.toInt()
        }

        return HardSoftLongScore.of(hardScore.toInt().toLong(), softScore.toLong())
    }
}