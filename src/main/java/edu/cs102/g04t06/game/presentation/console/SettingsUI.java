package edu.cs102.g04t06.game.presentation.console;

import edu.cs102.g04t06.App;
import edu.cs102.g04t06.game.presentation.console.layout.BaseStack;
import javafx.scene.control.Label;

/**
 * command to run: mvn clean javafx:run
 */

/**
 * Main menu view for the application.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class SettingsUI extends BaseStack {

    /**
     * Creates the menu UI and wires the start action.
     *
     * @param application the main application for navigation callbacks
     */
    public SettingsUI(App application, String modeOfPlay) {
        Label label = new Label(modeOfPlay);
        this.root.getChildren().add(label);
    }
}
