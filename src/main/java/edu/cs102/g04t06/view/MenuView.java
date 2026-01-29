package edu.cs102.g04t06.view;

import edu.cs102.g04t06.App;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class MenuView extends DefaultView {
    private final String bgImgURL = "/images/menuImg.png";

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
