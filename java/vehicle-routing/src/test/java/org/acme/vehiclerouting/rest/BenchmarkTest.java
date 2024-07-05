package org.acme.vehiclerouting.rest;

import jakarta.inject.Inject;

import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
public class BenchmarkTest {
    @Inject
    PlannerBenchmarkFactory benchmarkFactory;

    @Test
    public void benchmark() {
        VehicleRoutePlan plan = new VehicleRouteDemoResource().generate(VehicleRouteDemoResource.DemoData.FIRENZE);
        benchmarkFactory.buildPlannerBenchmark(plan).benchmarkAndShowReportInBrowser();
    }
}
