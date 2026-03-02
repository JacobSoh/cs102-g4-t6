package edu.cs102.g04t06.game.presentation.console;

import edu.cs102.g04t06.App;
import edu.cs102.g04t06.game.presentation.console.layout.BaseStack;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Main menu view for the application.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class OnBoardingUI extends BaseStack {
    private final String bgImgURL = "/images/menuImg.png";

    /**
     * Creates the menu UI and wires the start action.
     *
     * @param application the main application for navigation callbacks
     */
    public OnBoardingUI(App application) {
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
        StackPane.setAlignment(startBtn, Pos.BOTTOM_CENTER);
        startBtn.translateYProperty().bind(root.heightProperty().multiply(-0.20));

        startBtn.setOnAction(e -> {
            System.out.println("starting");
        });

    }
}
