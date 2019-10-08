package recommender.controller;

import com.mysql.cj.jdbc.exceptions.MySQLStatementCancelledException;
import recommender.model.Book;
import recommender.model.Review;
import recommender.model.Word;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used for connecting to database.
 */
public class DatabaseConnector {
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/recommender";

    private final Statement statement;

    private boolean canExecute;
    private boolean isCancelable;

    /**
     * Contructor for class.
     *
     * @throws ClassNotFoundException if JVM can't find drivers class
     * @throws SQLException           if connecting to database has failed
     */
    DatabaseConnector() throws ClassNotFoundException, SQLException {
        Class.forName(DB_DRIVER);

        final Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        statement = connection.createStatement();
        canExecute = true;
        isCancelable = false;
    }

    /**
     * Function returns all books in database.
     *
     * @param searchName string value which is used to narrow returned list
     * @return list of books or null when exception occurs
     */
    public List<Book> getBooks(final String searchName) {
        try {
            if (isCancelable) //function can cancel other 'getBooks' function to speed up searching process
                statement.cancel();
            else
                waitForExecution();

            isCancelable = true;
            canExecute = false;

            final String query = String.format("SELECT * FROM books WHERE title LIKE '%s%%'", searchName);
            final List<Book> books = executeAndReturnBookList(query);
            isCancelable = false;
            canExecute = true;

            return books;
        } catch (MySQLStatementCancelledException ignored) {
            canExecute = true;
            return null;
        } catch (SQLException e) {
            System.out.println("SQL EXCEPTION: " + e.getMessage());
            isCancelable = false;
            canExecute = true;
            return null;
        }
    }

    /**
     * Function returns all book in cluster.
     *
     * @param clusterIndex index of the cluster
     * @param bookID       ID number of given book
     * @return list of books or null when exception occurs
     */
    public List<Book> getBooksInCluster(final int clusterIndex, final int bookID) {
        waitForExecution();
        canExecute = false;
        try {
            final String query = String.format("SELECT * FROM books WHERE cluster = %d AND id <> %d", clusterIndex, bookID);
            final List<Book> books = executeAndReturnBookList(query);
            canExecute = true;

            return books;
        } catch (SQLException e) {
            System.out.println("SQL EXCEPTION: " + e.getMessage());
            canExecute = true;
            return null;
        }
    }

    /**
     * Function return list with every word in specific book.
     *
     * @param bookID ID number of given book
     * @return list of words or null when exception occurs
     */
    public List<Word> getBookWords(final int bookID) {
        waitForExecution();
        canExecute = false;
        try {
            final String query = String.format("SELECT * FROM word_vectors WHERE book_id = %d ORDER BY word_id", bookID);
            final ResultSet result = statement.executeQuery(query);
            final List<Word> wordsList = new ArrayList<>();

            while (result.next()) {
                final int wordIndex = result.getInt("word_id");
                final double wordValue = result.getDouble("word_value");

                wordsList.add(new Word(wordIndex, wordValue));
            }
            result.close();
            canExecute = true;

            return wordsList;
        } catch (SQLException e) {
            System.out.println("SQL EXCEPTION: " + e.getMessage());
            canExecute = true;
            return null;
        }
    }

    /**
     * Function returns all reviews for given book.
     *
     * @param bookID ID number of given book
     * @return list of reviews or null when exception occurs
     */
    public List<Review> getReviews(final int bookID) {
        waitForExecution();
        canExecute = false;
        try {
            final String query = String.format("SELECT positive, weight FROM reviews WHERE book_id = %d", bookID);
            final ResultSet result = statement.executeQuery(query);
            final List<Review> reviewsList = new ArrayList<>();

            while (result.next()) {
                final int positive = result.getInt("positive");
                final double weight = result.getDouble("weight");

                reviewsList.add(new Review(positive, weight));
            }
            result.close();
            canExecute = true;

            return reviewsList;
        } catch (SQLException e) {
            System.out.println("SQL EXCEPTION: " + e.getMessage());
            canExecute = true;
            return null;
        }
    }

    /**
     * Function exetutes given query and return list of books as result
     *
     * @param query query to execute
     * @return list of book
     * @throws SQLException if failed during execution of query
     */
    private List<Book> executeAndReturnBookList(final String query) throws SQLException {
        final ResultSet result = statement.executeQuery(query);
        final List<Book> bookList = new ArrayList<>();
        while (result.next()) {
            final int bookID = result.getInt("id");
            final String asin = result.getString("asin");
            final String title = result.getString("title");
            final String description = result.getString("description");
            final int clusterIndex = result.getInt("cluster");

            bookList.add(new Book(bookID, title, description, asin, clusterIndex));
        }
        result.close();

        return bookList;
    }

    /**
     * Function waits until other query is finished to prevent collision.
     */
    private void waitForExecution() {
        try {
            while (!canExecute)
                Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
    }
}
