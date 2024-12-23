from dataclasses import dataclass
from datetime import datetime
import numpy as np


@dataclass
class TimeConfig:
    morning_start: str = "05:00:00"
    morning_end: str = "07:00:00"
    afternoon_start: str = "12:00:00"
    afternoon_end: str = "18:00:00"
    cutoff_time: str = "06:00:00"


@dataclass
class DateRangeConfig:
    start_date: str = "2020-01-01"
    end_date: str = "2024-12-31"


@dataclass
class AttendanceStatusConfig:
    statuses = {"ON_TIME": 0.73, "ABSENT": 0.05, "LATE": 0.20, "EXCUSED": 0.02}


@dataclass
class BatchConfig:
    size: int = 5000
    student_progress_interval: int = 10


@dataclass
class DatabaseConfig:
    table_name: str = "attendances"
    csv_headers: str = "status,date,time_in,time_out,notes,student_id"


class GeneratorConfig:
    MOCK_DATA_DIR = "mocks"
    DATA_DIR = "data"
    DEFAULT_USER_COUNT = 8
    DEFAULT_TEACHER_COUNT = DEFAULT_USER_COUNT
    DEFAULT_CLASSROOM_COUNT = DEFAULT_TEACHER_COUNT
    DEFAULT_STUDENT_COUNT = 500
    DEFAULT_GUARDIAN_COUNT = DEFAULT_STUDENT_COUNT
    DEFAULT_GRADE_LEVELS = 2
    DEFAULT_STRANDS_COUNT = 5
    DEFAULT_STUDENT_SCHEDULES = 2

    # Probability configs
    USER_EXPIRED_CHANCE = 1  # 1%
    USER_LOCKED_CHANCE = 5  # 5%
    USER_ENABLED_CHANCE = 94  # 94%
