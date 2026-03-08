package edu.cs102.g04t06.game.presentation.console_old.template;

import java.util.Collection;

import edu.cs102.g04t06.App;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Base JavaFX view that provides a root {@link StackPane}.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class BaseStack<T extends Pane> {
    protected final T root;
    protected App application;
    private final ScrollPane scrollPane = new ScrollPane();

    public BaseStack(T root) {
        this.root = root;
    }

    public BaseStack(T root, App application) {
        this(root);
        this.application = application;
    }

    public void setBasePadding() {
        this.root.setPadding(new Insets(20, 15, 20, 15));
    }

    public void setBaseBackground(String bgImageUrl) {
        ImageView baseBackground = new ImageView(getClass().getResource(bgImageUrl).toExternalForm());
        baseBackground.setPreserveRatio(false);
        baseBackground.setSmooth(true);
        baseBackground.fitWidthProperty().bind(root.widthProperty());
        baseBackground.fitHeightProperty().bind(root.heightProperty());

        this.root.getChildren().add(0, baseBackground);
    }

    public ImageView createImageView(String bgImageUrl, double width, double height) {
        ImageView imageView = new ImageView(getClass().getResource(bgImageUrl).toExternalForm());

        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        return imageView;
    }

    public void addChildrens(Node... panels) {
        this.root.getChildren().addAll(panels);
    }

    public T getRoot() {
        return this.root;
    }
}
