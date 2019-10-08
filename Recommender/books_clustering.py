import nltk
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import TfidfVectorizer

import database_connector as connector
import text_processing as text

select_books = "SELECT id, description FROM books"
set_cluster = "UPDATE books SET cluster = %s WHERE id = %s"
insert_words_vector = "INSERT INTO word_vectors VALUES (%s, %s, %s)"


def tokenizer(description_tokens: str):
    return description_tokens.split()


if __name__ == "__main__":
    dbcursor = connector.get_cursor()
    stop_words = nltk.corpus.stopwords.words('english')
    descriptions = list()

    # Getting books from database.
    dbcursor.execute(select_books)
    books = dbcursor.fetchall()

    # Stemming and tokenizing descriptions.
    # Descriptions are left in text form, because they need to be process couple of times.
    for book in books:
        descriptions.append(" ".join(text.stem_words(book[1])))

    # TF-IDF vectorizing.
    tf_idf = TfidfVectorizer(max_df=0.7, min_df=10, stop_words=stop_words, tokenizer=tokenizer)
    tf_idf_matrix = tf_idf.fit_transform(descriptions)

    # Clustering with k-means.
    cluster_number = 3
    kmeans = KMeans(n_clusters=cluster_number, init='k-means++', random_state=0)
    kmeans.fit(tf_idf_matrix)

    clusters = kmeans.labels_.tolist()
    clusters_words_lists = list()
    books_cluster = list()

    for i in range(cluster_number):
        clusters_words_lists.append(list())
        books_cluster.append(list())

    # Setting cluster indexes in database and splitting descriptions in same way as clusters.
    for i in range(len(books)):
        cluster = int(clusters[i])
        book_id = books[i][0]
        cluster_tuple = (cluster, book_id)
        dbcursor.execute(set_cluster, cluster_tuple)
        connector.db.commit()

        description = descriptions[i]
        books_cluster[cluster - 1].append(book_id)
        clusters_words_lists[cluster - 1].append(description)

    # Creating new TF-IDF matrices for each cluster.
    clusters_tf_idf = list()
    for desc in clusters_words_lists:
        clusters_tf_idf.append(tf_idf.fit_transform(desc))

    # Splitting matrices into words vectors and inserting into database.
    for i in range(len(clusters_tf_idf)):
        books_list = books_cluster[i]
        cluster_tf_idf_matrix = clusters_tf_idf[i]
        for j in range(len(books_list)):
            book_id = books_list[j]
            vector = cluster_tf_idf_matrix.getrow(j).tocoo()

            words = vector.col.tolist()
            values = vector.data.tolist()

            for k in range(len(words)):
                word_vector = (book_id, int(words[k]), float(values[k]))
                dbcursor.execute(insert_words_vector, word_vector)
            connector.db.commit()
