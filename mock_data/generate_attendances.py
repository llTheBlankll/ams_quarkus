import datetime
import os
from typing import List, Optional
import numpy as np
from dotenv import load_dotenv
import psycopg2
from config import (
    TimeConfig,
    DateRangeConfig,
    AttendanceStatusConfig,
    BatchConfig,
    DatabaseConfig,
)


class AttendanceGenerator:
    def __init__(self):
        self.time_config = TimeConfig()
        self.date_config = DateRangeConfig()
        self.status_config = AttendanceStatusConfig()
        self.batch_config = BatchConfig()
        self.db_config = DatabaseConfig()
        self.db_connection = self._init_database()
        self.cursor = self.db_connection.cursor()

    def _init_database(self) -> psycopg2.extensions.connection:
        load_dotenv()
        return psycopg2.connect(
            database=os.getenv("POSTGRES_DB"),
            user=os.getenv("POSTGRES_USER"),
            password=os.getenv("POSTGRES_PASSWORD"),
            host=os.getenv("POSTGRES_HOST"),
            port=os.getenv("POSTGRES_PORT"),
        )

    def get_student_ids(self) -> List[int]:
        self.cursor.execute("SELECT id FROM students")
        return [r[0] for r in self.cursor.fetchall()]

    def random_time_in_range(self, start: str, end: str) -> Optional[str]:
        is_absent = np.random.choice([True, False], p=[0.1, 0.9])
        if is_absent:
            return None

        start_time = datetime.datetime.strptime(start, "%H:%M:%S")
        end_time = datetime.datetime.strptime(end, "%H:%M:%S")

        mean_seconds = int((end_time - start_time).total_seconds() / 2)
        std_dev = mean_seconds / 3

        random_seconds = int(np.random.normal(mean_seconds, std_dev))
        random_seconds = max(
            0, min(random_seconds, int((end_time - start_time).total_seconds()))
        )

        random_time = start_time + datetime.timedelta(seconds=random_seconds)
        return random_time.strftime("%H:%M:%S")

    def generate_attendance_record(
        self, student_id: int, date: datetime.datetime
    ) -> str:
        time_in = self.random_time_in_range(
            self.time_config.morning_start, self.time_config.morning_end
        )
        time_out = (
            None
            if time_in is None
            else self.random_time_in_range(
                self.time_config.afternoon_start, self.time_config.afternoon_end
            )
        )

        if time_in is None:
            status = "ABSENT"
        else:
            time_in_obj = datetime.datetime.strptime(time_in, "%H:%M:%S")
            cutoff = datetime.datetime.strptime(
                self.time_config.cutoff_time, "%H:%M:%S"
            )
            status = "LATE" if time_in_obj > cutoff else "ON_TIME"

        return (
            f"{status},{date.strftime('%Y-%m-%d')},"
            f"{time_in or ''},{time_out or ''},,"
            f"{student_id}\n"
        )

    def import_to_database(self, csv_file: str = "attendances.csv"):
        """Import the generated CSV file to database using COPY command"""
        try:
            # Get the absolute path of the CSV file
            csv_path = os.path.abspath(csv_file)

            # Create COPY command
            copy_command = f"""
                COPY {self.db_config.table_name} (
                    status, date, time_in, time_out, notes, student_id
                ) FROM STDIN WITH (
                    FORMAT CSV,
                    HEADER true
                )
            """

            print(f"Importing data from {csv_path} to database...")

            # Open and copy the CSV file
            with open(csv_path, 'r') as f:
                self.cursor.copy_expert(sql=copy_command, file=f)

            # Commit the transaction
            self.db_connection.commit()
            print("Data import completed successfully!")

        except Exception as e:
            self.db_connection.rollback()
            print(f"Error importing data: {e}")
        finally:
            self.cursor.close()
            self.db_connection.close()

    def generate(self):
        """Generate attendance records and save to CSV"""
        students = self.get_student_ids()
        start_date = datetime.datetime.strptime(self.date_config.start_date, "%Y-%m-%d")
        end_date = datetime.datetime.strptime(self.date_config.end_date, "%Y-%m-%d")
        dates = [
            start_date + datetime.timedelta(days=x)
            for x in range((end_date - start_date).days + 1)
        ]

        with open("attendances.csv", "w") as f:
            f.write(f"{self.db_config.csv_headers}\n")

            for count, student_id in enumerate(students):
                if count % self.batch_config.student_progress_interval == 0:
                    print(f"Processing student {count}/{len(students)}")

                rows = []
                for date in dates:
                    rows.append(self.generate_attendance_record(student_id, date))

                    if len(rows) >= self.batch_config.size:
                        f.write("".join(rows))
                        rows.clear()

                if rows:
                    f.write("".join(rows))


def main():
    print("Generating attendances...")
    print("This may take a while...")

    start_time = datetime.datetime.now()
    print("Start Time:", start_time)

    generator = AttendanceGenerator()
    generator.generate()

    end_time = datetime.datetime.now()
    print(f"Time taken to generate: {end_time - start_time}")

    # Ask user if they want to import the data
    if input("Do you want to import the generated data to database? (y/n): ").lower() == 'y':
        import_start_time = datetime.datetime.now()
        generator.import_to_database()
        import_end_time = datetime.datetime.now()
        print(f"Time taken to import: {import_end_time - import_start_time}")


if __name__ == "__main__":
    main()
