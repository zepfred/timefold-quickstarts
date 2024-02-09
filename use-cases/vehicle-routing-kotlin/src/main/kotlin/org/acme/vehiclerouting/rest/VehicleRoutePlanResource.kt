package org.acme.vehiclerouting.rest;

import java.util.UUID
import java.util.concurrent.ExecutionException

import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

import ai.timefold.solver.core.api.solver.SolverManager

import org.acme.vehiclerouting.domain.VehicleRoutePlan

@Path("route-plans")
class VehicleRoutePlanResource {
    private val solverManager: SolverManager<VehicleRoutePlan, String>?

    constructor() {
        this.solverManager = null
    }

    @Inject
    constructor(solverManager: SolverManager<VehicleRoutePlan, String>?) {
        this.solverManager = solverManager
    }

    @POST
    @Path("solve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun solve(problem: VehicleRoutePlan): VehicleRoutePlan {
        val jobId = UUID.randomUUID().toString()
        val solverJob = solverManager!!.solveBuilder()
            .withProblemId(jobId)
            .withProblem(problem)
            .run()
        val solution: VehicleRoutePlan
        try {
            // Wait until the solving ends
            solution = solverJob.finalBestSolution
        } catch (e: InterruptedException) {
            throw IllegalStateException("Solving failed.", e)
        } catch (e: ExecutionException) {
            throw IllegalStateException("Solving failed.", e)
        }
        return solution
    }
}