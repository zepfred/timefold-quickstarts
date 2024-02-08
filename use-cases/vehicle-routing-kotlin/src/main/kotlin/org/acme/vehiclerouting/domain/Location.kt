package org.acme.vehiclerouting.domain

class Location(val latitude: Double, val longitude: Double) {
    var drivingTimeSeconds: Map<Location, Long>? = null

    fun getDrivingTimeTo(location: Location): Long {
        return drivingTimeSeconds!![location]!!
    }
}