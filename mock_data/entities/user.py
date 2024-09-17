from dataclasses import dataclass, field
from typing import Optional
from datetime import datetime


@dataclass
class User:
    id: Optional[int] = None  # SERIAL, automatically generated, nullable initially
    username: Optional[str] = None  # Must be unique, length >= 3
    password: Optional[str] = None  # Fixed 60-char string
    email: Optional[str] = None  # Must be unique, length >= 3
    profile_picture: Optional[str] = None  # Text field for profile picture URL/path
    role: str = 'GUEST'  # Default role is 'GUEST'
    is_expired: bool = False  # Default is not expired
    is_locked: bool = False  # Default is not locked
    is_enabled: bool = True  # Default is enabled
    last_login: Optional[datetime] = None  # Timestamp for last login, nullable
    created_at: datetime = field(default_factory=datetime.now)  # Set default to current timestamp
    updated_at: datetime = field(default_factory=datetime.now)  # Set default to current timestamp
