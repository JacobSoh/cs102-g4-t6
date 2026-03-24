package edu.cs102.g04t06;

import edu.cs102.g04t06.game.presentation.console.ConsoleUI;

/**
 * Console application entry point for the Splendor game.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class App {
    private final ConsoleUI consoleUI;

    /**
     * Creates the application instance.
     */
    public App() {
        this.consoleUI = new ConsoleUI();
    }

    /**
     * Starts the console onboarding flow.
     */
    public void run() {
        showOnBoarding();
    }

    /**
     * Renders the onboarding screen in the console.
     */
    public void showOnBoarding() {
        this.consoleUI.showLoadScreen();
    }

    /**
     * Renders the game screen in the console.
     */
    public void showGame() {
        this.consoleUI.showGame();
    }

    public void showLobby(boolean modeOfPlay) {
        this.consoleUI.showLobby(modeOfPlay);
    }

    /**
     * Application entry point for Java launchers.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        new App().run();
    }

}
