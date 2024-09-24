package org.acme.schooltimetabling.solver;

import java.util.List;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.partitionedsearch.partitioner.SolutionPartitioner;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;

import org.acme.schooltimetabling.domain.Timetable;

public class SolverSolutionPartitioner implements SolutionPartitioner<Timetable> {
    @Override
    public List<Timetable> splitWorkingSolution(ScoreDirector<Timetable> scoreDirector, Integer integer) {
        Timetable timetable = ((InnerScoreDirector<Timetable, ?>) scoreDirector).cloneWorkingSolution();
        timetable.getLessons().forEach(lesson -> {
            lesson.setRoom(null);
            lesson.setTimeslot(null);
        });
        return List.of(timetable);
    }
}
