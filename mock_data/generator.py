import os
import random
from pathlib import Path
from typing import List

from dotenv import load_dotenv
import psycopg2
from entities.classroom import Classroom
from entities.grade_level import GradeLevel
from entities.guardian import Guardian
from entities.strand import Strand
from faker import Faker
from passlib.hash import bcrypt

from entities.student import Student
from entities.teacher import Teacher
from entities.user import User
from config import GeneratorConfig

fake = Faker()


class MockDataGenerator:
    def __init__(self, base_dir: str = "."):
        self.base_dir = Path(base_dir)
        self.mock_dir = self.base_dir / GeneratorConfig.MOCK_DATA_DIR
        self.data_dir = self.base_dir / GeneratorConfig.DATA_DIR

        # Create directories if they don't exist
        self.mock_dir.mkdir(exist_ok=True)

    def _read_file_lines(self, filename: str) -> List[str]:
        """Read lines from a file in the data directory."""
        file_path = self.data_dir / filename
        with open(file_path, "r") as f:
            return [line.strip() for line in f.readlines()]

    def generate_strands(self) -> List[Strand]:
        """Generate strand data from strands.txt"""
        strands = []
        for strand_name in self._read_file_lines("strands.txt"):
            strand = Strand()
            strand.name = strand_name
            strand.description = fake.sentence(5)
            strands.append(strand)
        return strands

    def generate_grade_levels(self) -> List[GradeLevel]:
        """Generate grade level data from grade_levels.txt"""
        grade_levels = []
        for grade_name in self._read_file_lines("grade_levels.txt"):
            grade = GradeLevel()
            grade.name = grade_name
            grade.description = fake.sentence(5)
            grade_levels.append(grade)
        return grade_levels

    def generate_users(self, count: int) -> List[User]:
        """Generate mock user data"""
        return [
            User(
                username=fake.user_name(),
                password=bcrypt.using(rounds=12, ident="2y").hash(
                    '1234'
                ),
                email=fake.unique.email(),
                profile_picture=fake.image_url(),
                role=fake.random_element(
                    elements=[("GUEST", 0.005), ("ADMIN", 0.1), ("TEACHER", 0.845)]
                ),
                is_expired=fake.boolean(
                    chance_of_getting_true=GeneratorConfig.USER_EXPIRED_CHANCE
                ),
                is_locked=fake.boolean(
                    chance_of_getting_true=GeneratorConfig.USER_LOCKED_CHANCE
                ),
                is_enabled=fake.boolean(
                    chance_of_getting_true=GeneratorConfig.USER_ENABLED_CHANCE
                ),
                last_login=fake.date_time_this_year(),
                created_at=fake.date_time_this_decade(),
                updated_at=fake.date_time_this_year(),
            )
            for _ in range(count)
        ]

    def generate_teachers(self, count: int) -> List[Teacher]:
        """Generate teacher data"""
        return [
            Teacher(
                first_name=fake.first_name(),
                last_name=fake.last_name(),
                middle_initial=fake.random_letter().upper(),
                age=fake.random_int(min=25, max=65),
                contact_number=fake.phone_number(),
                emergency_contact=fake.phone_number(),
                sex=fake.random_element(elements=("MALE", "FEMALE")),
                position=fake.job(),
                user_id=i + 1,
                created_at=fake.date_time_this_decade(),
                updated_at=fake.date_time_this_year(),
            )
            for i in range(count)
        ]

    def generate_classrooms(self, count: int, grade_levels_count: int) -> List[Classroom]:
        """Generate classroom data"""
        return [
            Classroom(
                room=f"Building {fake.random_uppercase_letter()}, Room {fake.random_number(3, 3)}",
                classroom_name=f"{fake.first_name()} {fake.last_name()}",
                teacher_id=i + 1,
                grade_level_id=random.randint(1, grade_levels_count),
                created_at=fake.date_time_this_decade(),
                updated_at=fake.date_time_this_year(),
            )
            for i in range(count)
        ]

    def generate_students(
        self,
        count: int,
        classrooms_count: int,
        grade_levels_count: int,
        strands_count: int,
        guardians_count: int,
        student_schedules_count: int,
    ) -> List[Student]:
        """Generate student data"""
        return [
            Student(
                id=fake.random_number(13, True),
                first_name=fake.first_name(),
                middle_initial=fake.random_letter().upper(),
                last_name=fake.last_name(),
                prefix=fake.prefix(),
                sex=fake.random_element(elements=("MALE", "FEMALE")),
                address=fake.address(),
                birthdate=fake.date_of_birth(minimum_age=16, maximum_age=23),
                classroom_id=random.randint(1, classrooms_count),
                grade_level_id=random.randint(1, grade_levels_count),
                strand_id=random.randint(1, strands_count),
                guardian_id=i + 1,
                student_schedule_id=random.randint(1, student_schedules_count),
                created_at=fake.date_time_this_decade(),
                updated_at=fake.date_time_this_year(),
            )
            for i in range(count)
        ]

    def generate_guardians(self, count: int) -> List[Guardian]:
        """Generate guardian data"""
        return [
            Guardian(
                id=i + 1,
                full_name=fake.name(),
                contact_number=fake.phone_number(),
            )
            for i in range(count)
        ]

    def write_sql_file(self, filename: str, sql_statements: List[str]):
        """Write SQL statements to a file in the mock directory"""
        file_path = self.mock_dir / filename
        with open(file_path, "w") as f:
            f.writelines(sql_statements)

    @staticmethod
    def sanitize_sql(sql: str) -> str:
        """Sanitize SQL string"""
        return sql.replace("'", " ").replace("\n", " ").strip()

    def generate(self):
        """Generate all mock data"""
        # Generate data
        strands = self.generate_strands()
        grade_levels = self.generate_grade_levels()
        users = self.generate_users(GeneratorConfig.DEFAULT_USER_COUNT)
        teachers = self.generate_teachers(GeneratorConfig.DEFAULT_TEACHER_COUNT)
        classrooms = self.generate_classrooms(
            GeneratorConfig.DEFAULT_CLASSROOM_COUNT,
            GeneratorConfig.DEFAULT_GRADE_LEVELS
        )
        guardians = self.generate_guardians(GeneratorConfig.DEFAULT_GUARDIAN_COUNT)
        students = self.generate_students(
            GeneratorConfig.DEFAULT_STUDENT_COUNT,
            GeneratorConfig.DEFAULT_CLASSROOM_COUNT,
            GeneratorConfig.DEFAULT_GRADE_LEVELS,
            GeneratorConfig.DEFAULT_STRANDS_COUNT,
            GeneratorConfig.DEFAULT_GUARDIAN_COUNT,
            GeneratorConfig.DEFAULT_STUDENT_SCHEDULES,
        )

        # Write SQL files
        data_mappings = {
            "strands.sql": [
                f"INSERT INTO strands (name, description) VALUES "
                f"('{self.sanitize_sql(s.name)}', '{self.sanitize_sql(s.description)}');\n"
                for s in strands
            ],
            "grade_levels.sql": [
                f"INSERT INTO grade_levels (name, description) VALUES "
                f"('{self.sanitize_sql(g.name)}', '{self.sanitize_sql(g.description)}');\n"
                for g in grade_levels
            ],
            "users.sql": [
                f"INSERT INTO users (username, password, email, profile_picture, role, "
                f"is_expired, is_locked, is_enabled, last_login, created_at, updated_at) VALUES "
                f"('{self.sanitize_sql(u.username)}', '{u.password}', "
                f"'{self.sanitize_sql(u.email)}', '{self.sanitize_sql(u.profile_picture)}', "
                f"'{self.sanitize_sql(u.role)}', {u.is_expired}, {u.is_locked}, "
                f"{u.is_enabled}, '{u.last_login}', '{u.created_at}', '{u.updated_at}');\n"
                for u in users
            ],
            "teachers.sql": [
                f"INSERT INTO teachers (first_name, last_name, middle_initial, age, "
                f"contact_number, emergency_contact, sex, position, user_id, created_at, updated_at) VALUES "
                f"('{self.sanitize_sql(t.first_name)}', '{self.sanitize_sql(t.last_name)}', "
                f"'{self.sanitize_sql(t.middle_initial)}', {t.age}, "
                f"'{self.sanitize_sql(t.contact_number)}', '{self.sanitize_sql(t.emergency_contact)}', "
                f"'{self.sanitize_sql(t.sex)}', '{self.sanitize_sql(t.position)}', {t.user_id}, "
                f"'{t.created_at}', '{t.updated_at}');\n"
                for t in teachers
            ],
            "classrooms.sql": [
                f"INSERT INTO classrooms (room, classroom_name, teacher_id, grade_level_id, created_at, updated_at) VALUES "
                f"('{self.sanitize_sql(c.room)}', '{self.sanitize_sql(c.classroom_name)}', "
                f"{c.teacher_id}, {c.grade_level_id}, '{c.created_at}', '{c.updated_at}');\n"
                for c in classrooms
            ],
            "guardians.sql": [
                f"INSERT INTO guardians (id, full_name, contact_number) VALUES "
                f"({g.id}, '{self.sanitize_sql(g.full_name)}', '{self.sanitize_sql(g.contact_number)}');\n"
                for g in guardians
            ],
            "students.sql": [
                "INSERT INTO students (id, first_name, middle_initial, last_name, prefix, "
                "sex, address, birthdate, classroom_id, strand_id, grade_level_id, "
                "guardian_id, student_schedule_id, created_at, updated_at) VALUES "
                + ",".join(
                    [
                        f"({s.id}, '{self.sanitize_sql(s.first_name)}', "
                        f"'{self.sanitize_sql(s.middle_initial)}', '{self.sanitize_sql(s.last_name)}', "
                        f"'{self.sanitize_sql(s.prefix)}', '{self.sanitize_sql(s.sex)}', "
                        f"'{self.sanitize_sql(s.address)}', '{s.birthdate}', {s.classroom_id}, "
                        f"{s.strand_id}, {s.grade_level_id}, {s.guardian_id}, {s.student_schedule_id}, "
                        f"'{s.created_at}', '{s.updated_at}')"
                        for s in students
                    ]
                )
                + ";"
            ],
        }

        for filename, statements in data_mappings.items():
            self.write_sql_file(filename, statements)

    def import_to_db(self):
        """Import generated SQL files to database"""
        load_dotenv()
        with psycopg2.connect(
            database=os.getenv("POSTGRES_DB"),
            user=os.getenv("POSTGRES_USER"),
            password=os.getenv("POSTGRES_PASSWORD"),
            host=os.getenv("POSTGRES_HOST"),
            port=os.getenv("POSTGRES_PORT"),
        ) as conn:
            with conn.cursor() as cur:
                sql_files = [
                    "strands.sql",
                    "grade_levels.sql",
                    "users.sql",
                    "teachers.sql",
                    "classrooms.sql",
                    "guardians.sql",
                    "students.sql",
                ]

                for sql_file in sql_files:
                    try:
                        with open(self.mock_dir / sql_file, "r") as f:
                            cur.execute(f.read())
                        conn.commit()
                    except Exception as e:
                        print(f"Error importing {sql_file}: {e}")
                        conn.rollback()


def main():
    generator = MockDataGenerator()
    print("Generating mock data...")
    generator.generate()
    print("Mock data generated successfully!")

    if input("Do you want to import to database? (y/n): ").lower() == "y":
        generator.import_to_db()
        print("All mock data has been successfully imported to the database.")


if __name__ == "__main__":
    main()
