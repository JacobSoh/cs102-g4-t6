package edu.cs102.g04t06.game.presentation.console_old.template;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class BoundPaneLayer<T extends Pane> extends BaseStack<T> {

    public BoundPaneLayer(T root) {
        super(root);
    }

    public BoundPaneLayer<T> bindWidthRatio(Region container, double widthRatio) {
        this.root.prefWidthProperty().bind(container.widthProperty().multiply(widthRatio));
        this.root.setMinWidth(0);
        return this;
    }

    public BoundPaneLayer<T> bindHeightRatio(Region container, double heightRatio) {
        this.root.prefHeightProperty().bind(container.heightProperty().multiply(heightRatio));
        this.root.setMinHeight(0);
        return this;
    }

    public BoundPaneLayer<T> hGrow() {
        HBox.setHgrow(this.root, Priority.ALWAYS);
        return this;
    }

    public BoundPaneLayer<T> vGrow() {
        VBox.setVgrow(this.root, Priority.ALWAYS);
        return this;
    }
}
