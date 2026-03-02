package edu.cs102.g04t06.game.presentation.console;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import edu.cs102.g04t06.App;

public class ConsoleUI {
    public static void showOnBoarding(Scene scene, App application) {
        StackPane newRoot = new OnBoardingUI(application).getRoot();
        scene.setRoot(newRoot);
    }

    public static void showOnSettings(Scene scene, App application) {
        StackPane newRoot = new SettingsUI(application).getRoot();
        scene.setRoot(newRoot);
    };
}
