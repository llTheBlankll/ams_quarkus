import io
import os
import psycopg2
import datetime
import random
import numpy
from dotenv import load_dotenv

load_dotenv()

con = psycopg2.connect(
    database=os.getenv("DB_DATABASE"),
    user=os.getenv("DB_USER"),
    password=os.getenv("DB_PASSWORD"),
    host=os.getenv("DB_HOST"),
    port=os.getenv("DB_PORT"),
)
cur = con.cursor()


def get_student_ids():
    cur.execute("SELECT id FROM students")
    return [r[0] for r in cur.fetchall()]


def random_time_in_range(start: str, end: str):
    start_time = datetime.datetime.strptime(start, "%H:%M:%S")
    end_time = datetime.datetime.strptime(end, "%H:%M:%S")
    random_time = start_time + datetime.timedelta(
        seconds=random.randint(0, int((end_time - start_time).total_seconds())),
    )
    return random_time.strftime("%H:%M:%S")


def random_status_generator():
    return numpy.random.choice(["ON_TIME", "ABSENT", "LATE", "EXCUSED"], p=[0.6, 0.10, 0.25, 0.05])


def generate(batch_size=1000):
    students = get_student_ids()  # Efficient student retrieval
    start_date = datetime.datetime.strptime("2022-01-01", "%Y-%m-%d")
    end_date = datetime.datetime.strptime("2024-12-31", "%Y-%m-%d")
    num_days = (end_date - start_date).days

    # Precompute all dates once
    dates = [start_date + datetime.timedelta(days=day) for day in range(num_days)]

    with open("attendances.csv", "w") as f:
        f.write("status,date,time_in,time_out,notes,student_id\n")

        for student_id in students:
            rows = []
            for date in dates:
                time_in = random_time_in_range("05:45:00", "08:00:00")
                time_out = random_time_in_range("19:00:00", "20:00:00")
                status = random_status_generator()
                notes = ""

                # Append row to the current batch
                rows.append(f"{status},{date},{time_in},{time_out},{notes},{student_id}\n")

                # Once the batch size is reached, write to file and clear memory
                if len(rows) >= batch_size:
                    f.write("".join(rows))
                    rows.clear()  # Clear the list to free up memory

            # Write any remaining rows for the current student
            if rows:
                f.write("".join(rows))


# copy attendances (status, date, time_in, time_out, notes, student_id) FROM 'C:\\Users\\Nytri\Projects\\ams_quarkus\mock_data\\attendances.csv' DELIMITER ',' CSV HEADER;
if __name__ == '__main__':
    # Generate attendances
    print("Generating attendances...")
    print("This may take a while...")

    start_time = datetime.datetime.now()
    print("Start Time:", start_time)
    generate(batch_size=1000)
    end_time = datetime.datetime.now()
    print(f"Time taken: {end_time - start_time}")
