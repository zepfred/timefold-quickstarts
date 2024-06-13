from timefold.solver.score import (ConstraintFactory, Joiners, constraint_provider,
                                   HardSoftScore)
from datetime import datetime, time

from .domain import Availability, Shift, DESIRED, UNDESIRED, UNAVAILABLE


def get_minute_overlap(shift1: Shift, shift2: Shift) -> int:
    return (min(shift1.end, shift2.end) - max(shift1.start, shift2.start)).total_seconds() // 60


def get_shift_duration_in_minutes(shift: Shift) -> int:
    return (shift.end - shift.start).total_seconds() // 60


@constraint_provider
def scheduling_constraints(constraint_factory: ConstraintFactory):
    return [
        required_skill(constraint_factory),
        no_overlapping_shifts(constraint_factory),
        at_least_10_hours_between_two_shifts(constraint_factory),
        one_shift_per_day(constraint_factory),
        unavailable_employee(constraint_factory),
        desired_day_for_employee(constraint_factory),
        undesired_day_for_employee(constraint_factory),
    ]


def required_skill(constraint_factory: ConstraintFactory):
    return (constraint_factory.for_each(Shift)
            .filter(lambda shift: shift.required_skill not in shift.employee.skills)
            .penalize(HardSoftScore.ONE_HARD)
            .as_constraint("Missing required skill")
            )


def no_overlapping_shifts(constraint_factory: ConstraintFactory):
    return (constraint_factory
            .for_each_unique_pair(Shift,
                                  Joiners.equal(lambda shift: shift.employee.name),
                                  Joiners.overlapping(lambda shift: shift.start, lambda shift: shift.end))
            .penalize(HardSoftScore.ONE_HARD, get_minute_overlap)
            .as_constraint("Overlapping shift")
            )


def at_least_10_hours_between_two_shifts(constraint_factory: ConstraintFactory):
    return (constraint_factory
            .for_each(Shift)
            .join(Shift,
                  Joiners.equal(lambda shift: shift.employee.name),
                  Joiners.less_than_or_equal(lambda shift: shift.end, lambda shift: shift.start)
                  )
            .filter(lambda first_shift, second_shift:
                    (second_shift.start - first_shift.end).total_seconds() // (60 * 60) < 10)
            .penalize(HardSoftScore.ONE_HARD,
                      lambda first_shift, second_shift:
                      600 - ((second_shift.start - first_shift.end).total_seconds() // 60))
            .as_constraint("At least 10 hours between 2 shifts")
            )


def one_shift_per_day(constraint_factory: ConstraintFactory):
    return (constraint_factory
            .for_each_unique_pair(Shift,
                                  Joiners.equal(lambda shift: shift.employee.name),
                                  Joiners.equal(lambda shift: shift.start.date()))
            .penalize(HardSoftScore.ONE_HARD)
            .as_constraint("Max one shift per day")
            )


def unavailable_employee(constraint_factory: ConstraintFactory):
    return (constraint_factory.for_each(Shift)
            .join(Availability,
                  Joiners.equal(lambda shift: shift.employee.name,
                                lambda availability: availability.employee.name),
                  Joiners.overlapping(lambda shift: shift.start,
                                      lambda shift: shift.end,
                                      lambda availability: datetime.combine(availability.date, time(0,0)),
                                      lambda availability: datetime.combine(availability.date, time(23, 59)))
                  )
            .filter(lambda shift, availability: availability.availability_type == UNAVAILABLE)
            .penalize(HardSoftScore.ONE_HARD,
                      lambda shift, availability: get_shift_duration_in_minutes(shift))
            .as_constraint("Unavailable employee")
            )


def desired_day_for_employee(constraint_factory: ConstraintFactory):
    return (constraint_factory.for_each(Shift)
            .join(Availability,
                  Joiners.equal(lambda shift: shift.employee.name,
                                lambda availability: availability.employee.name),
                  Joiners.overlapping(lambda shift: shift.start,
                                      lambda shift: shift.end,
                                      lambda availability: datetime.combine(availability.date, time(0,0)),
                                      lambda availability: datetime.combine(availability.date, time(23, 59)))
                  )
            .filter(lambda shift, availability: availability.availability_type == DESIRED)
            .reward(HardSoftScore.ONE_SOFT,
                    lambda shift, availability: get_shift_duration_in_minutes(shift))
            .as_constraint("Desired day for employee")
            )


def undesired_day_for_employee(constraint_factory: ConstraintFactory):
    return (constraint_factory.for_each(Shift)
            .join(Availability,
                  Joiners.equal(lambda shift: shift.employee.name,
                                lambda availability: availability.employee.name),
                  Joiners.overlapping(lambda shift: shift.start,
                                      lambda shift: shift.end,
                                      lambda availability: datetime.combine(availability.date, time(0,0)),
                                      lambda availability: datetime.combine(availability.date, time(23, 59))),
                  )
            .filter(lambda shift, availability: availability.availability_type == UNDESIRED)
            .penalize(HardSoftScore.ONE_SOFT,
                      lambda shift, availability: get_shift_duration_in_minutes(shift))
            .as_constraint("Undesired day for employee")
            )


__all__ = ['scheduling_constraints',
           'required_skill',
           'no_overlapping_shifts',
           'at_least_10_hours_between_two_shifts',
           'one_shift_per_day',
           'unavailable_employee',
           'desired_day_for_employee',
           'undesired_day_for_employee',
           ]
