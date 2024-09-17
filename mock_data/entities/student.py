from dataclasses import field, dataclass
from datetime import datetime
from typing import Optional


@dataclass
class Student:
    id: Optional[int] = None
    first_name: Optional[str] = None
    middle_initial: Optional[str] = None
    last_name: Optional[str] = None
    prefix: Optional[str] = None
    sex: str = 'Male'  # Default to 'Male', could be 'Female' or other values
    address: Optional[str] = None
    birthdate: Optional[datetime] = None
    classroom_id: Optional[int] = None
    grade_level_id: Optional[int] = None
    strand_id: Optional[int] = None
    guardian_id: Optional[int] = None
    student_schedule_id: int = 1  # Default to 1 could be 1 or 2
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: datetime = field(default_factory=datetime.now)
