from timefold.solver import SolverStatus
from timefold.solver.domain import *
from timefold.solver.score import HardSoftScore
from datetime import datetime, date, timedelta
from typing import Annotated, Any
from pydantic import BaseModel, ConfigDict, Field, PlainSerializer, BeforeValidator, ValidationInfo
from pydantic.alias_generators import to_camel

ScoreSerializer = PlainSerializer(lambda score: str(score) if score is not None else None,
                                  return_type=str | None)


def validate_score(v: Any, info: ValidationInfo) -> Any:
    if isinstance(v, HardSoftScore) or v is None:
        return v
    if isinstance(v, str):
        hard_part, soft_part = v.split('/')
        hard = int(hard_part.rstrip('hard'))
        soft = int(soft_part.rstrip('soft'))
        return HardSoftScore.of(hard, soft)
    raise ValueError('"score" should be a string')


ScoreValidator = BeforeValidator(validate_score)


DESIRED = 'DESIRED'
UNDESIRED = 'UNDESIRED'
UNAVAILABLE = 'UNAVAILABLE'


class BaseSchema(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )


class Employee(BaseSchema):
    name: Annotated[str, PlanningId]
    skills: set[str]


@planning_entity
class Shift(BaseSchema):
    id: Annotated[str, PlanningId]

    start: datetime
    end: datetime

    location: str
    required_skill: str

    employee: Annotated[Employee | None,
                        PlanningVariable,
                        Field(default=None)]


class Availability(BaseSchema):
    id: Annotated[str, PlanningId]
    employee: Employee
    date: date
    availability_type: str


@planning_solution
class EmployeeSchedule(BaseSchema):
    availabilities: Annotated[list[Availability], ProblemFactCollectionProperty]
    employees: Annotated[list[Employee], ProblemFactCollectionProperty, ValueRangeProvider]
    shifts: Annotated[list[Shift], PlanningEntityCollectionProperty]
    score: Annotated[HardSoftScore | None,
                     PlanningScore,
                     ScoreSerializer,
                     ScoreValidator,
                     Field(default=None)]
    solver_status: Annotated[SolverStatus | None, Field(default=None)]
