package edu.cs102.g04t06;

import edu.cs102.g04t06.game.presentation.console.ConsoleUI;
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

        stage.setScene(this.scene);
        stage.show();

        showGame();
    }

    /**
     * Renders the onboarding screen in the current scene.
     */
    public void showOnBoarding() {
        ConsoleUI.showOnBoarding(this.scene, this);
    }

    /**
     * Renders the settings screen in the current scene.
     */
    public void showGame() {
        ConsoleUI.showGame(this.scene, this);
    }

    public void showLobby(boolean modeOfPlay) {
        ConsoleUI.showLobby(this.scene, this, modeOfPlay);
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
