package org.acme.schooltimetabling.solver;

import java.util.List;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.partitionedsearch.partitioner.SolutionPartitioner;

import org.acme.schooltimetabling.domain.Timetable;

public class SolverSolutionPartitioner implements SolutionPartitioner<Timetable> {
    @Override
    public List<Timetable> splitWorkingSolution(ScoreDirector<Timetable> scoreDirector, Integer integer) {
        return List.of(scoreDirector.getWorkingSolution());
    }
}
