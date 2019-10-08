import mysql.connector

# Connecting to database.
db = mysql.connector.connect(
    host="localhost",
    user="root",
    password="12345678",
    database="recommender",
    auth_plugin="mysql_native_password"
)


def get_cursor():
    return db.cursor()
