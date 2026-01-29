package edu.cs102.g04t06;

import edu.cs102.g04t06.view.GameView;
import edu.cs102.g04t06.view.MenuView;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private Scene scene;

    @Override
    public void start(Stage stage) {
        // var javaVersion = SystemInfo.javaVersion();
        // var javafxVersion = SystemInfo.javafxVersion();

        // Set stage Attributes
        stage.setTitle("Splendor");
        // stage.setFullScreen(true);

        // Set stage size
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setMinHeight(primaryScreenBounds.getMaxY() / 2.0);
        stage.setMinWidth(primaryScreenBounds.getMaxX() / 2.0);
        stage.setHeight(primaryScreenBounds.getMaxY() / 2.0);
        stage.setWidth(primaryScreenBounds.getMaxX() / 2.0);

        // Set default scene
        StackPane root = new StackPane();
        this.scene = new Scene(root);

        stage.setScene(scene);
        stage.show();

        showMenu();
    }

    public void showMenu() {
        StackPane newRoot = new MenuView(this).getRoot();
        this.scene.setRoot(newRoot);
    }

    public void showGame() {
        StackPane newRoot = new GameView(this).getRoot();
        this.scene.setRoot(newRoot);
    }

    public static void main(String[] args) {
        launch();
    }

}
