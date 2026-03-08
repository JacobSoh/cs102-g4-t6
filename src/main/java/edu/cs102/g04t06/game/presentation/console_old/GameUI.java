package edu.cs102.g04t06.game.presentation.console_old;

import edu.cs102.g04t06.App;
import edu.cs102.g04t06.game.presentation.console_old.layout.market.ForegroundLayout;
import edu.cs102.g04t06.game.presentation.console_old.template.BaseStack;
import edu.cs102.g04t06.game.presentation.console_old.template.BoundPaneLayer;
import edu.cs102.g04t06.game.presentation.console_old.template.StylingSheet;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Main game view container.
 */
public class GameUI extends BaseStack<StackPane> implements StylingSheet {

    private static final boolean DEBUG_LAYOUT = true;

    // Marketground cutoffs
    private static final double CARD_SECTION_RATIO = 0.80;
    private static final double GEM_SECTION_RATIO = 0.20;

    // Gem size
    private static final double GEM_IMAGE_SIZE = 75;
    private static final double MIN_GEM_IMAGE_SIZE = 50;
    private static final double MAX_GEM_IMAGE_SIZE = 100;

    private final ForegroundLayout foregroundLayout = new ForegroundLayout();

    /**
     * Creates the game UI.
     *
     * @param application the main application for navigation callbacks
     */
    public GameUI(App application) {
        super(new StackPane(), application);
        setupForegroundSections();
        this.root.getChildren().add(foregroundLayout.createForegroundLayout());
    }

    private void setupForegroundSections() {
        HBox wideLayoutBox = foregroundLayout.getWideLayout();
        VBox playerBoardLayer = createPlayersBoardLayer(wideLayoutBox, PLAYER_COL_RATIO, false);
        VBox marketLayer = createMarketLayer(wideLayoutBox, MARKET_COL_RATIO, true);
        foregroundLayout.addWideLayoutChildrens(playerBoardLayer, marketLayer);

        VBox marketRow = createMarketColumn();
        marketRow.prefHeightProperty().bind(Bindings.max(650.0, this.root.heightProperty().multiply(0.70)));
        marketRow.setMaxWidth(Double.MAX_VALUE);

        HBox playersRow = new HBox(12);
        playersRow.setAlignment(Pos.TOP_CENTER);
        VBox leftPlayerRow = createPlayerLayer(playersRow, 0.50, true);
        VBox rightPlayerRow = createPlayerLayer(playersRow, 0.50, true);
        playersRow.getChildren().addAll(leftPlayerRow, rightPlayerRow);

        foregroundLayout.addNarrowLayoutChildrens(marketRow, playersRow);
    }

    private VBox createPlayersBoardLayer(HBox parent, double widthRatio, boolean hGrow) {
        BoundPaneLayer<VBox> playerBoardLayer = new BoundPaneLayer<>(new VBox()).bindWidthRatio(parent, widthRatio);
        if (hGrow) {
            playerBoardLayer.hGrow();
        }
        VBox playerBoard = playerBoardLayer.getRoot();
        playerBoard.prefHeightProperty().bind(parent.heightProperty());
        playerBoard.setMinWidth(0);
        playerBoard.setMaxHeight(Double.MAX_VALUE);
        playerBoard.setFillWidth(true);

        GridPane quadrants = new GridPane();
        quadrants.setHgap(10);
        quadrants.setVgap(10);
        quadrants.setAlignment(Pos.CENTER);
        quadrants.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        quadrants.prefWidthProperty().bind(playerBoard.widthProperty());
        quadrants.prefHeightProperty().bind(playerBoard.heightProperty());

        for (int i = 0; i < 2; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(50);
            col.setHgrow(Priority.ALWAYS);
            col.setHalignment(HPos.CENTER);
            quadrants.getColumnConstraints().add(col);
        }

        for (int i = 0; i < 4; i++) {
            Region quadrant = new Region();
            quadrant.setMinSize(0, 0);
            quadrant.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            applyDebugStyle(quadrant, "rgba(255,128,0,0.12)", "orange");
            GridPane.setHgrow(quadrant, Priority.ALWAYS);
            GridPane.setVgrow(quadrant, Priority.ALWAYS);
            quadrants.add(quadrant, i % 2, i / 2);
        }

        applyDebugStyle(playerBoard, "rgba(255,0,0,0.08)", "red");
        playerBoard.getChildren().setAll(quadrants);
        return playerBoard;
    }

    private VBox createPlayerLayer(HBox parent, double widthRatio, boolean hGrow) {
        BoundPaneLayer<VBox> playerLayer = new BoundPaneLayer<>(new VBox()).bindWidthRatio(parent, widthRatio);
        if (hGrow) {
            playerLayer.hGrow();
        }
        VBox playerCol = playerLayer.getRoot();
        playerCol.prefHeightProperty().bind(parent.heightProperty());
        playerCol.setMinWidth(0);
        playerCol.setMaxHeight(Double.MAX_VALUE);
        playerCol.setFillWidth(true);
        applyDebugStyle(playerCol, "rgba(255,0,0,0.08)", "red");

        return playerCol;
    }

    private VBox createMarketLayer(HBox parent, double widthRatio, boolean hGrow) {
        BoundPaneLayer<VBox> marketLayer = new BoundPaneLayer<>(new VBox()).bindWidthRatio(parent, widthRatio);
        if (hGrow) {
            marketLayer.hGrow();
        }
        VBox marketCol = marketLayer.getRoot();
        marketCol.prefHeightProperty().bind(parent.heightProperty());
        configureMarketColumn(marketCol);
        return marketCol;
    }

    private VBox createMarketColumn() {
        VBox marketCol = new VBox();
        marketCol.setAlignment(Pos.CENTER);
        marketCol.setFillWidth(true);
        marketCol.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        marketCol.setMinWidth(0);
        configureMarketColumn(marketCol);

        return marketCol;
    }

    private void configureMarketColumn(VBox marketColumn) {
        GridPane cardSection = new BoundPaneLayer<>(new GridPane())
                .bindHeightRatio(marketColumn, CARD_SECTION_RATIO)
                .vGrow()
                .getRoot();
        GridPane gemSection = new BoundPaneLayer<>(new GridPane())
                .bindHeightRatio(marketColumn, GEM_SECTION_RATIO)
                .getRoot();

        cardSection.setAlignment(Pos.CENTER);
        gemSection.setAlignment(Pos.CENTER);

        cardSection.prefWidthProperty().bind(marketColumn.widthProperty());
        gemSection.prefWidthProperty().bind(marketColumn.widthProperty());

        cardSection.setMaxWidth(Double.MAX_VALUE);
        gemSection.setMaxWidth(Double.MAX_VALUE);

        createMarketDisplayLayer(cardSection);
        createTokenDisplayLayer(gemSection);

        applyDebugStyle(marketColumn, "rgba(0,255,0,0.04)", "lime");
        applyDebugStyle(cardSection, "rgba(255,255,0,0.10)", "gold");
        applyDebugStyle(gemSection, "rgba(255,0,255,0.10)", "magenta");

        marketColumn.getChildren().addAll(cardSection, gemSection);
    }

    private void createMarketDisplayLayer(GridPane cardSection) {
        GridPane marketGrid = new GridPane();
        marketGrid.setAlignment(Pos.CENTER);
        marketGrid.setHgap(6);
        marketGrid.setVgap(6);
        marketGrid.setPadding(new Insets(4));
        marketGrid.setMinSize(0, 0);
        NumberBinding safeSectionWidth = Bindings.max(0.0, cardSection.widthProperty().subtract(16));
        NumberBinding safeSectionHeight = Bindings.max(0.0, cardSection.heightProperty().subtract(16));
        marketGrid.prefWidthProperty().bind(safeSectionWidth);
        marketGrid.prefHeightProperty().bind(safeSectionHeight);

        // 5 columns total: 1 deck column + 4 visible-card columns.
        marketGrid.getColumnConstraints().clear();
        for (int i = 0; i < 5; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(20);
            col.setHgrow(Priority.ALWAYS);
            col.setHalignment(HPos.CENTER);
            marketGrid.getColumnConstraints().add(col);
        }

        // Responsive sizes based on available market width/height.
        NumberBinding cellWidth = marketGrid.widthProperty()
                .subtract(marketGrid.getHgap() * 4)
                .divide(5);
        NumberBinding widthByRowHeight = marketGrid.heightProperty()
                .subtract(marketGrid.getVgap() * 3)
                .divide(4)
                .divide(1.45);
        NumberBinding responsiveCardWidth = Bindings.min(cellWidth.multiply(0.97), widthByRowHeight.multiply(0.97));
        NumberBinding cardWidth = Bindings.max(52.0, Bindings.min(250.0, responsiveCardWidth));
        NumberBinding cardHeight = cardWidth.multiply(1.45);
        NumberBinding nobleWidth = cardWidth.multiply(0.80);
        NumberBinding nobleHeight = cardHeight.multiply(0.80);

        // Top row: 4 nobles (columns 1..4)
        for (int col = 1; col <= 4; col++) {
            String noblePath = "/images/noble/noble_0" + col + ".png";
            ImageView noble = createImageView(noblePath, 86, 130);
            noble.fitWidthProperty().bind(nobleWidth);
            noble.fitHeightProperty().bind(nobleHeight);
            marketGrid.add(noble, col, 0);
            GridPane.setHalignment(noble, HPos.CENTER);
        }

        // Left column: hidden decks for level III, II, I (rows 1..3)
        String[] deckBacks = {
            "/images/card/dev_III_dos.png",
            "/images/card/dev_II_dos.png",
            "/images/card/dev_I_dos.png"
        };
        for (int row = 1; row <= 3; row++) {
            ImageView deck = createImageView(deckBacks[row - 1], 118, 170);
            deck.fitWidthProperty().bind(cardWidth);
            deck.fitHeightProperty().bind(cardHeight);
            marketGrid.add(deck, 0, row);
            GridPane.setHalignment(deck, HPos.CENTER);
        }

        // Visible market cards: 4 per row for level III, II, I (rows 1..3, columns 1..4)
        String[] prefixes = {"dev_III_", "dev_II_", "dev_I_"};
        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 4; col++) {
                String cardPath = "/images/card/" + prefixes[row - 1] + "0" + col + ".png";
                ImageView card = createImageView(cardPath, 118, 170);
                card.fitWidthProperty().bind(cardWidth);
                card.fitHeightProperty().bind(cardHeight);
                marketGrid.add(card, col, row);
                GridPane.setHalignment(card, HPos.CENTER);
            }
        }

        cardSection.getChildren().setAll(marketGrid);

    }

    private void createTokenDisplayLayer(GridPane gemSection) {
        GemColor[] colors = GemColor.values();
        int count = colors.length;
        double widthSpreadPercentage = 100.0 / count;

        gemSection.getColumnConstraints().clear();
        for (int i = 0; i < count; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(widthSpreadPercentage);
            col.setHgrow(Priority.ALWAYS);
            col.setHalignment(HPos.CENTER);
            gemSection.getColumnConstraints().add(col);
        }

        for (int i = 0; i < count; i++) {
            GemColor color = colors[i];
            String filePath = "/images/gem/gem_" + color.toString().toLowerCase() + ".png";
            ImageView token = createImageView(filePath, GEM_IMAGE_SIZE, GEM_IMAGE_SIZE);

            token.setPreserveRatio(true);
            NumberBinding responsiveSize = Bindings.min(
                    gemSection.widthProperty().divide(count).multiply(0.65),
                    gemSection.heightProperty().multiply(0.80)
            );
            NumberBinding clampedSize = Bindings.max(
                    MIN_GEM_IMAGE_SIZE,
                    Bindings.min(MAX_GEM_IMAGE_SIZE, responsiveSize)
            );
            token.fitWidthProperty().bind(clampedSize);
            token.fitHeightProperty().bind(clampedSize);
            GridPane.setHalignment(token, HPos.CENTER);
            gemSection.add(token, i, 0);
        }
    }

    private void applyDebugStyle(Region node, String fillColor, String borderColor) {
        if (!DEBUG_LAYOUT) {
            return;
        }
        node.setStyle(
                "-fx-background-color: " + fillColor + ";"
                + "-fx-border-color: " + borderColor + ";"
                + "-fx-border-width: 2;"
        );
    }
}
