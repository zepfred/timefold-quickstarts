package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.factory.MoveIteratorFactoryConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.list.ListChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.list.ListSwapMoveSelectorConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.solver.factory.CustomListChangeMoveFactory;
import org.acme.vehiclerouting.solver.factory.CustomListSwapMoveFactory;
import org.acme.vehiclerouting.solver.filter.ListChangeMoveShipmentFilter;
import org.acme.vehiclerouting.solver.filter.ListSwapMoveShipmentFilter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VehicleRoutingTest {

    @Inject
    SolverConfig solverConfig;
    @Inject
    PlannerBenchmarkFactory benchmarkFactory;

    @Test
    void solve() {
        // Load the problem
        VehicleRoutePlan problem = given()
                .when().get("/demo-data/HARTFORT")
                .then()
                .statusCode(200)
                .extract()
                .as(VehicleRoutePlan.class);

        // generic moves
        SolverConfig updatedConfig = solverConfig.copyConfig();
        updatedConfig.withEnvironmentMode(EnvironmentMode.FAST_ASSERT)
                .withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        SolverFactory<VehicleRoutePlan> solverFactory = SolverFactory.create(updatedConfig);
        Solver<VehicleRoutePlan> solver = solverFactory.buildSolver();
        VehicleRoutePlan solution = solver.solve(problem);
        assertThat(solution.getScore()).isNotNull();
        assertThat(solution.getScore().hardScore()).isZero();

        // generic moves and filters
        SolverConfig updatedConfig2 = solverConfig.copyConfig();
        updatedConfig2
                .withEnvironmentMode(EnvironmentMode.FAST_ASSERT)
                .withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        LocalSearchPhaseConfig localSearchPhaseConfig = new LocalSearchPhaseConfig();
        UnionMoveSelectorConfig unionMoveSelectorConfig = new UnionMoveSelectorConfig();
        unionMoveSelectorConfig
                .setMoveSelectorList(
                        List.of(new ListChangeMoveSelectorConfig().withFilterClass(ListChangeMoveShipmentFilter.class),
                                new ListSwapMoveSelectorConfig().withFilterClass(ListSwapMoveShipmentFilter.class)));
        localSearchPhaseConfig.setMoveSelectorConfig(unionMoveSelectorConfig);
        updatedConfig2.setPhaseConfigList(List.of(new ConstructionHeuristicPhaseConfig(), localSearchPhaseConfig));
        SolverFactory<VehicleRoutePlan> solverFactory2 = SolverFactory.create(updatedConfig2);
        Solver<VehicleRoutePlan> solver2 = solverFactory2.buildSolver();
        VehicleRoutePlan solution2 = solver2.solve(problem);
        assertThat(solution2.getScore()).isNotNull();
        assertThat(solution2.getScore().hardScore()).isZero();

        // custom moves
        SolverConfig updatedConfig3 = solverConfig.copyConfig();
        updatedConfig3
                .withEnvironmentMode(EnvironmentMode.FAST_ASSERT)
                .withTerminationSpentLimit(Duration.ofSeconds(30))
                .getTerminationConfig().withBestScoreLimit(null);
        LocalSearchPhaseConfig localSearchPhaseConfig2 = new LocalSearchPhaseConfig();
        UnionMoveSelectorConfig unionMoveSelectorConfig2 = new UnionMoveSelectorConfig();
        unionMoveSelectorConfig2
                .setMoveSelectorList(
                        List.of(new MoveIteratorFactoryConfig()
                                .withMoveIteratorFactoryClass(CustomListChangeMoveFactory.class),
                                new MoveIteratorFactoryConfig()
                                        .withMoveIteratorFactoryClass(CustomListSwapMoveFactory.class)));
        localSearchPhaseConfig2.setMoveSelectorConfig(unionMoveSelectorConfig2);
        updatedConfig3.setPhaseConfigList(List.of(new ConstructionHeuristicPhaseConfig(), localSearchPhaseConfig2));
        SolverFactory<VehicleRoutePlan> solverFactory3 = SolverFactory.create(updatedConfig3);
        Solver<VehicleRoutePlan> solver3 = solverFactory3.buildSolver();
        VehicleRoutePlan solution3 = solver3.solve(problem);
        assertThat(solution3.getScore()).isNotNull();
        assertThat(solution3.getScore().hardScore()).isZero();
    }

    @Test
    @Disabled
    void benchmark() {
        VehicleRoutePlan problem = given()
                .when().get("/demo-data/HARTFORT")
                .then()
                .statusCode(200)
                .extract()
                .as(VehicleRoutePlan.class);

        benchmarkFactory.buildPlannerBenchmark(problem).benchmark();
    }
}