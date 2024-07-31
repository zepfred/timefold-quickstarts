package org.acme.foodpackaging.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import ai.timefold.solver.core.impl.domain.variable.listener.support.AbstractEventTransactionSupport;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Line extends AbstractEventTransactionSupport {

    @PlanningId
    private String id;
    private String name;
    private String operator;
    private LocalDateTime startDateTime;

    @JsonIgnore
    @PlanningListVariable
    private List<Job> jobs;

    // No-arg constructor required for Timefold
    public Line() {
    }

    public Line(String id, String name, String operator, LocalDateTime startDateTime) {
        this.id = id;
        this.name = name;
        this.operator = operator;
        this.startDateTime = startDateTime;
        jobs = new ArrayList<>();
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOperator() {
        return operator;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    @Override
    public void _internal_Timefold_Event_Support_executeTargetMethod(String targetMethod) {
        // Do nothing
    }

    @Override
    public Object _internal_Timefold_Event_Support_getFieldValue(String fieldName) {
        if (fieldName.equals("jobs")) {
            return jobs;
        }
        throw new IllegalStateException("The field %s cannot be found.".formatted(fieldName));
    }
}
