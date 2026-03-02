package edu.cs102.g04t06.game.presentation.console;

import edu.cs102.g04t06.App;
import edu.cs102.g04t06.game.presentation.console.gamesubcomponents.*;
import edu.cs102.g04t06.game.presentation.console.layout.BaseStack;
import edu.cs102.g04t06.game.presentation.console.layout.StylingSheet;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * Main game view container.
 */
public class GameUI extends BaseStack implements StylingSheet {

    private static final String BG_IMAGE_URL = "/images/gameImg.jpg";
    private static final boolean DEBUG_LAYOUT = true;

    // Foreground cutoffs
    private static final double PLAYER_COL_RATIO = 0.20;
    private static final double MARKET_COL_RATIO = 0.60;

    // Marketground cutoffs
    private static final double CARD_SECTION_RATIO = 0.75;
    private static final double GEM_SECTION_RATIO = 0.15;
    private static final double ACTION_SECTION_RATIO = 0.10;

    /**
     * Creates the game UI.
     *
     * @param application the main application for navigation callbacks
     */
    public GameUI(App application) {
        super(BG_IMAGE_URL, application);
        this.root.getChildren().add(createForegroundLayout());
    }

    private HBox createForegroundLayout() {
        HBox foreground = new HBox();

        VBox leftPlayerCol = new VBoxLayer(foreground, PLAYER_COL_RATIO, false).getRoot();
        VBox marketCol = new VBoxLayer(foreground, MARKET_COL_RATIO, true).getRoot();
        VBox rightPlayerCol = new VBoxLayer(foreground, PLAYER_COL_RATIO, false).getRoot();

        applyDebugStyle(leftPlayerCol, "rgba(255,0,0,0.08)", "red");
        applyDebugStyle(rightPlayerCol, "rgba(0,0,255,0.08)", "dodgerblue");

        marketCol.getChildren().add(createMarketLayout());

        foreground.getChildren().addAll(leftPlayerCol, marketCol, rightPlayerCol);
        return foreground;
    }

    private VBox createMarketLayout() {
        VBox marketColumn = new VBox();

        TilePane cardSection = new TilePaneLayer(marketColumn, CARD_SECTION_RATIO, true).getRoot();
        HBox gemSection = new HBoxLayer(marketColumn, GEM_SECTION_RATIO, false).getRoot();
        HBox actionSection = new HBoxLayer(marketColumn, ACTION_SECTION_RATIO, false).getRoot();

        applyDebugStyle(marketColumn, "rgba(0,255,0,0.04)", "lime");
        applyDebugStyle(cardSection, "rgba(255,255,0,0.10)", "gold");
        applyDebugStyle(gemSection, "rgba(255,0,255,0.10)", "magenta");
        applyDebugStyle(actionSection, "rgba(0,255,255,0.10)", "cyan");

        marketColumn.getChildren().addAll(cardSection, gemSection, actionSection);
        return marketColumn;
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

    private Pane createTokenDisplayLayer() {
        HBox tokenLayer = new HBox();
        for (GemColor color : GemColor.values()) {
            
        }
        return tokenLayer;
    }
}
