from dataclasses import dataclass
from typing import Optional


@dataclass
class GradeLevel:
    name: str
    id: Optional[int] = None
    description: Optional[str] = None

    def __init__(self):
        pass