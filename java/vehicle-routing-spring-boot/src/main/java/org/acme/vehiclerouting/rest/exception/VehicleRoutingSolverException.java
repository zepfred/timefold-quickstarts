package org.acme.vehiclerouting.rest.exception;

import org.springframework.http.HttpStatus;

public class VehicleRoutingSolverException extends RuntimeException {

    private final String jobId;

    private final HttpStatus status;

    public VehicleRoutingSolverException(String jobId, HttpStatus status, String message) {
        super(message);
        this.jobId = jobId;
        this.status = status;
    }

    public VehicleRoutingSolverException(String jobId, Throwable cause) {
        super(cause.getMessage(), cause);
        this.jobId = jobId;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public String getJobId() {
        return jobId;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
