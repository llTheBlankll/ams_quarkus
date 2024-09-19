import string
import random
import psycopg2
import hashlib

connection = psycopg2.connect(database="ams_quarkus", user="postgres", password="Asharia2100", host="localhost",
                              port=5432)

cursor = connection.cursor()

cursor.execute("SELECT * from students")

# Fetch all rows from database
records = cursor.fetchall()

for record in records:
    randomString = ''.join(random.choices(string.ascii_uppercase + string.digits, k=16))
    hashedLrn = hashlib.md5(f"{str(record[0]) + randomString}".encode('utf-8')).hexdigest()
    print(randomString, hashedLrn)
    cursor.execute(
        f"INSERT INTO rfid_credentials (student_id, hashed_lrn, salt) VALUES ({record[0]}, '{hashedLrn}', '{randomString}')")

print("Done")
connection.commit()
