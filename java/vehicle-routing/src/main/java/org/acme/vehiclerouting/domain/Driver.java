package org.acme.vehiclerouting.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(scope = Driver.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@PlanningEntity
public class Driver {

    @PlanningId
    private String id;
    private String licenseType;

    @JsonIdentityReference(alwaysAsId = true)
    @InverseRelationShadowVariable(sourceVariableName = "driver")
    private List<Vehicle> vehicles = new ArrayList<>();

    public Driver() {
        // Required for cloning
    }

    public Driver(String id, String licenseType) {
        this.id = id;
        this.licenseType = licenseType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getVehicleCount() {
        return vehicles.size();
    }

    @Override
    public String toString() {
        return id;
    }

}
