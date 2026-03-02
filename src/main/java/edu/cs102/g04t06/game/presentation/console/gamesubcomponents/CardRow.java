package edu.cs102.g04t06.game.presentation.console.gamesubcomponents;

import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class CardRow extends CustomParent {
    public CardRow(VBox container, Double heightPercentage, boolean vGrow) {
        super(new TilePane(), container, heightPercentage, false);
        if (vGrow) super.setIsVGrow();
    }

    @Override
    public TilePane getRoot() {
        if (!(this.root instanceof TilePane)) return null;
        return (TilePane) this.root;
    }
    
}