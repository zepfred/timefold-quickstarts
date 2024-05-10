package org.acme.vehiclerouting.solver;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Shipment;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.geo.HaversineDrivingTimeCalculator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VehicleRoutingConstraintProviderTest {

    /*
     * LOCATION_1 to LOCATION_2 is approx. 11713 m ~843 seconds of driving time
     * LOCATION_2 to LOCATION_3 is approx. 8880 m ~639 seconds of driving time
     * LOCATION_1 to LOCATION_3 is approx. 13075 m ~941 seconds of driving time
     */
    private static final Location LOCATION_1 = new Location(49.288087, 16.562172);
    private static final Location LOCATION_2 = new Location(49.190922, 16.624466);
    private static final Location LOCATION_3 = new Location(49.1767533245638, 16.50422914190477);

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    @Inject
    ConstraintVerifier<VehicleRoutingConstraintProvider, VehicleRoutePlan> constraintVerifier;

    @BeforeAll
    static void initDrivingTimeMaps() {
        HaversineDrivingTimeCalculator.getInstance().initDrivingTimeMaps(Arrays.asList(LOCATION_1, LOCATION_2, LOCATION_3));
    }

    @Test
    void sameVehicleShipment() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);
        Vehicle vehicleB = new Vehicle("2", 100, LOCATION_1, tomorrow_07_00);
        Shipment shipment1 = new Shipment("1", 100);
        Visit visit1 = new Visit("1", "John", LOCATION_2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment1);
        visit1.setVehicle(vehicleA);
        shipment1.setPickupVisit(visit1);
        Visit visit2 = new Visit("2", "John", LOCATION_3, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment1);
        visit2.setVehicle(vehicleA);
        shipment1.setDeliveryVisit(visit2);
        Shipment shipment2 = new Shipment("2", 10);
        Visit visit3 = new Visit("3", "John", LOCATION_2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment2);
        visit3.setVehicle(vehicleA);
        shipment2.setPickupVisit(visit3);
        Visit visit4 = new Visit("4", "John", LOCATION_3, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment2);
        visit4.setVehicle(vehicleB);
        shipment2.setDeliveryVisit(visit4);
        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::sameVehicleShipment)
                .given(visit1, visit2, visit3, visit4)
                .penalizesBy(10);
    }

    @Test
    void pickupDeliveryOrder() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);
        Vehicle vehicleB = new Vehicle("2", 100, LOCATION_1, tomorrow_07_00);
        Shipment shipment1 = new Shipment("1", 100);
        Visit visit1 = new Visit("1", "John", LOCATION_2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment1);
        visit1.setVehicleIndex(1);
        visit1.setVehicle(vehicleA);
        shipment1.setPickupVisit(visit1);
        Visit visit2 = new Visit("2", "John", LOCATION_3, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment1);
        visit2.setVehicle(vehicleA);
        visit2.setVehicleIndex(2);
        shipment1.setDeliveryVisit(visit2);
        Shipment shipment2 = new Shipment("2", 10);
        Visit visit3 = new Visit("3", "John", LOCATION_2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment2);
        visit3.setVehicle(vehicleB);
        visit3.setVehicleIndex(5);
        shipment2.setPickupVisit(visit3);
        Visit visit4 = new Visit("4", "John", LOCATION_3, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment2);
        visit4.setVehicle(vehicleB);
        visit4.setVehicleIndex(2);
        shipment2.setDeliveryVisit(visit4);
        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::pickupDeliveryOrder)
                .given(visit1, visit2, visit3, visit4)
                .penalizesBy(3);
    }

    @Test
    void vehicleCapacityUnpenalized() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);
        Shipment shipment1 = new Shipment("1", 100);
        Visit visit1 = new Visit("1", "John", LOCATION_2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment1);
        Visit visit2 = new Visit("2", "John", LOCATION_3, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment1);
        visit1.setWeightAtVisit(0);
        visit1.setVehicle(vehicleA);
        visit2.setWeightAtVisit(100);
        visit2.setVehicle(vehicleA);
        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacityAtVisit)
                .given(visit1, visit2)
                .penalizesBy(0);
    }

    @Test
    void vehicleCapacityPenalized() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);
        Shipment shipment1 = new Shipment("1", 100);
        Visit visit1 = new Visit("1", "John", LOCATION_2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment1);
        Visit visit2 = new Visit("2", "John", LOCATION_3, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment1);
        visit1.setWeightAtVisit(0);
        visit1.setVehicle(vehicleA);
        visit2.setWeightAtVisit(105);
        visit2.setVehicle(vehicleA);
        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacityAtVisit)
                .given(visit1, visit2)
                .penalizesBy(5);
    }

    @Test
    void serviceFinishedAfterMaxEndTime() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_08_00_01 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0, 1));
        LocalDateTime tomorrow_08_40 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 40));
        LocalDateTime tomorrow_09_00 = LocalDateTime.of(TOMORROW, LocalTime.of(9, 0));
        LocalDateTime tomorrow_10_30 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 30));
        LocalDateTime tomorrow_18_00 = LocalDateTime.of(TOMORROW, LocalTime.of(18, 0));

        Visit visit1 = new Visit("2", "John", LOCATION_2, tomorrow_08_00, tomorrow_18_00, Duration.ofHours(1L), null);
        visit1.setArrivalTime(tomorrow_08_40);
        Visit visit2 = new Visit("3", "Paul", LOCATION_3, tomorrow_08_00, tomorrow_09_00, Duration.ofHours(1L), null);
        visit2.setArrivalTime(tomorrow_10_30);
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);

        connect(vehicleA, visit1, visit2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::serviceFinishedAfterMaxEndTime)
                .given(vehicleA, visit1, visit2)
                .penalizesBy(90 + visit2.getServiceDuration().toMinutes());

        visit2.setArrivalTime(tomorrow_08_00_01);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::serviceFinishedAfterMaxEndTime)
                .given(vehicleA, visit1, visit2)
                .penalizesBy(1);
    }
    
    @Test
    void minimizeVehicleTravelTime() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);
        Visit visit1 = new Visit("2", "John", LOCATION_2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), null);
        vehicleA.getVisits().add(visit1);
        Visit visit2 = new Visit("3", "Paul", LOCATION_3, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), null);
        vehicleA.getVisits().add(visit2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::minimizeVehicleTravelTime)
                .given(vehicleA, visit1, visit2)
                .penalizesBy(2423L); // The sum of the approximate driving time between all three locations.
    }

    @Test
    void minimizeShipmentTravelTime() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicle = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);
        Shipment shipment = new Shipment("1", 50);
        Shipment shipment2 = new Shipment("2", 50);
        Visit visit1 = new Visit("1", "John", LOCATION_2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment);
        visit1.setVehicle(vehicle);
        shipment.setPickupVisit(visit1);
        Visit visit2 = new Visit("2", "Paul", LOCATION_3, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment2);
        visit2.setVehicle(vehicle);
        shipment2.setPickupVisit(visit2);
        visit1.setNextVisit(visit2);
        Visit visit3 = new Visit("3", "Ann", LOCATION_2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L), shipment);
        visit3.setVehicle(vehicle);
        shipment.setDeliveryVisit(visit3);
        visit2.setNextVisit(visit3);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::minimizeShipmentTravelTime)
                .given(visit1, visit2, visit3)
                .penalizesBy(1278L);
    }

    static void connect(Vehicle vehicle, Visit... visits) {
        vehicle.setVisits(Arrays.asList(visits));
        for (int i = 0; i < visits.length; i++) {
            Visit visit = visits[i];
            visit.setVehicle(vehicle);
            if (i > 0) {
                visit.setPreviousVisit(visits[i - 1]);
            }
            if (i < visits.length - 1) {
                visit.setNextVisit(visits[i + 1]);
            }
        }
    }
}
