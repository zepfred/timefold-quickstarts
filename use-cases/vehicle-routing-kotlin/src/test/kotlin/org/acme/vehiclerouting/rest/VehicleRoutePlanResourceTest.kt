package org.acme.vehiclerouting.rest

import java.time.Duration

import ai.timefold.solver.core.api.solver.SolverStatus

import org.acme.vehiclerouting.domain.VehicleRoutePlan
import org.junit.jupiter.api.Test

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType

import org.awaitility.Awaitility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

@QuarkusTest
class VehicleRoutePlanResourceTest {
    @Test
    fun solveDemoDataUntilFeasible() {
        val vehicleRoutePlan = RestAssured.given()
            .`when`()["/demo-data/FIRENZE"]
            .then()
            .statusCode(200)
            .extract()
            .`as`(VehicleRoutePlan::class.java)

        val jobId = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(vehicleRoutePlan)
            .expect().contentType(ContentType.TEXT)
            .`when`().post("/route-plans")
            .then()
            .statusCode(200)
            .extract()
            .asString()

        Awaitility.await()
            .atMost(Duration.ofMinutes(1))
            .pollInterval(Duration.ofMillis(500L))
            .until {
                SolverStatus.NOT_SOLVING.name == RestAssured.get("/route-plans/$jobId/status")
                    .jsonPath().get("solverStatus")
            }

        val solution = RestAssured.get("/route-plans/$jobId").then().extract().`as`(
            VehicleRoutePlan::class.java
        )
        assertEquals(solution.solverStatus, SolverStatus.NOT_SOLVING)
        assertNotNull(solution.vehicles)
        assertNotNull(solution.visits)
        assertNotNull(solution.vehicles!!.get(0).visits)
        assertTrue(solution.score!!.isFeasible())
    }
}