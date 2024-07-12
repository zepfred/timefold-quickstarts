package org.acme.vehiclerouting.rest;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.rest.exception.ErrorInfo;
import org.acme.vehiclerouting.rest.exception.VehicleRoutingSolverException;
import org.acme.vehiclerouting.solver.ArrivalTimeUpdatingVariableListener;
import org.acme.vehiclerouting.solver.justifications.MinimizeTravelTimeJustification;
import org.acme.vehiclerouting.solver.justifications.ServiceFinishedAfterMaxEndTimeJustification;
import org.acme.vehiclerouting.solver.justifications.VehicleCapacityJustification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "School VehicleRoutePlans",
        description = "School VehicleRoutePlan service assigning lessons to rooms and timeslots.")
@RestController
@RequestMapping("/route-plans")
//@RegisterReflectionForBinding({ArrivalTimeUpdatingVariableListener.class, ReflectionMethodMemberAccessor.class, Visit.class})
@RegisterReflectionForBinding({ArrivalTimeUpdatingVariableListener.class})
public class VehicleRoutePlanController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleRoutePlanController.class);

    private final SolverManager<VehicleRoutePlan, String> solverManager;
    private final SolutionManager<VehicleRoutePlan, HardSoftScore> solutionManager;

    // TODO: Without any "time to live", the map may eventually grow out of memory.
    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    public VehicleRoutePlanController(SolverManager<VehicleRoutePlan, String> solverManager,
                                      SolutionManager<VehicleRoutePlan, HardSoftScore> solutionManager) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    @Operation(summary = "List the job IDs of all submitted VehicleRoutePlans.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all job IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "array", implementation = String.class)))})
    @GetMapping
    public Collection<String> list() {
        return jobIdToJob.keySet();
    }

    @Operation(summary = "Submit a VehicleRoutePlan to start solving as soon as CPU resources are available.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202",
                    description = "The job ID. Use that ID to get the solution with the other methods.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = String.class)))})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String solve(@RequestBody VehicleRoutePlan problem) {
        String jobId = UUID.randomUUID().toString();
        jobIdToJob.put(jobId, Job.ofVehicleRoutePlan(problem));
        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblemFinder(jobId_ -> jobIdToJob.get(jobId).VehicleRoutePlan)
                .withBestSolutionConsumer(solution -> jobIdToJob.put(jobId, Job.ofVehicleRoutePlan(solution)))
                .withExceptionHandler((jobId_, exception) -> {
                    jobIdToJob.put(jobId, Job.ofException(exception));
                    LOGGER.error("Failed solving jobId ({}).", jobId, exception);
                })
                .run();
        return jobId;
    }

    @Operation(summary = "Submit a VehicleRoutePlan to analyze its score.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202",
                    description = "Resulting score analysis, optionally without constraint matches.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ScoreAnalysis.class)))})
    @PutMapping(value = "/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RegisterReflectionForBinding({
            MinimizeTravelTimeJustification.class,
            ServiceFinishedAfterMaxEndTimeJustification.class,
            VehicleCapacityJustification.class,
    })
    public ScoreAnalysis<HardSoftScore> analyze(@RequestBody VehicleRoutePlan problem,
                                                @RequestParam(name = "fetchPolicy", required = false) ScoreAnalysisFetchPolicy fetchPolicy) {
        return fetchPolicy == null ? solutionManager.analyze(problem) : solutionManager.analyze(problem, fetchPolicy);
    }

    @Operation(
            summary = "Get the solution and score for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The best solution of the VehicleRoutePlan so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VehicleRoutePlan.class))),
            @ApiResponse(responseCode = "404", description = "No VehicleRoutePlan found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "500", description = "Exception during solving a VehicleRoutePlan.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })

    @GetMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VehicleRoutePlan getVehicleRoutePlan(
            @Parameter(description = "The job ID returned by the POST method.") @PathVariable("jobId") String jobId) {
        VehicleRoutePlan VehicleRoutePlan = getVehicleRoutePlanAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        VehicleRoutePlan.setSolverStatus(solverStatus);
        return VehicleRoutePlan;
    }

    @Operation(
            summary = "Get the VehicleRoutePlan status and score for a given job ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The VehicleRoutePlan status and the best score so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VehicleRoutePlan.class))),
            @ApiResponse(responseCode = "404", description = "No VehicleRoutePlan found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "500", description = "Exception during solving a VehicleRoutePlan.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GetMapping(value = "/{jobId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public VehicleRoutePlan getStatus(
            @Parameter(description = "The job ID returned by the POST method.") @PathVariable("jobId") String jobId) {
        VehicleRoutePlan VehicleRoutePlan = getVehicleRoutePlanAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        return new VehicleRoutePlan(VehicleRoutePlan.getName(), VehicleRoutePlan.getScore(), solverStatus);
    }

    private VehicleRoutePlan getVehicleRoutePlanAndCheckForExceptions(String jobId) {
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new VehicleRoutingSolverException(jobId, HttpStatus.NOT_FOUND, "No VehicleRoutePlan found.");
        }
        if (job.exception != null) {
            throw new VehicleRoutingSolverException(jobId, job.exception);
        }
        return job.VehicleRoutePlan;
    }

    @Operation(
            summary = "Terminate solving for a given job ID. Returns the best solution of the VehicleRoutePlan so far, as it might still be running or not even started.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The best solution of the VehicleRoutePlan so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VehicleRoutePlan.class))),
            @ApiResponse(responseCode = "404", description = "No VehicleRoutePlan found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "500", description = "Exception during solving a VehicleRoutePlan.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @DeleteMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VehicleRoutePlan terminateSolving(
            @Parameter(description = "The job ID returned by the POST method.") @PathVariable("jobId") String jobId) {
        // TODO: Replace with .terminateEarlyAndWait(... [, timeout]); see https://github.com/TimefoldAI/timefold-solver/issues/77
        solverManager.terminateEarly(jobId);
        return getVehicleRoutePlan(jobId);
    }

    private record Job(VehicleRoutePlan VehicleRoutePlan, Throwable exception) {

        static Job ofVehicleRoutePlan(VehicleRoutePlan VehicleRoutePlan) {
            return new Job(VehicleRoutePlan, null);
        }

        static Job ofException(Throwable error) {
            return new Job(null, error);
        }
    }
}
