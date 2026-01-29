package edu.cs102.g04t06.view;

import edu.cs102.g04t06.App;
import javafx.scene.control.Label;

public class GameView extends DefaultView {
    public GameView(App application) {
        Label label = new Label("Game Started");
        this.root.getChildren().add(label);
    }
}
