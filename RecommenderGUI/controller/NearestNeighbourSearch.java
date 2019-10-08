package recommender.controller;

import recommender.model.Book;
import recommender.model.Review;
import recommender.model.Word;

import java.util.ArrayList;
import java.util.List;

/**
 * Class performs nearest neighbour search algorithm. This class also sorts book based on their ranking.
 */
public class NearestNeighbourSearch extends Thread {
    private final MainController controller;
    private final DatabaseConnector database;
    private final Book book;

    private boolean stopFlag;

    private static final int BOOKS_SIZE = 20;

    /**
     * Constructor.
     *
     * @param book       input book
     * @param controller MainController instance
     * @param database   DatabaseConnector instance
     */
    NearestNeighbourSearch(final Book book, final MainController controller, final DatabaseConnector database) {
        this.book = book;
        this.controller = controller;
        this.database = database;
        this.stopFlag = false;
    }

    /**
     * Method computes similarity between two books.
     *
     * @param words1 word vector for first book
     * @param words2 word vector for second book
     * @return similarity value
     */
    private double computeSimilarity(final List<Word> words1, final List<Word> words2) {
        double similarity = 0;
        for (final Word word1 : words1) {
            final int index = words2.indexOf(word1);
            if (index != -1) {
                final Word word2 = words2.get(words2.indexOf(word1));
                similarity += word1.getWordValue() * word2.getWordValue();
            }
        }
        return similarity;
    }

    /**
     * Method computes given book rating
     *
     * @param reviews list of book reviews
     * @return ranking value
     */
    private double computeRating(final List<Review> reviews) {
        double rating = 0;
        for (final Review review : reviews) {
            if (review.getPositive() > 0)
                rating += review.getWeight();
            else
                rating -= review.getWeight();
        }

        return rating;
    }

    /**
     * Method sets stop flag for stopping execution of run method.
     */
    public void setStopFlag() {
        stopFlag = true;
    }

    @Override
    public void run() {
        final int clusterIndex = book.getClusterIndex();
        //getting books list from specific cluster
        final List<Book> booksInCluster = database.getBooksInCluster(clusterIndex, book.getBookID());
        //getting words vector for input book
        final List<Word> words = database.getBookWords(book.getBookID());

        if (stopFlag)
            return;

        if (booksInCluster != null && words != null) {
            final List<Book> similarBooks = new ArrayList<>(booksInCluster.size());
            final List<Double> similarities = new ArrayList<>(booksInCluster.size());

            for (final Book bookInCluster : booksInCluster) {
                if (stopFlag)
                    return;

                final List<Word> bookWords = database.getBookWords(bookInCluster.getBookID());
                if (bookWords != null) {
                    final double similarity = computeSimilarity(words, bookWords);
                    int index = 0;
                    //loop search for suitable index for adding data to the list.
                    for (; index < similarities.size(); index++) {
                        if (similarity > similarities.get(index))
                            break;
                    }
                    //books are placed in list descending order by similarity
                    similarities.add(index, similarity);
                    similarBooks.add(index, bookInCluster);
                }
            }

            final List<Book> bestBooks = new ArrayList<>(BOOKS_SIZE);
            final List<Double> ratings = new ArrayList<>(BOOKS_SIZE);

            //rating 20 most similar books
            for (int i = 0; i < BOOKS_SIZE; i++) {
                if (stopFlag)
                    return;

                final Book book = similarBooks.get(i);
                final List<Review> bookReviews = database.getReviews(book.getBookID());

                //the same principle as in similarity computing
                if (bookReviews != null) {
                    final double rating = computeRating(bookReviews);
                    int index = 0;
                    for (; index < ratings.size(); index++) {
                        if (rating > ratings.get(index))
                            break;
                    }
                    ratings.add(index, rating);
                    bestBooks.add(index, book);
                }
            }

            //setting 10 best books in list view
            if (!stopFlag)
                controller.setSimilarBooks(bestBooks.subList(0, 10));
        }
    }
}
