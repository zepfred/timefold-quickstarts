from fastapi import FastAPI, Depends, Request
from fastapi.staticfiles import StaticFiles
from timefold.solver import SolverManager, SolverFactory, SolutionManager
from timefold.solver.config import (SolverConfig, ScoreDirectorFactoryConfig,
                                    TerminationConfig, Duration)
from typing import Annotated

from .domain import Timetable, Lesson, Room, Timeslot, ConstraintAnalysisDTO, MatchAnalysisDTO
from .constraints import school_timetabling_constraints
from .demo_data import DemoData, generate_demo_data


solver_config = SolverConfig(
    solution_class=Timetable,
    entity_class_list=[Lesson],
    score_director_factory_config=ScoreDirectorFactoryConfig(
        constraint_provider_function=school_timetabling_constraints
    ),
    termination_config=TerminationConfig(
        spent_limit=Duration(seconds=30)
    )
)

solver_manager = SolverManager.create(SolverFactory.create(solver_config))
solution_manager = SolutionManager.create(solver_manager)

app = FastAPI(docs_url='/q/swagger-ui')
data_sets = {}


@app.get("/demo-data")
async def demo_data_list():
    return [e.name for e in DemoData]


@app.get("/demo-data/{dataset_id}")
async def demo_data_list(dataset_id: str):
    return generate_demo_data(getattr(DemoData, dataset_id))


@app.get("/timetables/{problem_id}")
async def get_timetable(problem_id: str) -> Timetable:
    timetable = data_sets[problem_id]
    return timetable.model_copy(update={
        'solver_status': solver_manager.get_solver_status(problem_id)
    })


@app.delete("/timetables/{problem_id}")
async def stop_solving(problem_id: str) -> None:
    solver_manager.terminate_early(problem_id)


def update_timetable(problem_id: str, timetable: Timetable):
    global data_sets
    data_sets[problem_id] = timetable


@app.post("/timetables")
async def solve_timetable(timetable: Timetable) -> str:
    data_sets['ID'] = timetable
    solver_manager.solve_and_listen('ID', timetable,
                                    lambda solution: update_timetable('ID', solution))
    return 'ID'


async def setup_context(request: Request) -> Timetable:
    json = await request.json()
    return Timetable.model_validate(json,
                                    context={
                                        'rooms': {
                                            room['id']: Room.model_validate(room) for room in json.get('rooms', [])
                                        },
                                        'timeslots': {
                                            timeslot['id']: Timeslot.model_validate(timeslot) for timeslot in json.get('timeslots', [])
                                        },
                                    })


@app.put("/timetables/analyze")
async def analyze_timetable(timetable: Annotated[Timetable, Depends(setup_context)]) -> dict:
    return {'constraints': [ConstraintAnalysisDTO(
        name=constraint.constraint_name,
        weight=constraint.weight,
        score=constraint.score,
        matches=[
            MatchAnalysisDTO(
                name=match.constraint_ref.constraint_name,
                score=match.score,
                justification=match.justification
            )
            for match in constraint.matches
        ]
    ) for constraint in solution_manager.analyze(timetable).constraint_analyses]}


app.mount("/", StaticFiles(directory="static", html=True), name="static")
