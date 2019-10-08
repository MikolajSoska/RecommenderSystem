package recommender;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import recommender.controller.MainController;

/**
 * Main class of the program. Responsible for displaying the graphical interface.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/mainScreen.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        primaryStage.setOnCloseRequest(event -> controller.shutdown());
        primaryStage.setTitle("Recommender");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        resizeStage(primaryStage);
        primaryStage.show();
    }

    /**
     * Main function.
     *
     * @param args args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Function changes the size of the application window based on the computer screen.
     *
     * @param stage stage to resize
     */
    private void resizeStage(final Stage stage) {
        final Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        stage.setHeight(screen.getHeight() * 0.8);
        stage.setWidth(screen.getWidth() * 0.7);
    }
}
