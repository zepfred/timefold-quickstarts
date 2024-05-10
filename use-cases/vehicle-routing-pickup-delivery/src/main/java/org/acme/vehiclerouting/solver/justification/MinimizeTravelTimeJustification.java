package org.acme.vehiclerouting.solver.justification;

import java.time.Duration;

import ai.timefold.solver.core.api.score.stream.ConstraintJustification;

public record MinimizeTravelTimeJustification(String vehicleName, long totalDrivingTimeSeconds,
        String description) implements ConstraintJustification {

    public MinimizeTravelTimeJustification(String label, String id, long totalDrivingTimeSeconds) {
        this(id, totalDrivingTimeSeconds, "%s %s total travel time is %s."
                .formatted(label, id, formatDrivingTime(totalDrivingTimeSeconds)));
    }

    private static String formatDrivingTime(long drivingTimeSeconds) {
        Duration drivingTime = Duration.ofSeconds(drivingTimeSeconds);
        return "%s hours %s minutes".formatted(drivingTime.toHours(),
                drivingTime.toSecondsPart() >= 30 ? drivingTime.toMinutesPart() + 1 : drivingTime.toMinutesPart());
    }
}
