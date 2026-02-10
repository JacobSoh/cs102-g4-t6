package edu.cs102.g04t06.game.presentation.console.old;

import edu.cs102.g04t06.App;
import javafx.scene.control.Label;

/**
 * View shown after the game starts.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class Game extends BaseStack {
    /**
     * Creates the game view UI.
     *
     * @param application the main application for callbacks
     */
    public Game(App application) {
        Label label = new Label("Game Started");
        this.root.getChildren().add(label);
    }
}