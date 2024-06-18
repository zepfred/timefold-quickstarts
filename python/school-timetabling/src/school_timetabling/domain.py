from timefold.solver import SolverStatus
from timefold.solver.domain import (planning_entity, planning_solution, PlanningId, PlanningVariable,
                                    PlanningEntityCollectionProperty,
                                    ProblemFactCollectionProperty, ValueRangeProvider,
                                    PlanningScore)
from timefold.solver.score import HardSoftScore
from datetime import time
from pydantic import BaseModel, ConfigDict, Field, PlainSerializer, BeforeValidator, PlainValidator, ValidationInfo
from pydantic.alias_generators import to_camel
from typing import Annotated, Any


def make_list_item_validator(key: str):
    def validator(v: Any, info: ValidationInfo) -> Any:
        if v is None:
            return None

        if not isinstance(v, str) or not info.context:
            return v

        return info.context.get(key)[v]

    return BeforeValidator(validator)


RoomDeserializer = make_list_item_validator('rooms')
TimeslotDeserializer = make_list_item_validator('timeslots')

IdSerializer = PlainSerializer(lambda item: item.id if item is not None else None,
                               return_type=str | None)
ScoreSerializer = PlainSerializer(lambda score: str(score) if score is not None else None,
                                  return_type=str | None)


def validate_score(v: Any, info: ValidationInfo) -> Any:
    if isinstance(v, HardSoftScore) or v is None:
        return v
    if isinstance(v, str):
        return HardSoftScore.parse(v)
    raise ValueError('"score" should be a string')


ScoreValidator = BeforeValidator(validate_score)


class BaseSchema(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )


class Timeslot(BaseSchema):
    id: Annotated[str, PlanningId]
    day_of_week: str
    start_time: time
    end_time: time


class Room(BaseSchema):
    id: Annotated[str, PlanningId]
    name: str


@planning_entity
class Lesson(BaseSchema):
    id: Annotated[str, PlanningId]
    subject: str
    teacher: str
    student_group: str
    timeslot: Annotated[Timeslot | None,
                        PlanningVariable,
                        IdSerializer,
                        TimeslotDeserializer,
                        Field(default=None)]
    room: Annotated[Room | None,
                    PlanningVariable,
                    IdSerializer,
                    RoomDeserializer,
                    Field(default=None)]


@planning_solution
class Timetable(BaseSchema):
    id: str
    timeslots: Annotated[list[Timeslot],
                         ProblemFactCollectionProperty,
                         ValueRangeProvider]
    rooms: Annotated[list[Room],
                     ProblemFactCollectionProperty,
                     ValueRangeProvider]
    lessons: Annotated[list[Lesson],
                       PlanningEntityCollectionProperty]
    score: Annotated[HardSoftScore | None,
                     PlanningScore,
                     ScoreSerializer,
                     ScoreValidator,
                     Field(default=None)]
    solver_status: Annotated[SolverStatus, Field(default=SolverStatus.NOT_SOLVING)]


class MatchAnalysisDTO(BaseSchema):
    name: str
    score: Annotated[HardSoftScore, ScoreSerializer]
    justification: object


class ConstraintAnalysisDTO(BaseSchema):
    name: str
    weight: Annotated[HardSoftScore, ScoreSerializer]
    matches: list[MatchAnalysisDTO]
    score: Annotated[HardSoftScore, ScoreSerializer]
