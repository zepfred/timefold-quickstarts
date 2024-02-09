package org.acme.vehiclerouting.solver;

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Arrays

import jakarta.inject.Inject

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier
import ai.timefold.solver.core.api.score.stream.ConstraintFactory

import org.acme.vehiclerouting.domain.Location
import org.acme.vehiclerouting.domain.Vehicle
import org.acme.vehiclerouting.domain.VehicleRoutePlan
import org.acme.vehiclerouting.domain.Visit
import org.acme.vehiclerouting.domain.geo.HaversineDrivingTimeCalculator
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import io.quarkus.test.junit.QuarkusTest

@QuarkusTest
internal class VehicleRoutingConstraintProviderTest {

    @Inject
    lateinit var constraintVerifier: ConstraintVerifier<VehicleRoutingConstraintProvider, VehicleRoutePlan>

    @Test
    fun vehicleCapacityPenalized() {
        val tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0))
        val tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0))
        val tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0))
        val vehicleA = Vehicle("1", 100, LOCATION_1, tomorrow_07_00)
        val visit1 = Visit("2", "John", LOCATION_2, 80, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L))
        vehicleA.visits!!.add(visit1)
        val visit2 = Visit("3", "Paul", LOCATION_3, 40, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L))
        vehicleA.visits!!.add(visit2)

        constraintVerifier!!.verifyThat { obj: VehicleRoutingConstraintProvider, factory: ConstraintFactory? ->
            obj.vehicleCapacity(
                factory!!
            )
        }
            .given(vehicleA, visit1, visit2)
            .penalizesBy(20)
    }

    companion object {
        /*
     * LOCATION_1 to LOCATION_2 is approx. 11713 m ~843 seconds of driving time
     * LOCATION_2 to LOCATION_3 is approx. 8880 m ~639 seconds of driving time
     * LOCATION_1 to LOCATION_3 is approx. 13075 m ~941 seconds of driving time
     */
        private val LOCATION_1 = Location(49.288087, 16.562172)
        private val LOCATION_2 = Location(49.190922, 16.624466)
        private val LOCATION_3 = Location(49.1767533245638, 16.50422914190477)

        private val TOMORROW: LocalDate = LocalDate.now().plusDays(1)
        @JvmStatic
        @BeforeAll
        fun initDrivingTimeMaps() {
            HaversineDrivingTimeCalculator.INSTANCE.initDrivingTimeMaps(
                Arrays.asList(
                    LOCATION_1, LOCATION_2, LOCATION_3
                )
            )
        }
    }
}