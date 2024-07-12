package org.acme.vehiclerouting.rest;

import static org.awaitility.Awaitility.await;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.JsonNode;

@SpringBootTest(properties = {
        // Effectively disable spent-time termination in favor of the best-score-limit
        "timefold.solver.termination.spent-limit=1h",
        "timefold.solver.termination.best-score-limit=0hard/*soft" },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VehicleRoutePlanControllerTest {

    @LocalServerPort
    String port;

    @Test
    void solve() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        VehicleRoutePlan problem = client.get()
                .uri("/demo-data/FIRENZE")
                .exchange()
                .expectBody(VehicleRoutePlan.class)
                .returnResult()
                .getResponseBody();

        String jobId = client.post()
                .uri("/route-plans")
                .bodyValue(problem)
                .exchange()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        client.get()
                                .uri("/route-plans/" + jobId + "/status")
                                .exchange()
                                .expectBody(JsonNode.class)
                                .returnResult()
                                .getResponseBody()
                                .get("solverStatus")
                                .asText()));

        client.get()
                .uri("/route-plans/" + jobId)
                .exchange()
                .expectBody()
                .jsonPath("solverStatus").isEqualTo("NOT_SOLVING")
                .jsonPath("score").isNotEmpty();
    }

}