import os
import random

from dotenv import load_dotenv
import psycopg2
from entities.classroom import Classroom
from entities.grade_level import GradeLevel
from entities.guardian import Guardian
from entities.rfid_credential import RFIDCredential
from entities.strand import Strand
from faker import Faker
from bcrypt import hashpw, gensalt

from entities.student import Student
from entities.teacher import Teacher
from entities.user import User

fake = Faker()


def generate_strands():
    with open("./data/strands.txt", "r") as strands_file:
        mock_strands: list[Strand] = []
        for strand in strands_file.readlines():
            strand_data = Strand()
            strand_data.name = strand
            strand_data.description = fake.sentence(5)
            mock_strands.append(strand_data)
    return mock_strands


def generate_grade_levels():
    with open("./data/grade_levels.txt", "r") as file:
        mock_grade_levels: list[GradeLevel] = []
        for grade_level in file.readlines():
            data = GradeLevel()
            data.name = grade_level
            data.description = fake.sentence(5)
            mock_grade_levels.append(data)
    return mock_grade_levels


# Function to generate mock user data
def generate_users(count: int):
    users = []
    for i in range(count):
        user = User(
            username=fake.user_name(),
            password=hashpw(
                fake.password(length=32).encode("utf-8"), gensalt(12)
            ).decode("utf-8"),
            email=fake.unique.email(),
            profile_picture=fake.image_url(),
            role=fake.random_element(
                elements=("GUEST", "ADMIN", "TEACHER")
            ),  # Random role
            is_expired=fake.boolean(
                chance_of_getting_true=5
            ),  # 5% chance of being expired
            is_locked=fake.boolean(
                chance_of_getting_true=10
            ),  # 10% chance of being locked
            is_enabled=fake.boolean(
                chance_of_getting_true=90
            ),  # 90% chance of being enabled
            last_login=fake.date_time_this_year(),  # Last login within the current year
            created_at=fake.date_time_this_decade(),  # Creation date within the last 10 years
            updated_at=fake.date_time_this_year(),  # Updated within this year
        )
        users.append(user)
    return users


def generate_teachers(count: int):
    teachers: list[Teacher] = []
    for i in range(count):
        teacher = Teacher(
            first_name=fake.first_name(),
            last_name=fake.last_name(),
            middle_initial=fake.random_letter().upper(),  # Random middle initial
            age=fake.random_int(min=25, max=65),  # Random age between 25 and 65
            contact_number=fake.phone_number(),
            emergency_contact=fake.phone_number(),
            sex=fake.random_element(elements=("MALE", "FEMALE")),
            position=fake.job(),  # Random job title for position
            user_id=i + 1,  # User ID proportional to generated users (1 to count)
            created_at=fake.date_time_this_decade(),
            updated_at=fake.date_time_this_year(),
        )
        teachers.append(teacher)
    return teachers


def generate_classrooms(count: int):
    classrooms = []
    for i in range(count):
        classroom = Classroom(
            room=f"Building {fake.random_uppercase_letter()}, Room {fake.random_number(3, 3)}",
            # Random room identifier
            classroom_name=fake.word(),
            teacher_id=i + 1,  # Random teacher ID
            grade_level_id=random.choice(
                [1, 2]
            ),  # Randomly choose between grade levels 1 and 2
            created_at=fake.date_time_this_decade(),
            updated_at=fake.date_time_this_year(),
        )
        classrooms.append(classroom)
    return classrooms


def generate_students(
    count: int,
    classrooms_count: int,
    grade_levels_count: int,
    strands_count: int,
    guardians_count: int,
    student_schedules_count: int,
):
    students = []
    for i in range(count):
        student = Student(
            id=fake.random_number(13, True),
            first_name=fake.first_name(),
            middle_initial=fake.random_letter().upper(),  # Random middle initial
            last_name=fake.last_name(),
            prefix=fake.prefix(),
            sex=fake.random_element(elements=("MALE", "FEMALE")),
            address=fake.address(),
            birthdate=fake.date_of_birth(minimum_age=16, maximum_age=23),
            # Assuming students are between 5 and 18 years old
            classroom_id=random.randint(1, classrooms_count),
            # Random classroom ID
            grade_level_id=random.randint(
                1, grade_levels_count
            ),  # Random grade level ID
            strand_id=random.randint(1, strands_count),  # Random strand ID
            guardian_id=i + 1,
            # Random guardian ID
            student_schedule_id=random.randint(1, student_schedules_count),
            # Randomly choose between schedule IDs 1 and 2
            created_at=fake.date_time_this_decade(),
            updated_at=fake.date_time_this_year(),
        )
        students.append(student)
    return students


def generate_guardians(count: int):
    guardians = []
    for i in range(count):
        guardian = Guardian(
            id=i + 1,  # Simulate auto-incrementing ID
            full_name=fake.name(),  # Generate a full name
            contact_number=fake.phone_number(),  # Generate a phone number
        )
        guardians.append(guardian)
    return guardians


def generate():
    # * Create Strands
    with open("mocks/strands.sql", "w") as file:
        for strand in generate_strands():
            strand_statement = f"INSERT INTO strands (name, description) VALUES ('{sanitize_sql(strand.name)}', '{sanitize_sql(strand.description)}');\n"
            file.write(strand_statement)

    with open("mocks/grade_levels.sql", "w") as file:
        for grade_level in generate_grade_levels():
            grade_level_statement = f"INSERT INTO grade_levels (name, description) VALUES ('{sanitize_sql(grade_level.name)}', '{sanitize_sql(grade_level.description)}');\n"
            file.write(grade_level_statement)

    with open("mocks/users.sql", "w") as file:
        for user in generate_users(6):
            user_statement = (
                "INSERT INTO users (username, password, email, profile_picture, role, is_expired, is_locked, is_enabled, last_login) VALUES"
                + f"('{sanitize_sql(user.username)}', '{user.password}', '{sanitize_sql(user.email)}', '{sanitize_sql(user.profile_picture)}', '{sanitize_sql(user.role)}', {user.is_expired}, {user.is_locked}, {user.is_enabled}, '{user.last_login}');\n"
            )
            file.write(user_statement)

    with open("mocks/teachers.sql", "w") as file:
        for teacher in generate_teachers(6):
            teacher_statement = (
                "INSERT INTO teachers (first_name, last_name, middle_initial, age, contact_number, emergency_contact, sex, position, user_id) VALUES "
                + f"('{sanitize_sql(teacher.first_name)}', '{sanitize_sql(teacher.last_name)}', '{sanitize_sql(teacher.middle_initial)}', {teacher.age}, '{sanitize_sql(teacher.contact_number)}', '{sanitize_sql(teacher.emergency_contact)}', '{sanitize_sql(teacher.sex)}', '{sanitize_sql(teacher.position)}', {teacher.user_id});\n"
            )
            file.write(teacher_statement)

    with open("mocks/classrooms.sql", "w") as file:
        for classroom in generate_classrooms(6):
            classroom_statement = (
                "INSERT INTO classrooms (room, classroom_name, teacher_id, grade_level_id) VALUES "
                f"('{sanitize_sql(classroom.room)}', '{sanitize_sql(classroom.classroom_name)}', '{classroom.teacher_id}', '{classroom.grade_level_id}');\n"
            )
            file.write(classroom_statement)

    with open("mocks/guardians.sql", "w") as file:
        for guardian in generate_guardians(378):
            guardian_statement = (
                "INSERT INTO guardians (full_name, contact_number) VALUES "
                f"('{sanitize_sql(guardian.full_name)}', '{sanitize_sql(guardian.contact_number)}');\n"
            )
            file.write(guardian_statement)

    with open("mocks/students.sql", "w") as file:
        student_statement = "INSERT INTO students (id, first_name, middle_initial, last_name, prefix, sex, address,birthdate, classroom_id, strand_id, grade_level_id, guardian_id, student_schedule_id) VALUES "
        for i, student in enumerate(generate_students(378, 6, 2, 5, 276, 2)):
            student_statement += f"({student.id}, '{sanitize_sql(student.first_name)}', '{sanitize_sql(student.middle_initial)}', '{sanitize_sql(student.last_name)}', '{sanitize_sql(student.prefix)}', '{sanitize_sql(student.sex)}', '{sanitize_sql(student.address)}', '{student.birthdate}', {student.classroom_id}, {student.strand_id}, {student.grade_level_id}, {student.guardian_id},{student.student_schedule_id}),\n"
            print(i)
            if i >= 377:
                print("last! : " + str(i))
                student_statement = student_statement[:-2]
                student_statement += ";"
                print(student_statement[-1])
        file.write(student_statement)


def sanitize_sql(sql: str):
    return sql.replace("'", " ").replace("\n", " ").strip()


def import_to_db():
    load_dotenv()
    con = psycopg2.connect(
        database=os.getenv("POSTGRES_DB"),
        user=os.getenv("POSTGRES_USER"),
        password=os.getenv("POSTGRES_PASSWORD"),
        host=os.getenv("POSTGRES_HOST"),
        port=os.getenv("POSTGRES_PORT"),
    )
    cur = con.cursor()

    try:
        with open("mocks/strands.sql", "r") as file:
            cur.execute(file.read())

        with open("mocks/grade_levels.sql", "r") as file:
            cur.execute(file.read())
        con.commit()

        with open("mocks/users.sql", "r") as file:
            cur.execute(file.read())
        con.commit()

        with open("mocks/teachers.sql", "r") as file:
            cur.execute(file.read())
        con.commit()

        with open("mocks/classrooms.sql", "r") as file:
            cur.execute(file.read())
        con.commit()

        with open("mocks/guardians.sql", "r") as file:
            cur.execute(file.read())
        con.commit()

        with open("mocks/students.sql", "r") as file:
            cur.execute(file.read())
        con.commit()

    except Exception as e:
        print(e)

    print("All mock data has been successfully imported to the database.")
    cur.close()
    con.close()


if __name__ == "__main__":
    print("Generating mock data...")
    generate()
    print("Mock data generated successfully!")
    s = str(input("Do you want to import to database? (y/n)"))
    if s == "y":
        import_to_db()
