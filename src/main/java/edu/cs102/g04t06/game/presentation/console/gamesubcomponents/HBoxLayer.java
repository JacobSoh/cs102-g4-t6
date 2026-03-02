package edu.cs102.g04t06.game.presentation.console.gamesubcomponents;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HBoxLayer extends CustomParent {
    public HBoxLayer(VBox container, Double heightPercentage, boolean vGrow) {
        super(new HBox(), container, heightPercentage, false);
        if (vGrow) super.setIsVGrow();
    }

    @Override
    public HBox getRoot() {
        if (!(this.root instanceof HBox)) return null;
        return (HBox) this.root;
    }
    
}