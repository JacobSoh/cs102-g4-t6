package edu.cs102.g04t06.view;

import javafx.scene.layout.StackPane;

public class DefaultView {
    protected StackPane root;

    public DefaultView() {
        this.root = new StackPane();
    }
    
    public StackPane getRoot() {
        return this.root;
    }
}
