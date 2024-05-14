package org.acme.vehiclerouting.solver.factory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.CompositeMove;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.list.ListChangeMove;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;

import org.acme.vehiclerouting.domain.Shipment;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;

public class CustomListChangeMoveFactory implements MoveIteratorFactory<VehicleRoutePlan, Move<VehicleRoutePlan>> {
    private ListVariableDescriptor<VehicleRoutePlan> listVariableDescriptor;

    @Override
    public long getSize(ScoreDirector<VehicleRoutePlan> scoreDirector) {
        var solution = scoreDirector.getWorkingSolution();
        var changeVehicle = solution.getShipments().size() * (solution.getVehicles().size() - 1);
        // permutation(visits, 2) / 2, as we don't allow deliveries before pickups
        var changesPerVehicle = getChangesPerVehicle(solution.getVisits().size());
        return changeVehicle + changesPerVehicle;
    }

    private static long getChangesPerVehicle(int visitsSize) {
        return factorial(visitsSize).divide(factorial(visitsSize - 2)).divide(BigInteger.TWO).longValue();
    }

    private static BigInteger factorial(int n) {
        var result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    @Override
    public Iterator<Move<VehicleRoutePlan>> createOriginalMoveIterator(ScoreDirector<VehicleRoutePlan> scoreDirector) {
        if (listVariableDescriptor == null) {
            InnerScoreDirector<VehicleRoutePlan, HardSoftLongScore> innerScoreDirector =
                    (InnerScoreDirector<VehicleRoutePlan, HardSoftLongScore>) scoreDirector;
            this.listVariableDescriptor = innerScoreDirector.getSolutionDescriptor().findEntityDescriptorOrFail(Vehicle.class)
                    .getGenuineListVariableDescriptor();
        }
        return new CompleteListChangeMoveIterator(listVariableDescriptor, scoreDirector.getWorkingSolution().getShipments(),
                scoreDirector.getWorkingSolution().getVehicles(),
                scoreDirector.getWorkingSolution().getVisits().stream()
                        .collect(Collectors.toMap(Visit::getId, Function.identity())),
                (int) getSize(scoreDirector));
    }

    @Override
    public Iterator<Move<VehicleRoutePlan>> createRandomMoveIterator(ScoreDirector<VehicleRoutePlan> scoreDirector,
            Random workingRandom) {
        if (listVariableDescriptor == null) {
            InnerScoreDirector<VehicleRoutePlan, HardSoftLongScore> innerScoreDirector =
                    (InnerScoreDirector<VehicleRoutePlan, HardSoftLongScore>) scoreDirector;
            this.listVariableDescriptor = innerScoreDirector.getSolutionDescriptor().findEntityDescriptorOrFail(Vehicle.class)
                    .getGenuineListVariableDescriptor();
        }
        return new RandomListChangeMoveIterator(listVariableDescriptor, scoreDirector.getWorkingSolution().getShipments(),
                scoreDirector.getWorkingSolution().getVehicles(),
                scoreDirector.getWorkingSolution().getVisits().stream()
                        .collect(Collectors.toMap(Visit::getId, Function.identity())),
                workingRandom);
    }

    private static class CompleteListChangeMoveIterator implements Iterator<Move<VehicleRoutePlan>> {

        private final ListVariableDescriptor<VehicleRoutePlan> listVariableDescriptor;
        private final List<Shipment> shipments;
        private final List<Vehicle> vehicles;
        private final Map<String, Visit> visits;
        private final int estimatedSize;
        private List<Move<VehicleRoutePlan>> moves;
        private int currentIndex;

        public CompleteListChangeMoveIterator(ListVariableDescriptor<VehicleRoutePlan> listVariableDescriptor,
                List<Shipment> shipments, List<Vehicle> vehicles, Map<String, Visit> visits, int estimatedSize) {
            this.listVariableDescriptor = listVariableDescriptor;
            this.shipments = shipments;
            this.vehicles = vehicles;
            this.visits = visits;
            this.estimatedSize = estimatedSize;
        }

        private void initialize() {
            if (moves == null) {
                currentIndex = 0;
                moves = new ArrayList<>(estimatedSize);
                for (Shipment shipment : shipments) {
                    var pickupVisit = visits.get(shipment.getPickupVisit().getId());
                    var deliveryVisit = visits.get(shipment.getDeliveryVisit().getId());
                    for (Vehicle vehicle : vehicles) {
                        moves.addAll(generateMovesPerVehicle(vehicle, pickupVisit, deliveryVisit));
                    }
                }
            }
        }

        private List<Move<VehicleRoutePlan>> generateMovesPerVehicle(Vehicle vehicle, Visit pickupVisit,
                Visit deliveryVisit) {
            List<Move<VehicleRoutePlan>> movesPerVehicle =
                    new ArrayList<>((int) getChangesPerVehicle(vehicle.getVisits().size()));
            for (int i = 0; i < vehicle.getVisits().size() - 1; i++) {
                for (int j = i + 1; j < vehicle.getVisits().size(); j++) {
                    var compositeMove = CompositeMove.buildMove(
                            new ListChangeMove<>(listVariableDescriptor, pickupVisit.getVehicle(),
                                    pickupVisit.getVehicleIndex(), vehicle, i),
                            new ListChangeMove<>(listVariableDescriptor, deliveryVisit.getVehicle(),
                                    deliveryVisit.getVehicleIndex(), vehicle, j));
                    movesPerVehicle.add(compositeMove);
                }
            }
            return movesPerVehicle;
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

    private static class RandomListChangeMoveIterator implements Iterator<Move<VehicleRoutePlan>> {

        private final ListVariableDescriptor<VehicleRoutePlan> listVariableDescriptor;
        private final List<Shipment> shipments;
        private final List<Vehicle> vehicles;
        private final Map<String, Visit> visits;
        private final Random workingRandom;

        public RandomListChangeMoveIterator(ListVariableDescriptor<VehicleRoutePlan> listVariableDescriptor,
                List<Shipment> shipments, List<Vehicle> vehicles, Map<String, Visit> visits, Random workingRandom) {
            this.listVariableDescriptor = listVariableDescriptor;
            this.shipments = shipments;
            this.vehicles = vehicles;
            this.visits = visits;
            this.workingRandom = workingRandom;
        }

        @Override
        public boolean hasNext() {
            return !vehicles.isEmpty() && shipments.size() >= 2;
        }

        @Override
        public Move<VehicleRoutePlan> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Shipment shipment = shipments.get(workingRandom.nextInt(shipments.size()));
            Visit originalPickuptVisit = visits.get(shipment.getPickupVisit().getId());
            Visit originalDeliveryVisit = visits.get(shipment.getDeliveryVisit().getId());
            Vehicle vehicle = vehicles.get(workingRandom.nextInt(vehicles.size()));
            var pickupVisitIndex = !vehicle.getVisits().isEmpty() ? workingRandom.nextInt(vehicle.getVisits().size() - 1) : 0;
            var deliveryVisitIndex =
                    !vehicle.getVisits().isEmpty() ? workingRandom.nextInt(pickupVisitIndex + 1, vehicle.getVisits().size())
                            : 1;
            var originalDeliveryVisitIndex = originalDeliveryVisit.getVehicleIndex();
            if (originalPickuptVisit.getVehicle() == originalDeliveryVisit.getVehicle() && originalDeliveryVisitIndex > 0) {
                // The first move removes one item from the list, and we need to adjust the original delivery index
                originalDeliveryVisitIndex--;
            }
            return CompositeMove.buildMove(
                    new ListChangeMove<>(listVariableDescriptor, originalPickuptVisit.getVehicle(),
                            originalPickuptVisit.getVehicleIndex(), vehicle, pickupVisitIndex),
                    new ListChangeMove<>(listVariableDescriptor, originalDeliveryVisit.getVehicle(),
                            originalDeliveryVisitIndex, vehicle, deliveryVisitIndex));
        }
    }
}
