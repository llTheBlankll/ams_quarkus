from dataclasses import dataclass
from typing import Optional


@dataclass
class Guardian:
    id: Optional[int] = None
    full_name: Optional[str] = None
    contact_number: Optional[str] = None
