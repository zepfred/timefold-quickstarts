package org.acme.vehiclerouting.solver.filter;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.list.ListChangeMove;

import org.acme.vehiclerouting.domain.Shipment;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;

public class ListChangeMoveShipmentFilter implements SelectionFilter<VehicleRoutePlan, ListChangeMove<VehicleRoutePlan>> {
    @Override
    public boolean accept(ScoreDirector<VehicleRoutePlan> scoreDirector, ListChangeMove<VehicleRoutePlan> move) {
        var visitIterator = move.getPlanningValues().iterator();
        if (visitIterator.hasNext()) {
            Visit visit = (Visit) visitIterator.next();
            if (visit != null) {
                Shipment shipment = visit.getShipment();
                Visit otherVisit = visit.isPickup() ? shipment.getDeliveryVisit() : shipment.getPickupVisit();
                // We check if the destination vehicle has the other visit
                Vehicle vehicle = (Vehicle) move.getDestinationEntity();
                return vehicle.getVisits().contains(otherVisit);
            }
        }
        return true;
    }
}
