-- * Create enum types for each table.
CREATE TYPE AttendanceStatus AS ENUM(
    'LATE',
    'ON_TIME',
    'EXCUSED',
    'ABSENT'
);

CREATE TYPE Sex AS ENUM('MALE', 'FEMALE');

-- CREATE A CAST, The CREATE CAST solution does not seem to work when the enum
-- is used as an argument of a JPA Repository.
-- Entity findByMyEnum(MyEnum myEnum)
CREATE CAST (
    CHARACTER VARYING as AttendanceStatus
)
WITH
    INOUT AS IMPLICIT;

CREATE TABLE strands (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

-- * GRADE LEVELS TABLE
CREATE TABLE IF NOT EXISTS grade_levels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_name UNIQUE (name),
    -- The length of a name should be at least 3 characters.
    CONSTRAINT name_length CHECK (LENGTH(name) >= 3)
);

-- * CREATE USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(64) UNIQUE,
    password CHAR(60),
    email VARCHAR(128) UNIQUE,
    profile_picture TEXT,
    role VARCHAR(48) DEFAULT 'GUEST',
    is_expired BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    is_enabled BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (LENGTH(username) >= 3),
    CHECK (LENGTH(email) >= 3)
);

-- * Creates Teachers Table.
CREATE TABLE IF NOT EXISTS teachers (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(32),
    last_name VARCHAR(32),
    middle_initial VARCHAR(4),
    profile_picture VARCHAR(255) NULL,
    age INT,
    contact_number VARCHAR(32),
    emergency_contact VARCHAR(32),
    sex VARCHAR(16),
    position VARCHAR(128),
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- * CLASSROOMS TABLE
CREATE TABLE IF NOT EXISTS classrooms (
    id SERIAL PRIMARY KEY,
    room VARCHAR(255) NOT NULL,
    classroom_name VARCHAR(255) NOT NULL,
    profile_picture VARCHAR(255) NULL,
    teacher_id INT NULL,
    grade_level_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (grade_level_id) REFERENCES grade_levels (id) ON DELETE SET NULL,
    FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE SET NULL
);

-- * GUARDIANS TABLE
CREATE TABLE IF NOT EXISTS guardians (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(128) NOT NULL,
    contact_number VARCHAR(32) NULL,
    CHECK (LENGTH(full_name) >= 2)
);

CREATE INDEX guardian_full_name_idx ON guardians (full_name);

CREATE TABLE IF NOT EXISTS student_schedules (
    id SERIAL PRIMARY KEY,
    on_time TIME,
    late_time TIME,
    absent_time TIME,
    is_flag BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX student_schedules_on_time_idx ON student_schedules (on_time);

CREATE INDEX student_schedules_late_time_idx ON student_schedules (late_time);

-- * STUDENTS TABLE
-- Mockaroo
-- if level == "Grade 11" || level == "Grade 12" then "Senior High School"
-- elseif level == "Grade 7" || level == "Grade 8" || level == "Grade 9" || level == "Grade 10" then "Junior High School"
-- elseif level == "Grade 1" || level == "Grade 2" || level == "Grade 3" || level == "Grade 4" || level == "Grade 5" || level == "Grade 6" then "Elementary"
-- end
CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY,
    first_name VARCHAR(128) NOT NULL,
    middle_initial VARCHAR(8) NULL,
    last_name VARCHAR(128) NOT NULL,
    prefix VARCHAR(8) NULL,
    sex Sex NOT NULL,
    address TEXT,
    birthdate DATE NOT NULL,
    classroom_id INT,
    grade_level_id INT,
    strand_id INT,
    guardian_id INT,
    student_schedule_id INT,
    CHECK (LENGTH(first_name) >= 2),
    CHECK (LENGTH(last_name) >= 2),
    FOREIGN KEY (grade_level_id) REFERENCES grade_levels (id) ON DELETE SET NULL,
    FOREIGN KEY (classroom_id) REFERENCES classrooms (id) ON DELETE SET NULL,
    FOREIGN KEY (strand_id) REFERENCES strands (id) ON DELETE SET NULL,
    FOREIGN KEY (guardian_id) REFERENCES guardians (id) ON DELETE SET NULL,
    FOREIGN KEY (student_schedule_id) REFERENCES student_schedules (id) ON DELETE SET NULL
);

CREATE INDEX students_section_id_idx ON students (classroom_id);

CREATE INDEX students_grade_level_idx ON students (grade_level_id);

-- * RFID CREDENTIALS
CREATE TABLE IF NOT EXISTS rfid_credentials (
    id SERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE,
    hashed_lrn CHAR(32) NOT NULL,
    salt VARCHAR(16) NOT NULL,
    CHECK (LENGTH(hashed_lrn) = 32),
    CHECK (LENGTH(salt) = 16),
    FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE
);

CREATE INDEX rfid_credentials_lrn_idx ON rfid_credentials (student_id);

-- * ATTENDANCE TABLE
CREATE TABLE IF NOT EXISTS attendances (
    id SERIAL PRIMARY KEY,
    status AttendanceStatus NOT NULL DEFAULT 'ABSENT',
    date DATE DEFAULT CURRENT_DATE,
    time_in TIME DEFAULT LOCALTIME,
    time_out TIME DEFAULT LOCALTIME,
    notes TEXT,
    student_id BIGINT NULL,
    UNIQUE (student_id, date, status),
    CONSTRAINT fk_student_lrn FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX attendance_date_idx on attendances (date);

CREATE INDEX attendance_status_idx on attendances (status);

CREATE INDEX attendance_student_id_idx on attendances (student_id);

-- * MAKE ATTENDANCE ENUM TYPE CHARACTER VARYING
ALTER TABLE attendances
ALTER COLUMN status TYPE CHARACTER VARYING;