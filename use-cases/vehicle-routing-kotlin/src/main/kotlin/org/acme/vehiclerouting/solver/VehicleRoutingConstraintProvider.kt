package org.acme.vehiclerouting.solver

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore
import ai.timefold.solver.core.api.score.stream.Constraint
import ai.timefold.solver.core.api.score.stream.ConstraintFactory
import ai.timefold.solver.core.api.score.stream.ConstraintProvider

import org.acme.vehiclerouting.domain.Visit
import org.acme.vehiclerouting.domain.Vehicle
import org.acme.vehiclerouting.solver.justifications.MinimizeTravelTimeJustification
import org.acme.vehiclerouting.solver.justifications.ServiceFinishedAfterMaxEndTimeJustification
import org.acme.vehiclerouting.solver.justifications.VehicleCapacityJustification

class VehicleRoutingConstraintProvider : ConstraintProvider {
    override fun defineConstraints(factory: ConstraintFactory): Array<Constraint> {
        return arrayOf(
            vehicleCapacity(factory),
            serviceFinishedAfterMaxEndTime(factory),
            minimizeTravelTime(factory)
        )
    }

    protected fun vehicleCapacity(factory: ConstraintFactory): Constraint {
        return factory.forEach(Vehicle::class.java)
            .filter({ vehicle: Vehicle -> vehicle.totalDemand > vehicle.capacity })
            .penalizeLong(
                HardSoftLongScore.ONE_HARD
            ) { vehicle: Vehicle -> vehicle.totalDemand - vehicle.capacity }
            .justifyWith({ vehicle: Vehicle, score: HardSoftLongScore? ->
                VehicleCapacityJustification(
                    vehicle.id, vehicle.totalDemand.toInt(),
                    vehicle.capacity
                )
            })
            .asConstraint(VEHICLE_CAPACITY)
    }

    protected fun serviceFinishedAfterMaxEndTime(factory: ConstraintFactory): Constraint {
        return factory.forEach(Visit::class.java)
            .filter({ obj: Visit -> obj.isServiceFinishedAfterMaxEndTime })
            .penalizeLong(HardSoftLongScore.ONE_HARD,
                { obj: Visit -> obj.serviceFinishedDelayInMinutes })
            .justifyWith({ visit: Visit, score: HardSoftLongScore? ->
                ServiceFinishedAfterMaxEndTimeJustification(
                    visit.id,
                    visit.serviceFinishedDelayInMinutes
                )
            })
            .asConstraint(SERVICE_FINISHED_AFTER_MAX_END_TIME)
    }

    protected fun minimizeTravelTime(factory: ConstraintFactory): Constraint {
        return factory.forEach(Vehicle::class.java)
            .penalizeLong(HardSoftLongScore.ONE_SOFT,
                { obj: Vehicle -> obj.totalDrivingTimeSeconds })
            .justifyWith({ vehicle: Vehicle, score: HardSoftLongScore? ->
                MinimizeTravelTimeJustification(
                    vehicle.id,
                    vehicle.totalDrivingTimeSeconds
                )
            })
            .asConstraint(MINIMIZE_TRAVEL_TIME)
    }

    companion object {
        const val VEHICLE_CAPACITY: String = "vehicleCapacity"
        const val SERVICE_FINISHED_AFTER_MAX_END_TIME: String = "serviceFinishedAfterMaxEndTime"
        const val MINIMIZE_TRAVEL_TIME: String = "minimizeTravelTime"
    }
}