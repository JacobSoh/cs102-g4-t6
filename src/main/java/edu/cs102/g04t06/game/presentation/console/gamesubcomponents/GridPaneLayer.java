package edu.cs102.g04t06.game.presentation.console.gamesubcomponents;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GridPaneLayer extends CustomParent {
    public GridPaneLayer(VBox container, Double heightPercentage, boolean vGrow) {
        super(new GridPane(), container, heightPercentage, false);
        if (vGrow) super.setIsVGrow();
    }

    @Override
    public GridPane getRoot() {
        if (!(this.root instanceof GridPane)) return null;
        return (GridPane) this.root;
    }
    
}