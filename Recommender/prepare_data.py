import json
import gzip
import data_parser as dp
import mysql.connector


db = mysql.connector.connect(
    host="localhost",
    user="root",
    password="12345678",
    database="recommender",
    auth_plugin="mysql_native_password"
)
dbcursor = db.cursor()

insert_book = "INSERT INTO books (asin, title, description) VALUES (%s, %s, %s)"
select_book = "SELECT id FROM books WHERE asin = "
insert_review = "INSERT INTO reviews (book_id, weight, text) VALUES (%s, %s, %s)"
delete_book = "DELETE FROM books WHERE id = "
delete_reviews = "DELETE FROM reviews WHERE book_id = "


if __name__ == "__main__":
    generator = dp.parse_data("meta_Books.json.gz")
    counter = 0
    train_counter = 0

    # Adding books to database.
    for book in generator:
        if counter < 103443:
            if "description" in book and "title" in book:
                desc = book.get("description")
                if len(desc.split()) > 100:
                    counter += 1

                    asin = book["asin"]
                    title = book["title"]

                    new_book = (asin, title, desc)
                    dbcursor.execute(insert_book, new_book)
                    db.commit()

    generator = dp.parse_data("reviews_Books.json.gz")
    train_file = gzip.open("train_reviews.json.gz", 'wt', encoding="UTF-8")

    # Processing reviews.
    for review in generator:
        # Creating training dataset.
        if train_counter < 100000:
            overall = review.get("overall")
            if overall != 3.0:
                train_review = dict()
                train_review["reviewText"] = review.get("reviewText")
                train_review["overall"] = review.get("overall")
                train_file.write(json.dumps(train_review) + "\n")
                train_counter += 1

        helpful = review.get("helpful")
        # Script ignores reviews with weight equal to 0.
        if helpful[1] > 0:
            asin = review.get("asin")
            dbcursor.execute(select_book + '\"' + asin + '\"')
            book_id = dbcursor.fetchone()
            if book_id is not None:  # If book of review is in database.
                text = review.get("reviewText")
                weight = helpful[0] / helpful[1]
                new_review = (book_id[0], weight, text)
                dbcursor.execute(insert_review, new_review)
                db.commit()

    train_file.close()
