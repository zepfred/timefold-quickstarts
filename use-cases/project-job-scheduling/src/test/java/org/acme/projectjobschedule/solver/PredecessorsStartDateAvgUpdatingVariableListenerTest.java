package org.acme.projectjobschedule.solver;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import ai.timefold.solver.core.api.solver.change.ProblemChange;
import ai.timefold.solver.core.api.solver.change.ProblemChangeDirector;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class PredecessorsStartDateAvgUpdatingVariableListenerTest {

    @Inject
    SolverConfig solverConfig;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        // Load the problem
        ProjectJobSchedule problem = given()
                .when().get("/demo-data")
                .then()
                .statusCode(200)
                .extract()
                .as(ProjectJobSchedule.class);

        // First run
        SolverConfig firstConfig = solverConfig.copyConfig();
        firstConfig.withTerminationSpentLimit(Duration.ofSeconds(10))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<ProjectJobSchedule> solverFactory = SolverFactory.create(firstConfig);
        Solver<ProjectJobSchedule> solver = solverFactory.buildSolver();
        ProjectJobSchedule solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();

        // Second run
        SolverConfig secondConfig = solverConfig.copyConfig();
        secondConfig.withTerminationSpentLimit(Duration.ofSeconds(10))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverManager<ProjectJobSchedule, String> solverManager = SolverManager.create(secondConfig);
        SolverJob<ProjectJobSchedule, String> job = solverManager.solveAndListen("1", solution, ignore -> {
        });
        Allocation allocation = solution.getAllocations().get(1);
        AllocationChange allocationChange = new AllocationChange(allocation);
        job.addProblemChange(allocationChange).get();

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> job.getSolverStatus() == SolverStatus.NOT_SOLVING);

        // Changing the delay, trigger the listeners, and updates the best solution and the score becomes different
        assertThat(job.getFinalBestSolution().getScore()).isNotEqualTo(solution.getScore());
    }

    public class AllocationChange implements ProblemChange<ProjectJobSchedule> {

        private Allocation allocation;

        public AllocationChange(Allocation allocation) {
            this.allocation = allocation;
        }

        @Override
        public void doChange(ProjectJobSchedule workingSolution, ProblemChangeDirector problemChangeDirector) {
            problemChangeDirector.changeVariable(allocation, "delay", workingCall -> workingCall.setDelay(500));
        }
    }
}
