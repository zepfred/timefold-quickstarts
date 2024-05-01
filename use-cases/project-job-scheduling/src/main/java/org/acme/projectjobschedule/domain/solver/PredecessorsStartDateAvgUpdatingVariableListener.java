package org.acme.projectjobschedule.domain.solver;

import java.util.ArrayDeque;
import java.util.Queue;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;

public class PredecessorsStartDateAvgUpdatingVariableListener implements VariableListener<ProjectJobSchedule, Allocation> {

    @Override
    public void beforeEntityAdded(ScoreDirector<ProjectJobSchedule> scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<ProjectJobSchedule> scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<ProjectJobSchedule> scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector<ProjectJobSchedule> scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<ProjectJobSchedule> scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<ProjectJobSchedule> scoreDirector, Allocation allocation) {
        // Do nothing
    }

    protected void updateAllocation(ScoreDirector<ProjectJobSchedule> scoreDirector, Allocation originalAllocation) {
        // Reset computed variables when a planning variable changes to prevent score corruption
        originalAllocation.invalidateComputedVariables();
        Queue<Allocation> uncheckedSuccessorQueue = new ArrayDeque<>();
        uncheckedSuccessorQueue.addAll(originalAllocation.getSuccessorAllocations());
        while (!uncheckedSuccessorQueue.isEmpty()) {
            Allocation allocation = uncheckedSuccessorQueue.remove();
            boolean updated = updatePredecessorsAvgDate(scoreDirector, allocation);
            if (updated) {
                uncheckedSuccessorQueue.addAll(allocation.getSuccessorAllocations());
            }
        }
    }

    protected boolean updatePredecessorsAvgDate(ScoreDirector<ProjectJobSchedule> scoreDirector, Allocation allocation) {
        double avgDate = allocation.getPredecessorAllocations().stream()
                .mapToInt(Allocation::getStartDate)
                .average()
                .orElse(0);

        scoreDirector.beforeVariableChanged(allocation, "predecessorsStartDateAvg");
        allocation.setPredecessorsStartDateAvg(avgDate);
        scoreDirector.afterVariableChanged(allocation, "predecessorsStartDateAvg");
        return true;
    }
}
