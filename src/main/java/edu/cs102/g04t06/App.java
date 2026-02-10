package edu.cs102.g04t06;

import edu.cs102.g04t06.game.presentation.console.old.Game;
import edu.cs102.g04t06.game.presentation.console.old.Menu;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for the Splendor game.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class App extends Application {

    private Scene scene;

    /**
     * Creates the application instance.
     */
    public App() {
        // Default constructor required by JavaFX Application lifecycle.
    }

    /**
     * Initializes the primary stage and shows the main menu.
     *
     * @param stage the primary JavaFX stage
     */
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

    /**
     * Replaces the scene root with the menu view.
     */
    public void showMenu() {
        StackPane newRoot = new Menu(this).getRoot();
        this.scene.setRoot(newRoot);
    }

    /**
     * Replaces the scene root with the game view.
     */
    public void showGame() {
        StackPane newRoot = new Game(this).getRoot();
        this.scene.setRoot(newRoot);
    }

    /**
     * Application entry point for Java launchers.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }

}
