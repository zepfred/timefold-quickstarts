package org.acme.vehiclerouting.solver.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;

import org.acme.vehiclerouting.domain.Shipment;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.solver.move.CustomListSwapMove;

public class CustomListSwapMoveFactory implements MoveIteratorFactory<VehicleRoutePlan, Move<VehicleRoutePlan>> {
    @Override
    public long getSize(ScoreDirector<VehicleRoutePlan> scoreDirector) {
        return (long) scoreDirector.getWorkingSolution().getShipments().size()
                * scoreDirector.getWorkingSolution().getShipments().size();
    }

    @Override
    public Iterator<Move<VehicleRoutePlan>> createOriginalMoveIterator(ScoreDirector<VehicleRoutePlan> scoreDirector) {
        return new CompleteListSwapMoveIterator(scoreDirector.getWorkingSolution().getShipments(), scoreDirector
                .getWorkingSolution().getVisits().stream().collect(Collectors.toMap(Visit::getId, Function.identity())),
                (int) getSize(scoreDirector));
    }

    @Override
    public Iterator<Move<VehicleRoutePlan>> createRandomMoveIterator(ScoreDirector<VehicleRoutePlan> scoreDirector,
            Random workingRandom) {
        return new RandomListSwapMoveIterator(scoreDirector.getWorkingSolution().getShipments(), scoreDirector
                .getWorkingSolution().getVisits().stream().collect(Collectors.toMap(Visit::getId, Function.identity())),
                workingRandom);
    }

    private static class CompleteListSwapMoveIterator implements Iterator<Move<VehicleRoutePlan>> {

        private final List<Shipment> shipments;
        private final Map<String, Visit> visits;
        private final int estimatedSize;
        private List<Move<VehicleRoutePlan>> moves;
        private int currentIndex;

        public CompleteListSwapMoveIterator(List<Shipment> shipments, Map<String, Visit> visits, int estimatedSize) {
            this.shipments = shipments;
            this.visits = visits;
            this.estimatedSize = estimatedSize;
        }

        private void initialize() {
            if (moves == null) {
                currentIndex = 0;
                moves = new ArrayList<>(estimatedSize);
                for (int i = 0; i < shipments.size(); i++) {
                    for (int j = i + 1; j < shipments.size(); j++) {
                        if (i == j) {
                            continue;
                        }
                        Visit leftPickupVisit = visits.get(shipments.get(i).getPickupVisit().getId());
                        Visit leftDeliveryVisit = visits.get(shipments.get(i).getDeliveryVisit().getId());
                        Visit rightPickupVisit = visits.get(shipments.get(j).getPickupVisit().getId());
                        Visit rightDeliveryVisit = visits.get(shipments.get(j).getDeliveryVisit().getId());
                        moves.add(new CustomListSwapMove(leftPickupVisit, leftDeliveryVisit, rightPickupVisit,
                                rightDeliveryVisit));
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            initialize();
            return currentIndex < moves.size();
        }

        @Override
        public Move<VehicleRoutePlan> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return moves.get(currentIndex++);
        }
    }

    private static class RandomListSwapMoveIterator implements Iterator<Move<VehicleRoutePlan>> {

        private final List<Shipment> shipments;
        private final Map<String, Visit> visits;
        private final Random workingRandom;

        public RandomListSwapMoveIterator(List<Shipment> shipments, Map<String, Visit> visits, Random workingRandom) {
            this.shipments = shipments;
            this.visits = visits;
            this.workingRandom = workingRandom;
        }

        @Override
        public boolean hasNext() {
            return shipments.size() >= 2;
        }

        @Override
        public Move<VehicleRoutePlan> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Shipment firstShipment = shipments.get(workingRandom.nextInt(shipments.size()));
            Shipment secondShipment = shipments.get(workingRandom.nextInt(shipments.size()));
            while (firstShipment.equals(secondShipment)) {
                secondShipment = shipments.get(workingRandom.nextInt(shipments.size()));
            }
            Visit leftPickupVisit = visits.get(firstShipment.getPickupVisit().getId());
            Visit leftDeliveryVisit = visits.get(firstShipment.getDeliveryVisit().getId());
            Visit rightPickupVisit = visits.get(secondShipment.getPickupVisit().getId());
            Visit rightDeliveryVisit = visits.get(secondShipment.getDeliveryVisit().getId());

            return new CustomListSwapMove(leftPickupVisit, leftDeliveryVisit, rightPickupVisit, rightDeliveryVisit);
        }
    }
}
