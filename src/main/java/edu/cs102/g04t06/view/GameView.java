package edu.cs102.g04t06.view;

import edu.cs102.g04t06.App;
import javafx.scene.control.Label;

/**
 * View shown after the game starts.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class GameView extends DefaultView {
    /**
     * Creates the game view UI.
     *
     * @param application the main application for callbacks
     */
    public GameView(App application) {
        Label label = new Label("Game Started");
        this.root.getChildren().add(label);
    }
}
