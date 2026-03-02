package edu.cs102.g04t06.game.presentation.console;

import edu.cs102.g04t06.App;
import edu.cs102.g04t06.game.presentation.console.layout.BaseStack;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * Main menu view for the application. Part of the {@code edu.cs102.g04t06}
 * module.
 */
public class GameUI extends BaseStack {

    private static final String BG_IMAGE_URL = "/images/gameImg.jpg";
    private static final double SIDE_COLUMN_PERCENT = 25;
    private static final double CENTER_COLUMN_PERCENT = 50;
    private static final double CARD_SECTION_PERCENT = 75;
    private static final double GEM_SECTION_PERCENT = 15;
    private static final double ACTION_SECTION_PERCENT = 10;
    private static final double CARD_WIDTH = 120;
    private static final double CARD_HEIGHT = 170;
    private static final double MARKET_GAP = 16;
    private static final Insets MARKET_PADDING = new Insets(18, 0, 12, 0);

    /**
     * Creates the menu UI and wires the start action.
     *
     * @param application the main application for navigation callbacks
     */
    public GameUI(App application) {
        super(BG_IMAGE_URL, application);
        GridPane mainLayout = createMainlayout();
        GridPane marketLayout = createMarketLayout();
        GridPane cardLayout = createCardLayout();
        GridPane gemLayout = createGemLayout();
        Label marketActionLayout = createMarketActionLayout();

        marketLayout.add(cardLayout, 0, 0);
        marketLayout.add(gemLayout, 0, 1);
        marketLayout.add(marketActionLayout, 0, 2);
        GridPane.setValignment(cardLayout, VPos.TOP);
        GridPane.setValignment(gemLayout, VPos.CENTER);
        GridPane.setValignment(marketActionLayout, VPos.CENTER);

        mainLayout.add(marketLayout, 1, 0);

        this.root.getChildren().add(mainLayout);
    }

    private GridPane createMainlayout() {
        GridPane gridPane = new GridPane();
        gridPane.prefWidthProperty().bind(this.root.widthProperty());
        gridPane.prefHeightProperty().bind(this.root.heightProperty());

        // Left column (25%)
        ColumnConstraints col25 = new ColumnConstraints();
        col25.setPercentWidth(SIDE_COLUMN_PERCENT);

        // Center column (50%)
        ColumnConstraints col50 = new ColumnConstraints();
        col50.setPercentWidth(CENTER_COLUMN_PERCENT);
        col50.setFillWidth(true);

        // Right column (25%)
        ColumnConstraints col25Right = new ColumnConstraints();
        col25Right.setPercentWidth(SIDE_COLUMN_PERCENT);

        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);

        gridPane.getRowConstraints().add(row);
        gridPane.getColumnConstraints().addAll(col25, col50, col25Right);

        return gridPane;
    }

    private GridPane createMarketLayout() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setPadding(MARKET_PADDING);
        gridPane.setHgap(MARKET_GAP);
        gridPane.setVgap(MARKET_GAP);
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);

        // Card section (75%)
        RowConstraints row65 = new RowConstraints();
        row65.setPercentHeight(CARD_SECTION_PERCENT);

        // Gem section (15%)
        RowConstraints row15 = new RowConstraints();
        row15.setPercentHeight(GEM_SECTION_PERCENT);

        // Action section (10%)
        RowConstraints row20 = new RowConstraints();
        row20.setPercentHeight(ACTION_SECTION_PERCENT);

        gridPane.getRowConstraints().addAll(row65, row15, row20);

        return gridPane;
    }

    private GridPane createCardLayout() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setHgap(MARKET_GAP);
        gridPane.setVgap(MARKET_GAP);
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHalignment(HPos.CENTER);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            gridPane.getColumnConstraints().add(col);
        }

        // for (int i = 0; i < 2; i++) {
        //     RowConstraints row = new RowConstraints();
        //     row.setPercentHeight(50);
        //     gridPane.getRowConstraints().add(row);
        // }
        gridPane.add(createCardImage("/images/noble/noble_01.png"), 1, 0);
        gridPane.add(createCardImage("/images/noble/noble_01.png"), 2, 0);
        gridPane.add(createCardImage("/images/noble/noble_01.png"), 3, 0);

        for (int col = 0; col < 4; col++) {
            for (int row = 1; row < 4; row++) {
                gridPane.add(createCardImage("/images/card/dev_I_11.png"), col, row);
            }
        }

        return gridPane;
    }

    private Label createMarketActionLayout() {
        Label label = new Label("In progress");

        return label;
    }

    private GridPane createGemLayout() {
        GridPane gridPane = new GridPane();
        for (int col = 0; col < 6; col++) {
            gridPane.add(createCardImage("/images/gem/jeton_blanc.png"), col, 0);
        }

        return gridPane;
    }

    private ImageView createCardImage(String imagePath) {
        ImageView imageView = new ImageView(getClass().getResource(imagePath).toExternalForm());

        imageView.setFitWidth(CARD_WIDTH);
        imageView.setFitHeight(CARD_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
}
