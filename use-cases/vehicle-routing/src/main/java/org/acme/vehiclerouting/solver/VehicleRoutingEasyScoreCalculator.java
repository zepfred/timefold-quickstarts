package org.acme.vehiclerouting.solver;

import java.util.List;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;
import ai.timefold.solver.core.impl.util.MutableInt;

import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;

public class VehicleRoutingEasyScoreCalculator implements EasyScoreCalculator<VehicleRoutePlan, HardSoftLongScore> {
    @Override
    public HardSoftLongScore calculateScore(VehicleRoutePlan vehicleRoutePlan) {

        List<Vehicle> vehicleList = vehicleRoutePlan.getVehicles();

        MutableInt hardScore = new MutableInt(0);
        int softScore = 0;
        for (Vehicle vehicle : vehicleList) {

            // The demand exceeds the capacity
            if (vehicle.getVisits() != null && vehicle.getTotalDemand() > vehicle.getCapacity()) {
                hardScore.setValue(hardScore.intValue() - (vehicle.getTotalDemand() - vehicle.getCapacity()));
            }

            // Max end-time not meted
            vehicle.getVisits().forEach(visit -> {
                if (visit.isServiceFinishedAfterMaxEndTime()) {
                    hardScore.setValue((int) (hardScore.intValue() - visit.getServiceFinishedDelayInMinutes()));
                }
            });

            softScore -= (int) vehicle.getTotalDrivingTimeSeconds();
        }

        return HardSoftLongScore.of(hardScore.intValue(), softScore);
    }
}
