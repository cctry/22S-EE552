package edu.stevens.spreadSheet;

import edu.stevens.spreadSheet.model.POIWorkbookFactory;
import edu.stevens.spreadSheet.view.TableController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private TableController tableController;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("SpreedSheet");
        stage.setHeight(600);
        stage.setWidth(800);
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main.fxml"));
        Parent root = fxmlLoader.load();
        tableController = fxmlLoader.getController();
        var workbook = POIWorkbookFactory.emptyWorkbook(32,16);
        tableController.setWorkbook(workbook);
        scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
