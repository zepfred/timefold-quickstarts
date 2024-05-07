package org.acme.vehiclerouting.solver;

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
            if (visit.getArrivalTime() != null) {
                scoreDirector.beforeVariableChanged(visit, WEIGHT_AT_VISIT_FIELD);
                visit.setWeightAtVisit(null);
                scoreDirector.afterVariableChanged(visit, WEIGHT_AT_VISIT_FIELD);
            }
            return;
        }

        int weight = visit.getVehicle().getVisits().subList(0, visit.getVehicleIndex()).stream()
                .mapToInt(v -> v.isPickup() ? v.getShipment().getWeight() : Math.negateExact(v.getShipment().getWeight()))
                .sum();
        scoreDirector.beforeVariableChanged(visit, WEIGHT_AT_VISIT_FIELD);
        visit.setWeightAtVisit(weight);
        scoreDirector.afterVariableChanged(visit, WEIGHT_AT_VISIT_FIELD);
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
