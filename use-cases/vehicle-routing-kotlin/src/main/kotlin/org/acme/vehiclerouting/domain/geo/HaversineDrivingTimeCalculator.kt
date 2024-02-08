package org.acme.vehiclerouting.domain.geo

import kotlin.math.asin
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin

import org.acme.vehiclerouting.domain.Location

class HaversineDrivingTimeCalculator private constructor() : DrivingTimeCalculator {
    override fun calculateDrivingTime(from: Location, to: Location): Long {
        if (from == to) {
            return 0L
        }

        val fromCartesian = locationToCartesian(from)
        val toCartesian = locationToCartesian(to)
        return metersToDrivingSeconds(calculateDistance(fromCartesian, toCartesian))
    }

    private fun calculateDistance(from: CartesianCoordinate, to: CartesianCoordinate): Long {
        if (from == to) {
            return 0L
        }

        val dX = from.x - to.x
        val dY = from.y - to.y
        val dZ = from.z - to.z
        val r: Double = sqrt((dX * dX) + (dY * dY) + (dZ * dZ))
        return Math.round(TWICE_EARTH_RADIUS_IN_M * asin(r))
    }

    private fun locationToCartesian(location: Location): CartesianCoordinate {
        val latitudeInRads = Math.toRadians(location.latitude)
        val longitudeInRads = Math.toRadians(location.longitude)
        // Cartesian coordinates, normalized for a sphere of diameter 1.0
        val cartesianX: Double = 0.5 * cos(latitudeInRads) * sin(longitudeInRads)
        val cartesianY: Double = 0.5 * cos(latitudeInRads) * cos(longitudeInRads)
        val cartesianZ: Double = 0.5 * sin(latitudeInRads)
        return CartesianCoordinate(cartesianX, cartesianY, cartesianZ)
    }

    private data class CartesianCoordinate(val x: Double, val y: Double, val z: Double)
    companion object {
        @JvmStatic
        @get:Synchronized
        val INSTANCE: HaversineDrivingTimeCalculator = HaversineDrivingTimeCalculator()

        const val AVERAGE_SPEED_KMPH: Int = 50

        private const val EARTH_RADIUS_IN_M = 6371000
        private const val TWICE_EARTH_RADIUS_IN_M = 2 * EARTH_RADIUS_IN_M

        fun metersToDrivingSeconds(meters: Long): Long {
            return Math.round(meters.toDouble() / AVERAGE_SPEED_KMPH * 3.6)
        }
    }
}