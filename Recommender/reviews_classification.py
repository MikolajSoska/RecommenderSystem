import nltk
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split

import data_parser as dp
import database_connector as connector
import text_processing as text

select_reviews = "SELECT id, text FROM reviews WHERE positive IS NULL LIMIT 10000"
set_sentiment = "UPDATE reviews SET positive = %s WHERE id = %s"

if __name__ == "__main__":
    generator = dp.parse_data("train_reviews.json.gz")
    stop_words = nltk.corpus.stopwords.words('english')
    dbcursor = connector.get_cursor()

    # Setting output labels.
    reviews = list()
    for review in generator:
        overall = float(review.get("overall"))
        if overall > 3:
            review["label"] = 1
        else:
            review["label"] = 0
        reviews.append(review)

    # Splitting the data.
    train_data, test_data = train_test_split(reviews, train_size=0.8, shuffle=True)

    reviews_text = list()
    reviews_label = list()
    for review in train_data:
        reviews_text.append(review.get("reviewText"))
        reviews_label.append(review.get("label"))

    test_reviews = list()
    test_labels = list()
    for review in test_data:
        test_reviews.append(review.get("reviewText"))
        test_labels.append(review.get("label"))

    # TF-IDF vectorizing.
    tf_idf = TfidfVectorizer(min_df=10, stop_words=stop_words, tokenizer=text.stem_words)
    train_matrix = tf_idf.fit_transform(reviews_text)
    test_matrix = tf_idf.transform(test_reviews)

    # Classification algorithm training.
    classifier = LogisticRegression(solver="liblinear")
    classifier.fit(train_matrix, reviews_label)

    # Classification algorithm testing.
    predictions = classifier.predict(test_matrix)
    right = 0
    for i in range(len(predictions)):
        if predictions[i] == test_labels[i]:
            right += 1

    print("accuracy: " + str(right / len(predictions)))

    # Predicting labels for reviews and inserting into database.
    dbcursor.execute(select_reviews)
    reviews = dbcursor.fetchall()
    while len(reviews) > 0:
        reviews_text = list()
        for review in reviews:
            reviews_text.append(review[1])
        tf_id_matrix = tf_idf.transform(reviews_text)
        predictions = classifier.predict(tf_id_matrix)
        for i in range(len(reviews)):
            review_id = reviews[i][0]
            sentiment = int(predictions[i])
            arguments = (sentiment, review_id)
            dbcursor.execute(set_sentiment, arguments)
        connector.db.commit()
        dbcursor.execute(select_reviews)
        reviews = dbcursor.fetchall()
