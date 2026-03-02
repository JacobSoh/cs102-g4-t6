package edu.cs102.g04t06.game.presentation.console.gamesubcomponents;
import java.util.Collection;

import javafx.scene.Node;
import javafx.scene.layout.*;

public class CustomParent {
    protected final Pane root;

    public CustomParent(Pane root, Pane container, Double columnPercentage, boolean isWidth) {
        this.root = root;
        if (isWidth) {
            this.root.prefWidthProperty().bind(container.widthProperty().multiply(columnPercentage));
            this.root.setMinWidth(0);
        } else {
            this.root.prefHeightProperty().bind(container.heightProperty().multiply(columnPercentage));
            this.root.setMinHeight(0);
        }
    }

    public void setIsHGrow() {
        HBox.setHgrow(this.root, Priority.ALWAYS);
    }

    public void setIsVGrow() {
        VBox.setVgrow(this.root, Priority.ALWAYS);
    }

    public void addColumns(Collection<? extends Node> panels) {
        this.root.getChildren().addAll(panels);
    }

    public Pane getRoot() {
        return this.root;
    }
    
}