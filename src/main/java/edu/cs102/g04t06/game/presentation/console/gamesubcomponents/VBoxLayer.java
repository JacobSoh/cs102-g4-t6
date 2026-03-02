package edu.cs102.g04t06.game.presentation.console.gamesubcomponents;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VBoxLayer extends CustomParent {
    public VBoxLayer(HBox container, Double columnPercentage, boolean isHGrow) {
        super(new VBox(), container, columnPercentage, true);
        if (isHGrow) super.setIsHGrow();
    }

    @Override
    public VBox getRoot() {
        if (!(this.root instanceof VBox)) return null;
        return (VBox) this.root;
    }
}