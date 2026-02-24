package edu.cs102.g04t06.game.presentation.console.old;

import javafx.scene.layout.StackPane;

/**
 * Base JavaFX view that provides a root {@link StackPane}.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class BaseStack {
    /**
     * Root container for this view.
     */
    protected StackPane root;

    /**
     * Constructs a view with an empty {@link StackPane} root.
     */
    public BaseStack() {
        this.root = new StackPane();
    }
    
    /**
     * Returns the root node for this view.
     *
     * @return the root {@link StackPane}
     */
    public StackPane getRoot() {
        return this.root;
    }
}
