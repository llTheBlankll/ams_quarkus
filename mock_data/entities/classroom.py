from dataclasses import field, dataclass
from datetime import datetime
from typing import Optional


@dataclass
class Classroom:
    id: Optional[int] = None
    room: Optional[str] = None
    classroom_name: Optional[str] = None
    teacher_id: Optional[int] = None
    grade_level_id: int = 1  # Default to grade level 11
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: datetime = field(default_factory=datetime.now)
