package edu.stevens.spreadSheet;

import edu.stevens.spreadSheet.view.TableController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private TableController tableController;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("SpreedSheet");
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main.fxml"));
        Parent root = fxmlLoader.load();
        this.tableController = fxmlLoader.getController();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);

        stage.show();
    }

    public void setTableController(TableController controller) {
        this.tableController = controller;
    }

    public static void main(String[] args) {
        launch();
    }
}
