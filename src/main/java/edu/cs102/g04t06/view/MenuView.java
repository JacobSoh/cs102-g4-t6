package edu.cs102.g04t06.view;

import edu.cs102.g04t06.App;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 * Main menu view for the application.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class MenuView extends DefaultView {
    private final String bgImgURL = "/images/menuImg.png";

    /**
     * Creates the menu UI and wires the start action.
     *
     * @param application the main application for navigation callbacks
     */
    public MenuView(App application) {
        // Calling image
        ImageView bg = new ImageView(
            getClass().getResource(this.bgImgURL).toExternalForm()
        );

        // Setting image resolutions and behavior
        bg.setPreserveRatio(false);
        bg.setSmooth(true);
        bg.fitWidthProperty().bind(this.root.widthProperty());
        bg.fitHeightProperty().bind(this.root.heightProperty());

        this.root.getChildren().add(0,bg);

        Button startBtn = new Button("Start Game");
        root.getChildren().add(startBtn);

        startBtn.setOnAction(e -> {
            application.showGame();
        });

    }
}
