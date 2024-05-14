package org.acme.vehiclerouting.solver.move;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.AbstractMove;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;

import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;

public class CustomListSwapMove extends AbstractMove<VehicleRoutePlan> {

    private final Visit leftPickupVisit;
    private final Visit leftDeliveryVisit;
    private final Visit rightPickupVisit;
    private final Visit rightDeliveryVisit;

    public CustomListSwapMove(Visit leftPickupVisit, Visit leftDeliveryVisit, Visit rightPickupVisit,
            Visit rightDeliveryVisit) {
        this.leftPickupVisit = leftPickupVisit;
        this.leftDeliveryVisit = leftDeliveryVisit;
        this.rightPickupVisit = rightPickupVisit;
        this.rightDeliveryVisit = rightDeliveryVisit;
    }

    @Override
    protected Move<VehicleRoutePlan> createUndoMove(ScoreDirector<VehicleRoutePlan> scoreDirector) {
        return new CustomListSwapMove(rightPickupVisit, rightDeliveryVisit, leftPickupVisit, leftDeliveryVisit);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<VehicleRoutePlan> scoreDirector) {
        InnerScoreDirector<VehicleRoutePlan, HardSoftLongScore> innerScoreDirector =
                (InnerScoreDirector<VehicleRoutePlan, HardSoftLongScore>) scoreDirector;

        ListVariableDescriptor<VehicleRoutePlan> listVariableDescriptor = innerScoreDirector.getSolutionDescriptor()
                .findEntityDescriptorOrFail(Vehicle.class).getGenuineListVariableDescriptor();

        // Pickup
        if (leftPickupVisit.getVehicle() == rightPickupVisit.getVehicle()) {
            int fromIndex = Math.min(leftPickupVisit.getVehicleIndex(), rightPickupVisit.getVehicleIndex());
            int toIndex = Math.max(leftPickupVisit.getVehicleIndex(), rightPickupVisit.getVehicleIndex()) + 1;
            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, leftPickupVisit.getVehicle(), fromIndex,
                    toIndex);
            listVariableDescriptor.setElement(leftPickupVisit.getVehicle(), leftPickupVisit.getVehicleIndex(),
                    rightPickupVisit);
            listVariableDescriptor.setElement(rightPickupVisit.getVehicle(), rightPickupVisit.getVehicleIndex(),
                    leftPickupVisit);
            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, leftPickupVisit.getVehicle(), fromIndex,
                    toIndex);
        } else {
            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, leftPickupVisit.getVehicle(),
                    leftPickupVisit.getVehicleIndex(), leftPickupVisit.getVehicleIndex() + 1);
            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, rightPickupVisit.getVehicle(),
                    rightPickupVisit.getVehicleIndex(), rightPickupVisit.getVehicleIndex() + 1);
            listVariableDescriptor.setElement(leftPickupVisit.getVehicle(), leftPickupVisit.getVehicleIndex(),
                    rightPickupVisit);
            listVariableDescriptor.setElement(rightPickupVisit.getVehicle(), rightPickupVisit.getVehicleIndex(),
                    leftPickupVisit);
            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, leftPickupVisit.getVehicle(),
                    leftPickupVisit.getVehicleIndex(), leftPickupVisit.getVehicleIndex() + 1);
            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, rightPickupVisit.getVehicle(),
                    rightPickupVisit.getVehicleIndex(), rightPickupVisit.getVehicleIndex() + 1);
        }
        // Delivery
        if (leftDeliveryVisit.getVehicle() == rightDeliveryVisit.getVehicle()) {
            int fromIndex = Math.min(leftDeliveryVisit.getVehicleIndex(), rightDeliveryVisit.getVehicleIndex());
            int toIndex = Math.max(leftDeliveryVisit.getVehicleIndex(), rightDeliveryVisit.getVehicleIndex()) + 1;
            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, leftDeliveryVisit.getVehicle(), fromIndex,
                    toIndex);
            listVariableDescriptor.setElement(leftDeliveryVisit.getVehicle(), leftDeliveryVisit.getVehicleIndex(),
                    rightDeliveryVisit);
            listVariableDescriptor.setElement(rightDeliveryVisit.getVehicle(), rightDeliveryVisit.getVehicleIndex(),
                    leftDeliveryVisit);
            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, leftDeliveryVisit.getVehicle(), fromIndex,
                    toIndex);
        } else {
            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, leftDeliveryVisit.getVehicle(),
                    leftDeliveryVisit.getVehicleIndex(), leftDeliveryVisit.getVehicleIndex() + 1);
            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, rightDeliveryVisit.getVehicle(),
                    rightDeliveryVisit.getVehicleIndex(), rightDeliveryVisit.getVehicleIndex() + 1);
            listVariableDescriptor.setElement(leftDeliveryVisit.getVehicle(), leftDeliveryVisit.getVehicleIndex(),
                    rightDeliveryVisit);
            listVariableDescriptor.setElement(rightDeliveryVisit.getVehicle(), rightDeliveryVisit.getVehicleIndex(),
                    leftDeliveryVisit);
            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, leftDeliveryVisit.getVehicle(),
                    leftDeliveryVisit.getVehicleIndex(), leftDeliveryVisit.getVehicleIndex() + 1);
            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, rightDeliveryVisit.getVehicle(),
                    rightDeliveryVisit.getVehicleIndex(), rightDeliveryVisit.getVehicleIndex() + 1);
        }
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<VehicleRoutePlan> scoreDirector) {
        return !(leftPickupVisit == rightPickupVisit && leftDeliveryVisit == rightDeliveryVisit);
    }
}
