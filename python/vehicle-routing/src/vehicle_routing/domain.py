from timefold.solver import SolverStatus
from timefold.solver.score import HardSoftScore, ScoreDirector
from timefold.solver.domain import *

from dataclasses import dataclass
from typing import Annotated, Optional, Any
from pydantic import BaseModel, ConfigDict, PlainSerializer, BeforeValidator, Field, ValidationInfo, computed_field
from pydantic.alias_generators import to_camel


class BaseSchema(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True,
    )


def make_id_item_validator(key: str):
    def validator(v: Any, info: ValidationInfo) -> Any:
        if v is None:
            return None

        if not isinstance(v, str) or not info.context:
            return v

        return info.context.get(key)[v]

    return BeforeValidator(validator)


def make_id_list_item_validator(key: str):
    def validator(v: Any, info: ValidationInfo) -> Any:
        if v is None:
            return None

        if isinstance(v, (list, tuple)):
            out = []
            for item in v:
                if not isinstance(v, str) or not info.context:
                    return v
                out.append(info.context.get(key)[item])
            return out

        return v

    return BeforeValidator(validator)


LocationSerializer = PlainSerializer(lambda location: [
    location.latitude,
    location.longitude,
], return_type=list[float])
ScoreSerializer = PlainSerializer(lambda score: str(score), return_type=str)
IdSerializer = PlainSerializer(lambda item: item.id if item is not None else None, return_type=str | None)
IdListSerializer = PlainSerializer(lambda items: [item.id for item in items], return_type=list)

LocationValidator = BeforeValidator(lambda location: location if isinstance(location, Location)
                                    else Location(latitude=location[0], longitude=location[1]))
VisitListValidator = make_id_list_item_validator('visits')
VisitValidator = make_id_item_validator('visits')
VehicleValidator = make_id_item_validator('vehicles')


def validate_score(v: Any, info: ValidationInfo) -> Any:
    if isinstance(v, HardSoftScore) or v is None:
        return v
    if isinstance(v, str):
        return HardSoftScore.parse(v)
    raise ValueError('"score" should be a string')


ScoreValidator = BeforeValidator(validate_score)


class Location(BaseSchema):
    latitude: float
    longitude: float

    def driving_time_to(self, other: 'Location') -> int:
        return round((
             (self.latitude - other.latitude) ** 2 +
             (self.longitude - other.longitude) ** 2
         ) ** 0.5 * 1000)

    def __str__(self):
        return f'[{self.latitude}, {self.longitude}]'

    def __repr__(self):
        return f'Location({self.latitude}, {self.longitude})'


@planning_entity
class Visit(BaseSchema):
    id: Annotated[str, PlanningId]
    name: str
    location: Annotated[Location, LocationSerializer, LocationValidator]
    demand: int
    vehicle: Annotated[Optional['Vehicle'],
                       InverseRelationShadowVariable(source_variable_name='visits'),
                       IdSerializer,
                       VehicleValidator,
                       Field(default=None)]
    previous_visit: Annotated[Optional['Visit'],
                              PreviousElementShadowVariable(source_variable_name='visits'),
                              IdSerializer,
                              VisitValidator,
                              Field(default=None)]
    next_visit: Annotated[Optional['Visit'],
                          NextElementShadowVariable(source_variable_name='visits'),
                          IdSerializer,
                          VisitValidator,
                          Field(default=None)]

    def driving_time_seconds_from_previous_standstill(self) -> int:
        if self.vehicle is None:
            raise ValueError("This method must not be called when the shadow variables are not initialized yet.")

        if self.previous_visit is None:
            return self.vehicle.home_location.driving_time_to(self.location)
        else:
            return self.previous_visit.location.driving_time_to(self.location)

    def driving_time_seconds_from_previous_standstill_or_none(self) -> Optional[int]:
        if self.vehicle is None:
            return None
        return self.driving_time_seconds_from_previous_standstill()

    def __str__(self):
        return self.id

    def __repr__(self):
        return f'Visit({self.id})'


@planning_entity
class Vehicle(BaseSchema):
    id: Annotated[str, PlanningId]
    capacity: int
    home_location: Annotated[Location, LocationSerializer, LocationValidator]
    visits: Annotated[list[Visit],
                      PlanningListVariable,
                      IdListSerializer,
                      VisitListValidator,
                      Field(default_factory=list)]

    @computed_field
    @property
    def total_demand(self) -> int:
        return self.calculate_total_demand()

    @computed_field
    @property
    def total_driving_time_seconds(self) -> int:
        return self.calculate_total_driving_time_seconds()

    def calculate_total_demand(self) -> int:
        total_demand = 0
        for visit in self.visits:
            total_demand += visit.demand
        return total_demand

    def calculate_total_driving_time_seconds(self) -> int:
        if len(self.visits) == 0:
            return 0
        total_driving_time_seconds = 0
        previous_location = self.home_location

        for visit in self.visits:
            total_driving_time_seconds += previous_location.driving_time_to(visit.location)
            previous_location = visit.location

        total_driving_time_seconds += previous_location.driving_time_to(self.home_location)
        return total_driving_time_seconds

    def __str__(self):
        return self.id

    def __repr__(self):
        return f'Vehicle({self.id})'


@planning_solution
class VehicleRoutePlan(BaseSchema):
    name: str
    south_west_corner: Annotated[Location, LocationSerializer, LocationValidator]
    north_east_corner: Annotated[Location, LocationSerializer, LocationValidator]
    vehicles: Annotated[list[Vehicle], PlanningEntityCollectionProperty]
    visits: Annotated[list[Visit], PlanningEntityCollectionProperty, ValueRangeProvider]
    score: Annotated[Optional[HardSoftScore],
                     PlanningScore,
                     ScoreSerializer,
                     ScoreValidator,
                     Field(default=None)]
    solver_status: Annotated[Optional[SolverStatus],
                             Field(default=None)]

    @computed_field
    @property
    def total_driving_time_seconds(self) -> int:
        out = 0
        for vehicle in self.vehicles:
            out += vehicle.total_driving_time_seconds
        return out

    def __str__(self):
        return f'VehicleRoutePlan(name={self.id}, vehicles={self.vehicles}, visits={self.visits})'


@dataclass
class MatchAnalysisDTO:
    name: str
    score: Annotated[HardSoftScore, ScoreSerializer]
    justification: object


@dataclass
class ConstraintAnalysisDTO:
    name: str
    weight: Annotated[HardSoftScore, ScoreSerializer]
    matches: list[MatchAnalysisDTO]
    score: Annotated[HardSoftScore, ScoreSerializer]
