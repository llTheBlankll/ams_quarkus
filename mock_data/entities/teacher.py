from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional


@dataclass
class Teacher:
    id: Optional[int] = None
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    middle_initial: Optional[str] = None
    age: Optional[int] = None
    contact_number: Optional[str] = None
    emergency_contact: Optional[str] = None
    sex: Optional[str] = None
    position: Optional[str] = None
    user_id: Optional[int] = None
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: datetime = field(default_factory=datetime.now)
