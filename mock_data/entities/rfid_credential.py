from dataclasses import dataclass
from typing import Optional


@dataclass
class RFIDCredential:
    id: Optional[int]
    student_id: int
    hashed_lrn: str
    salt: str
