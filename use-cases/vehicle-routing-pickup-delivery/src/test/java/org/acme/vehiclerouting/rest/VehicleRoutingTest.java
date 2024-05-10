package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class VehicleRoutingTest {

    @Inject
    SolverConfig solverConfig;

    @Test
    void solve() {
        // Load the problem
        VehicleRoutePlan problem = given()
                .when().get("/demo-data/HARTFORT")
                .then()
                .statusCode(200)
                .extract()
                .as(VehicleRoutePlan.class);

        // Update the environment
        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(EnvironmentMode.TRACKED_FULL_ASSERT)
                .withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<VehicleRoutePlan> solverFactory = SolverFactory.create(updatedConfig);
        SolutionManager<VehicleRoutePlan, HardSoftLongScore> solutionManager = SolutionManager.create(solverFactory);

        // Solve the problem - generic moves
        Solver<VehicleRoutePlan> solver = solverFactory.buildSolver();
        VehicleRoutePlan solution = solver.solve(problem);
        validateSolution(solutionManager, solution);
    }

    private void validateSolution(SolutionManager<VehicleRoutePlan, HardSoftLongScore> solutionManager,
            VehicleRoutePlan solution) {
        assertThat(solution.getScore()).isNotNull();
        assertThat(solution.getScore().hardScore()).isZero();
        ScoreAnalysis<HardSoftLongScore> scoreAnalysis = solutionManager.analyze(solution);
        assertThat(scoreAnalysis).isNotNull();
    }
}