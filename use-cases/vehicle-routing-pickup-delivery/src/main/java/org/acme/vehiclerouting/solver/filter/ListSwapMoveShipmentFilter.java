package org.acme.vehiclerouting.solver.filter;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.list.ListSwapMove;

import org.acme.vehiclerouting.domain.Shipment;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;

public class ListSwapMoveShipmentFilter implements SelectionFilter<VehicleRoutePlan, Move<VehicleRoutePlan>> {
    @Override
    public boolean accept(ScoreDirector<VehicleRoutePlan> scoreDirector, Move<VehicleRoutePlan> move) {
        if (move instanceof ListSwapMove<VehicleRoutePlan> listSwapMove) {
            Vehicle leftVehicle = (Vehicle) listSwapMove.getLeftEntity();
            Visit leftVisit = leftVehicle.getVisits().get(listSwapMove.getLeftIndex());
            Vehicle rightVehicle = (Vehicle) listSwapMove.getRightEntity();
            Visit rightVisit = rightVehicle.getVisits().get(listSwapMove.getRightIndex());
            Shipment leftShipment = leftVisit.getShipment();
            Visit otherLeftVisit = leftVisit.isPickup() ? leftShipment.getDeliveryVisit() : leftShipment.getPickupVisit();
            Shipment rightShipment = rightVisit.getShipment();
            Visit otherRightVisit = rightVisit.isPickup() ? rightShipment.getDeliveryVisit() : rightShipment.getPickupVisit();
            return leftVehicle.getVisits().contains(otherRightVisit) && rightVehicle.getVisits().contains(otherLeftVisit);
        }
        return true;
    }
}
