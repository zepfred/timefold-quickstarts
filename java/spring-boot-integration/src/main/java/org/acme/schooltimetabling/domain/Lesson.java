package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import java.util.List;

@PlanningEntity
public class Lesson {

    @PlanningId
    private String id;

    private String subject;
    private String teacher;
    private String studentGroup;

    @JsonIdentityReference
    @PlanningVariable(valueRangeProviderRefs = "timeslots")
    private Timeslot timeslot;

    @JsonIdentityReference
    @PlanningVariable(valueRangeProviderRefs = "rooms")
    private Room room;

    public Lesson() {
    }

    public Lesson(String id, String subject, String teacher, String studentGroup) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.studentGroup = studentGroup;
    }

    public Lesson(String id, String subject, String teacher, String studentGroup, Timeslot timeslot, Room room) {
        this(id, subject, teacher, studentGroup);
        this.timeslot = timeslot;
        this.room = room;
    }

    @Override
    public String toString() {
        return subject + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getStudentGroup() {
        return studentGroup;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @ValueRangeProvider(id = "timeslots")
    public List<Timeslot> getTimeslots(Timetable timetable) {
        return timetable.getTimeslots();
    }

    @ValueRangeProvider(id = "rooms")
    public List<Room> getRooms(Timetable timetable) {
        return timetable.getRooms();
    }
}
