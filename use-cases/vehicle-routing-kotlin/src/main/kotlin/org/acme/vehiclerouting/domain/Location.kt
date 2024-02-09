package org.acme.vehiclerouting.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
class Location @JsonCreator constructor(
    @param:JsonProperty(
        "latitude"
    ) val latitude: Double, @param:JsonProperty("longitude") val longitude: Double
) {
    /**
     * Set the driving time map (in seconds).
     *
     * @param drivingTimeSeconds a map containing driving time from here to other locations
     */
    @JsonIgnore
    var drivingTimeSeconds: Map<Location, Long>? = null

    /**
     * Driving time to the given location in seconds.
     *
     * @param location other location
     * @return driving time in seconds
     */
    fun getDrivingTimeTo(location: Location): Long {
        if (drivingTimeSeconds == null) {
            return 0
        }
        return drivingTimeSeconds!![location]!!
    }

    override fun toString(): String {
        return "$latitude,$longitude"
    }
}