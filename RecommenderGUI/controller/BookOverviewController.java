package recommender.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import recommender.model.Book;

/**
 * Class for displaying content of given book.
 */
public class BookOverviewController {
    @FXML private Label bookTitle;
    @FXML private Label bookAsin;
    @FXML private Label bookDescription;

    /**
     * Function that set book params.
     *
     * @param book book to display
     */
    void initialize(final Book book) {
        bookTitle.setText(book.getTitle());
        bookAsin.setText(book.getAsin());
        bookDescription.setText(book.getDescription());
    }
}
