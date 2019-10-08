package recommender.controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import recommender.model.Book;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main controller for reccomender program.
 */
public class MainController implements Initializable {
    @FXML private BorderPane mainPane;
    @FXML private VBox leftColumn;

    @FXML private ListView<Book> booksList;
    @FXML private ListView<Book> similarBooks;

    @FXML private Label bookTitle;
    @FXML private Label bookAsin;
    @FXML private Label bookDescription;
    @FXML private Label searchingLabel;

    @FXML private TextField searchField;

    private DatabaseConnector database;
    private ExecutorService executorService;
    private NearestNeighbourSearch search;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        executorService = Executors.newFixedThreadPool(20);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchBook());

        //after selecting book, program start searching for nearest neighbours
        booksList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Book>) c -> {
            final Book book = booksList.getSelectionModel().getSelectedItem();
            if (book != null) {
                if (search != null)
                    search.setStopFlag(); //stop currently running search

                bookTitle.setText(book.getTitle());
                bookAsin.setText(book.getAsin());
                bookDescription.setText(book.getDescription());
                searchingLabel.setPrefHeight(20);
                similarBooks.setVisible(false);

                search = new NearestNeighbourSearch(book, this, database);
                search.start();
            }
        });

        Platform.runLater(this::resize);
        try {
            database = new DatabaseConnector();

            final List<Book> books = database.getBooks("");
            if (books != null) {
                booksList.getItems().setAll(books);
                booksList.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            System.out.println("SQL EXCEPTION: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("CLASS NOT FOUND EXCEPTION: " + e.getMessage());
        }
    }

    /**
     * Method sets list of similar books in list view.
     *
     * @param books list of books
     */
    public void setSimilarBooks(final List<Book> books) {
        Platform.runLater(() -> {
            searchingLabel.setPrefHeight(0);
            similarBooks.setVisible(true);
            similarBooks.getItems().setAll(books);
        });

    }

    /**
     * Method stops all threads.
     */
    public void shutdown() {
        executorService.shutdownNow();
        Platform.exit();
    }

    /**
     * Method for window resizing.
     */
    private void resize() {
        final double width = mainPane.getWidth() * 0.25;
        leftColumn.setMaxWidth(width);
        leftColumn.setMinWidth(width);
        leftColumn.setPrefWidth(width);
    }

    /**
     * Method starts new search thread after changing input in search text field.
     */
    private void searchBook() {
        executorService.execute(() -> {
            final List<Book> books = database.getBooks(searchField.getText());
            if (books != null)
                Platform.runLater(() -> booksList.getItems().setAll(books));
        });
    }

    /**
     * Launches after clicking in similar books list view.
     * Method opens new window with information about clicked book.
     *
     * @param event mouse event
     */
    @FXML
    private void openBookOverview(final MouseEvent event) {
        if (event.getClickCount() == 2) { //double click
            final Book book = similarBooks.getSelectionModel().getSelectedItem();
            if (book != null) {
                try {
                    final FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/bookOverview.fxml"));
                    final Parent parent = loader.load();
                    final BookOverviewController controller = loader.getController();
                    final Stage stage = new Stage();

                    controller.initialize(book);
                    stage.setTitle("Overview of " + book.getAsin());
                    stage.setResizable(false);
                    stage.setScene(new Scene(parent));
                    stage.show();
                } catch (IOException e) {
                    System.out.println("IO EXCEPTION: " + e.getMessage());
                }
            }
        }
    }
}
