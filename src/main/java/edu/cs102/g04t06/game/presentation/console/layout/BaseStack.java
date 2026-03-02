package edu.cs102.g04t06.game.presentation.console.layout;

import edu.cs102.g04t06.App;
import javafx.scene.image.ImageView;
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
    protected App application;

    /**
     * Constructs a view with an empty {@link StackPane} root.
     */
    public BaseStack() {
        this.root = new StackPane();
    }

    public BaseStack(String bgImageUrl) {
        this();
        this.root.getChildren().add(0, createBackdrop(bgImageUrl));
    }

    public BaseStack(String bgImageUrl, App application) {
        this(bgImageUrl);
        this.application = application;
    }

    /**
     * Creates a background image that scales with the given root container.
     *
     * @param bgImageUrl classpath image path (for example {@code /images/menuImg.jpeg})
     * @param root the container whose width and height are used for binding
     * @return configured full-size background image view
     */
    private ImageView createBackdrop(String bgImageUrl) {
        ImageView imageView = new ImageView(getClass().getResource(bgImageUrl).toExternalForm());

        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(root.widthProperty());
        imageView.fitHeightProperty().bind(root.heightProperty());
        return imageView;
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
