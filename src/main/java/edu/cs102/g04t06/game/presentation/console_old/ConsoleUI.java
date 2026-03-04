package edu.cs102.g04t06.game.presentation.console_old;

import edu.cs102.g04t06.App;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class ConsoleUI {
    public static void showOnBoarding(Scene scene, App application) {
        StackPane newRoot = new OnBoardingUI(application).getRoot();
        scene.setRoot(newRoot);
    }

    public static void showLobby(Scene scene, App application, boolean modeOfPlay) {
        StackPane newRoot = new LobbyUI(application, modeOfPlay).getRoot();
        scene.setRoot(newRoot);
    }

    public static void showGame(Scene scene, App application) {
        StackPane newRoot = new GameUI(application).getRoot();
        scene.setRoot(newRoot);
    }
}
