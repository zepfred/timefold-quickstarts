package org.acme.vehiclerouting.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.IndexShadowVariable;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import org.acme.vehiclerouting.solver.listener.ArrivalTimeUpdatingVariableListener;
import org.acme.vehiclerouting.solver.listener.WeightAtVisitVariableListener;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Visit.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Visit {

    @PlanningId
    private String id;
    private String name;
    private Location location;
    private LocalDateTime minStartTime;
    private LocalDateTime maxEndTime;
    private Duration serviceDuration;
    @JsonIdentityReference(alwaysAsId = true)
    private Shipment shipment;

    @JsonIdentityReference(alwaysAsId = true)
    @InverseRelationShadowVariable(sourceVariableName = "visits")
    private Vehicle vehicle;
    @JsonIdentityReference(alwaysAsId = true)
    @IndexShadowVariable(sourceVariableName = "visits")
    private Integer vehicleIndex;
    @JsonIdentityReference(alwaysAsId = true)
    @PreviousElementShadowVariable(sourceVariableName = "visits")
    private Visit previousVisit;
    @JsonIdentityReference(alwaysAsId = true)
    @NextElementShadowVariable(sourceVariableName = "visits")
    private Visit nextVisit;
    @ShadowVariable(variableListenerClass = ArrivalTimeUpdatingVariableListener.class, sourceVariableName = "vehicle")
    @ShadowVariable(variableListenerClass = ArrivalTimeUpdatingVariableListener.class, sourceVariableName = "previousVisit")
    private LocalDateTime arrivalTime;
    @ShadowVariable(variableListenerClass = WeightAtVisitVariableListener.class, sourceVariableName = "previousVisit")
    @ShadowVariable(variableListenerClass = WeightAtVisitVariableListener.class, sourceVariableName = "nextVisit")
    private Integer weightAtVisit;

    public Visit() {
    }

    public Visit(String id, String name, Location location, LocalDateTime minStartTime, LocalDateTime maxEndTime,
            Duration serviceDuration, Shipment shipment) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.minStartTime = minStartTime;
        this.maxEndTime = maxEndTime;
        this.serviceDuration = serviceDuration;
        this.shipment = shipment;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDateTime getMinStartTime() {
        return minStartTime;
    }

    public LocalDateTime getMaxEndTime() {
        return maxEndTime;
    }

    public Duration getServiceDuration() {
        return serviceDuration;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Visit getPreviousVisit() {
        return previousVisit;
    }

    public void setPreviousVisit(Visit previousVisit) {
        this.previousVisit = previousVisit;
    }

    public Visit getNextVisit() {
        return nextVisit;
    }

    public void setNextVisit(Visit nextVisit) {
        this.nextVisit = nextVisit;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public Integer getVehicleIndex() {
        return vehicleIndex;
    }

    public void setVehicleIndex(Integer vehicleIndex) {
        this.vehicleIndex = vehicleIndex;
    }

    public Integer getWeightAtVisit() {
        return weightAtVisit;
    }

    public void setWeightAtVisit(Integer weightAtVisit) {
        this.weightAtVisit = weightAtVisit;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public LocalDateTime getDepartureTime() {
        if (arrivalTime == null) {
            return null;
        }
        return getStartServiceTime().plus(serviceDuration);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public LocalDateTime getStartServiceTime() {
        if (arrivalTime == null) {
            return null;
        }
        return arrivalTime.isBefore(minStartTime) ? minStartTime : arrivalTime;
    }

    @JsonIgnore
    public boolean isServiceFinishedAfterMaxEndTime() {
        return arrivalTime != null
                && arrivalTime.plus(serviceDuration).isAfter(maxEndTime);
    }

    @JsonIgnore
    public long getServiceFinishedDelayInMinutes() {
        if (arrivalTime == null) {
            return 0;
        }
        return roundDurationToNextOrEqualMinutes(Duration.between(maxEndTime, arrivalTime.plus(serviceDuration)));
    }

    private static long roundDurationToNextOrEqualMinutes(Duration duration) {
        var remainder = duration.minus(duration.truncatedTo(ChronoUnit.MINUTES));
        var minutes = duration.toMinutes();
        if (remainder.equals(Duration.ZERO)) {
            return minutes;
        }
        return minutes + 1;
    }

    @JsonIgnore
    public long getDrivingTimeSecondsFromPreviousStandstill() {
        if (vehicle == null) {
            throw new IllegalStateException(
                    "This method must not be called when the shadow variables are not initialized yet.");
        }
        if (previousVisit == null) {
            return vehicle.getHomeLocation().getDrivingTimeTo(location);
        }
        return previousVisit.getLocation().getDrivingTimeTo(location);
    }

    // Required by the web UI even before the solution has been initialized.
    @JsonProperty(value = "drivingTimeSecondsFromPreviousStandstill", access = JsonProperty.Access.READ_ONLY)
    public Long getDrivingTimeSecondsFromPreviousStandstillOrNull() {
        if (vehicle == null) {
            return null;
        }
        return getDrivingTimeSecondsFromPreviousStandstill();
    }

    @JsonIgnore
    public boolean isPickup() {
        return this.shipment.getPickupVisit().equals(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Visit visit))
            return false;
        return Objects.equals(getId(), visit.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
