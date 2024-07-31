package org.acme.taskassigning.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import ai.timefold.solver.core.impl.domain.variable.listener.support.AbstractEventTransactionSupport;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@PlanningEntity
@JsonIdentityInfo(scope = Employee.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Employee extends AbstractEventTransactionSupport {

    @PlanningId
    private String id;
    private String fullName;

    private List<String> skills;
    private Map<Customer, Affinity> customerToAffinity;

    @PlanningListVariable(allowsUnassignedValues = true)
    private List<Task> tasks;

    public Employee() {
    }

    public Employee(String id) {
        this.id = id;
    }

    public Employee(String id, String fullName) {
        this(id);
        this.fullName = fullName;
        skills = new ArrayList<>();
        customerToAffinity = new LinkedHashMap<>();
        tasks = new ArrayList<>();
    }

    public Employee(String id, String fullName, List<String> skills, Map<Customer, Affinity> customerToAffinity) {
        this(id, fullName);
        this.skills = skills;
        this.customerToAffinity = customerToAffinity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public Map<Customer, Affinity> getCustomerToAffinity() {
        return customerToAffinity;
    }

    public void setCustomerToAffinity(Map<Customer, Affinity> customerToAffinity) {
        this.customerToAffinity = customerToAffinity;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @param customer never null
     * @return never null
     */
    @JsonIgnore
    public Affinity getAffinity(Customer customer) {
        Affinity affinity = customerToAffinity.get(customer);
        if (affinity == null) {
            affinity = Affinity.NONE;
        }
        return affinity;
    }

    @JsonIgnore
    public Integer getEndTime() {
        return tasks.isEmpty() ? 0 : tasks.get(tasks.size() - 1).getEndTime();
    }

    @Override
    public void _internal_Timefold_Event_Support_executeTargetMethod(String targetMethod) {
        // Do nothing
    }

    @Override
    public Object _internal_Timefold_Event_Support_getFieldValue(String fieldName) {
        if (fieldName.equals("tasks")) {
            return tasks;
        }
        throw new IllegalStateException("The field %s cannot be found.".formatted(fieldName));
    }

    @Override
    public String toString() {
        return fullName;
    }
}
