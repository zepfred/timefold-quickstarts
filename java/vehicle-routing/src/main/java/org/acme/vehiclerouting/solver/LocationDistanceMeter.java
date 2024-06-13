package org.acme.vehiclerouting.solver;

import ai.timefold.solver.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.geo.HaversineDrivingTimeCalculator;

public class LocationDistanceMeter implements NearbyDistanceMeter<Visit, Vehicle> {
    @Override
    public double getNearbyDistance(Visit origin, Vehicle destination) {
        return HaversineDrivingTimeCalculator.getInstance().calculateDrivingTime(origin.getLocation(), destination.getHomeLocation());
    }
}
