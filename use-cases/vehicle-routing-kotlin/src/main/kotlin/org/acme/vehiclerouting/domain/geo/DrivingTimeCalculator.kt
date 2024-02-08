package org.acme.vehiclerouting.domain.geo

import org.acme.vehiclerouting.domain.Location
import java.util.function.Function
import java.util.stream.Collectors

interface DrivingTimeCalculator {
    /**
     * Calculate the driving time between `from` and `to` in seconds.
     *
     * @param from starting location
     * @param to target location
     * @return driving time in seconds
     */
    fun calculateDrivingTime(from: Location, to: Location): Long

    /**
     * Bulk calculation of driving time.
     * Typically, much more scalable than [.calculateDrivingTime] iteratively.
     *
     * @param fromLocations never null
     * @param toLocations never null
     * @return never null
     */
    fun calculateBulkDrivingTime(
        fromLocations: Collection<Location>,
        toLocations: Collection<Location>
    ): Map<Location, Map<Location, Long>> {
        return fromLocations.stream().collect(
            Collectors.toMap(
                Function.identity()
            ) { from: Location ->
                toLocations.stream()
                    .collect(
                        Collectors.toMap(
                            Function.identity(),
                            { to: Location ->
                                calculateDrivingTime(
                                    from,
                                    to
                                )
                            })
                    )
            }
        )
    }

    /**
     * Calculate driving time matrix for the given list of locations and assign driving time maps accordingly.
     *
     * @param locations locations list
     */
    fun initDrivingTimeMaps(locations: Collection<Location>) {
        val drivingTimeMatrix = calculateBulkDrivingTime(locations, locations)
        locations.forEach { location: Location ->
            location.drivingTimeSeconds = drivingTimeMatrix[location]
        }
    }
}