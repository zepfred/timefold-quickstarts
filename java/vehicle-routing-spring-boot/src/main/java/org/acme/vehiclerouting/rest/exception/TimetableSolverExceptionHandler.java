package org.acme.vehiclerouting.rest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TimetableSolverExceptionHandler {

    @ExceptionHandler({VehicleRoutingSolverException.class})
    public ResponseEntity<ErrorInfo> handleTimetableSolverException(VehicleRoutingSolverException exception) {
        return new ResponseEntity<>(new ErrorInfo(exception.getJobId(), exception.getMessage()), exception.getStatus());
    }
}
