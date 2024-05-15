package org.acme.vehiclerouting.solver.listener;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;

public class WeightAtVisitVariableListener implements VariableListener<VehicleRoutePlan, Visit> {

    private static final String WEIGHT_AT_VISIT_FIELD = "weightAtVisit";

    @Override
    public void beforeVariableChanged(ScoreDirector<VehicleRoutePlan> scoreDirector, Visit visit) {
        // event ignored
    }

    @Override
    public void afterVariableChanged(ScoreDirector<VehicleRoutePlan> scoreDirector, Visit visit) {
        if (visit.getVehicle() == null) {
            scoreDirector.beforeVariableChanged(visit, WEIGHT_AT_VISIT_FIELD);
            visit.setWeightAtVisit(null);
            scoreDirector.afterVariableChanged(visit, WEIGHT_AT_VISIT_FIELD);
            return;
        }

        int weight = 0;
        for (int i = 0; i < visit.getVehicleIndex(); i++) {
            Visit previousVisit = visit.getVehicle().getVisits().get(i);
            if (previousVisit.isPickup()) {
                weight += previousVisit.getShipment().getWeight();
            } else {
                weight -= previousVisit.getShipment().getWeight();
            }
        }
        if (visit.getWeightAtVisit() == null || visit.getWeightAtVisit() != weight) {
            scoreDirector.beforeVariableChanged(visit, WEIGHT_AT_VISIT_FIELD);
            visit.setWeightAtVisit(weight);
            scoreDirector.afterVariableChanged(visit, WEIGHT_AT_VISIT_FIELD);
        }
    }

    @Override
    public void beforeEntityAdded(ScoreDirector<VehicleRoutePlan> scoreDirector, Visit visit) {
        // event ignored
    }

    @Override
    public void afterEntityAdded(ScoreDirector<VehicleRoutePlan> scoreDirector, Visit visit) {
        // event ignored
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<VehicleRoutePlan> scoreDirector, Visit visit) {
        // event ignored
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<VehicleRoutePlan> scoreDirector, Visit visit) {
        // event ignored
    }
}
