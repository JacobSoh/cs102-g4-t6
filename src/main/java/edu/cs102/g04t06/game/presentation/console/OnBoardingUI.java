package edu.cs102.g04t06.game.presentation.console;

import edu.cs102.g04t06.App;
import edu.cs102.g04t06.game.presentation.console.layout.BaseStack;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.PopupWindow;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Onboarding view shown when the application starts.
 *
 * <p>This view renders a full-screen background image and an action row
 * containing mode selection plus a start button.</p>
 */
public class OnBoardingUI extends BaseStack {

    private static final String BG_IMAGE_URL = "/images/menuImg.jpeg";
    private static final double BUTTON_GROUP_BOTTOM_OFFSET_RATIO = -0.20;
    private static final String BUTTON_FONT_FAMILY = "'Georgia'";
    private static final double BUTTON_GLOW_OPACITY = 0.35;
    private static final double BUTTON_GLOW_RADIUS = 16;
    private static final double BUTTON_HEIGHT = 38;
    private static final double MODE_BUTTON_WIDTH = 140;
    private static final double START_BUTTON_WIDTH = 140;
    private static final String GOLD_TEXT = "#F5E6B3";
    private static final String DARK_TEXT = "#1a0a00";
    private static final String MARK_COLOR_BASE = GOLD_TEXT;
    private static final String MARK_COLOR_HOVER = DARK_TEXT;
    private static final String POPUP_MENU_STYLE =
        "-fx-background-color: linear-gradient(to bottom, rgba(36, 22, 12, 0.97), rgba(15, 9, 5, 0.97));" +
        "-fx-border-color: rgba(242, 210, 122, 0.85);" +
        "-fx-border-width: 1.6px;" +
        "-fx-border-radius: 10px;" +
        "-fx-background-radius: 10px;" +
        "-fx-padding: 2px;" +
        "-fx-background-insets: 0;";
    private static final String TRANSPARENT_ROOT_STYLE =
        "-fx-background-color: transparent;" +
        "-fx-border-color: transparent;";
    private static final String POPUP_STYLESHEET_CSS =
        ".menu-item { -fx-background-color: transparent !important; -fx-padding: 0; }" +
        ".menu-item:focused { -fx-background-color: transparent !important; }" +
        ".menu-item:hover { -fx-background-color: transparent !important; }" +
        ".context-menu { -fx-background-color: transparent; -fx-padding: 0; }";
    private static Path popupStylesheetPath;
    private boolean selectedModeValue;

    /**
     * Creates the onboarding UI and wires navigation actions.
     *
     * @param application the main application for navigation callbacks
     */
    public OnBoardingUI(App application) {
        super(BG_IMAGE_URL, application);
        this.selectedModeValue = false;
        this.root.getChildren().add(createButtonGroup());
    }

    /**
     * Creates the bottom action row with mode selection and start button.
     *
     * @return configured action container
     */
    private GridPane createButtonGroup() {
        GridPane gridPane = new GridPane();

        gridPane.add(createModeOfPlayDropdown(), 0, 0);
        gridPane.add(createStartButton(), 1, 0);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(16);
        gridPane.setVgap(12);
        gridPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane.setAlignment(gridPane, Pos.BOTTOM_CENTER);
        gridPane.translateYProperty().bind(this.root.heightProperty().multiply(BUTTON_GROUP_BOTTOM_OFFSET_RATIO));

        return gridPane;
    }

    /**
     * Creates the start button and binds navigation to settings.
     *
     * @return configured start button
     */
    private Button createStartButton() {
        Button startBtn = new Button("Start");

        startBtn.setPrefHeight(BUTTON_HEIGHT);
        startBtn.setMinHeight(BUTTON_HEIGHT);
        startBtn.setMaxHeight(BUTTON_HEIGHT);

        startBtn.setStyle(luxuryButtonBaseStyle(START_BUTTON_WIDTH, BUTTON_HEIGHT));
        startBtn.setEffect(createGoldGlow());
        startBtn.setCursor(Cursor.HAND);
        startBtn.setFocusTraversable(false);

        startBtn.setOnMouseEntered(e -> startBtn.setStyle(luxuryButtonHoverStyle(START_BUTTON_WIDTH, BUTTON_HEIGHT)));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(luxuryButtonBaseStyle(START_BUTTON_WIDTH, BUTTON_HEIGHT)));
        startBtn.setOnMousePressed(e -> {
            startBtn.setScaleX(0.97);
            startBtn.setScaleY(0.97);
            startBtn.setTranslateY(1);
        });
        startBtn.setOnMouseReleased(e -> {
            startBtn.setScaleX(1.0);
            startBtn.setScaleY(1.0);
            startBtn.setTranslateY(0);
        });

        startBtn.setOnAction(e -> this.application.showLobby(this.selectedModeValue));
        return startBtn;
    }

    private MenuButton createModeOfPlayDropdown() {
        MenuButton menuButton = new MenuButton("  Mode");
        menuButton.setPrefWidth(MODE_BUTTON_WIDTH);
        menuButton.setPrefHeight(BUTTON_HEIGHT);
        menuButton.setMinHeight(BUTTON_HEIGHT);
        menuButton.setMaxHeight(BUTTON_HEIGHT);

        applyModeButtonState(menuButton, false);
        menuButton.setEffect(createGoldGlow());
        menuButton.setCursor(Cursor.HAND);
        menuButton.setFocusTraversable(false);

        menuButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> applyModeButtonLabelStyle(menuButton, false));
            }
        });
        menuButton.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                Platform.runLater(this::styleDropdownPopupWindows);
            }
        });

        menuButton.setOnMouseEntered(e -> {
            applyModeButtonState(menuButton, true);
            applyModeButtonLabelStyle(menuButton, true);
        });
        menuButton.setOnMouseExited(e -> {
            applyModeButtonState(menuButton, false);
            applyModeButtonLabelStyle(menuButton, false);
        });

        menuButton.getItems().addAll(
            createModeMenuItem(menuButton, "Online", true, true, false),
            createModeMenuItem(menuButton, "Offline", false, false, true)
        );
        return menuButton;
    }

    private void applyModeButtonState(MenuButton menuButton, boolean hovered) {
        String base = hovered
            ? luxuryButtonHoverStyle(MODE_BUTTON_WIDTH, BUTTON_HEIGHT)
            : luxuryButtonBaseStyle(MODE_BUTTON_WIDTH, BUTTON_HEIGHT);
        String markColor = hovered ? MARK_COLOR_HOVER : MARK_COLOR_BASE;
        menuButton.setStyle(base + "-fx-mark-color: " + markColor + ";" + "-fx-padding: 4 18 4 18;");
    }

    private void applyModeButtonLabelStyle(MenuButton menuButton, boolean hovered) {
        Node label = menuButton.lookup(".label");
        if (label == null) {
            return;
        }
        String color = hovered ? DARK_TEXT : GOLD_TEXT;
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Georgia'; -fx-font-weight: bold;");
    }

    private void styleDropdownPopupWindows() {
        for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
            if (!(window instanceof PopupWindow popupWindow)) {
                continue;
            }

            window.getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
            popupWindow.getScene().getRoot().setStyle(TRANSPARENT_ROOT_STYLE);

            try {
                window.getScene().getStylesheets().add(ensurePopupStylesheet());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            window.getScene().getRoot().lookupAll("*").forEach(node -> {
                String cls = node.getStyleClass().toString();
                if (cls.contains("context-menu")) {
                    node.setEffect(null);
                    node.setStyle(POPUP_MENU_STYLE);
                } else if (cls.contains("menu-item") || cls.contains("container") || cls.contains("graphic")) {
                    node.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                }
            });
        }
    }

    private String ensurePopupStylesheet() throws Exception {
        if (popupStylesheetPath == null) {
            popupStylesheetPath = Files.createTempFile("splendor-", ".css");
            Files.writeString(popupStylesheetPath, POPUP_STYLESHEET_CSS);
        }
        return popupStylesheetPath.toUri().toString();
    }

    /**
     * Creates a single dropdown item with an associated boolean value.
     *
     * @param menuButton the parent button whose label is updated on selection
     * @param optionName the text shown for this selectable option
     * @param optionValue the internal boolean value to store for this option
     * @return configured menu item with width binding and selection handler
     */
    private CustomMenuItem createModeMenuItem(
        MenuButton menuButton,
        String optionName,
        boolean optionValue,
        boolean isTopRounded,
        boolean isBottomRounded
    ) {
        Label optionLabel = new Label(optionName);
        optionLabel.setAlignment(Pos.CENTER_LEFT);
        optionLabel.setMaxWidth(Double.MAX_VALUE);
        optionLabel.prefWidthProperty().bind(menuButton.widthProperty().subtract(16));
        String rowRadius = isTopRounded
            ? "9px 9px 0px 0px"
            : (isBottomRounded ? "0px 0px 9px 9px" : "0px");
        String borderWidth = isBottomRounded ? "0" : "0 0 0.8px 0";
        optionLabel.setStyle(dropdownRowStyle(rowRadius, borderWidth, false));
        optionLabel.setOnMouseEntered(e -> optionLabel.setStyle(dropdownRowStyle(rowRadius, borderWidth, true)));
        optionLabel.setOnMouseExited(e -> optionLabel.setStyle(dropdownRowStyle(rowRadius, borderWidth, false)));

        optionLabel.setFocusTraversable(false);
        CustomMenuItem optionItem = new CustomMenuItem(optionLabel, true);
        optionItem.setHideOnClick(true);
        optionItem.setUserData(optionValue);
        optionItem.setOnAction(e -> {
            menuButton.setText(optionName);
            this.selectedModeValue = (boolean) optionItem.getUserData();
        });
        return optionItem;
    }

    private String dropdownRowStyle(String rowRadius, String borderWidth, boolean hovered) {
        String textColor = hovered ? DARK_TEXT : GOLD_TEXT;
        String bg = hovered
            ? "linear-gradient(to bottom, rgba(243, 210, 122, 0.98), rgba(173, 124, 35, 0.98))"
            : "linear-gradient(to bottom, rgba(36, 22, 12, 0.92), rgba(15, 9, 5, 0.92))";
        return
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-family: " + BUTTON_FONT_FAMILY + ";" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 18 8 18;" +
            "-fx-background-color: " + bg + ";" +
            "-fx-background-radius: " + rowRadius + ";" +
            "-fx-background-insets: 0;" +
            "-fx-border-color: rgba(242, 210, 122, 0.85);" +
            "-fx-border-width: " + borderWidth + ";" +
            "-fx-border-radius: " + rowRadius + ";";
    }

    private DropShadow createGoldGlow() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#F2D27A", BUTTON_GLOW_OPACITY));
        glow.setRadius(BUTTON_GLOW_RADIUS);
        glow.setSpread(0.08);
        glow.setOffsetY(2);
        return glow;
    }

    private String luxuryButtonBaseStyle(double minWidth, double minHeight) {
        return
            "-fx-min-width: " + minWidth + "px;" +
            "-fx-min-height: " + minHeight + "px;" +
            "-fx-background-color: " +
                "linear-gradient(to bottom, rgba(36, 22, 12, 0.92), rgba(15, 9, 5, 0.92))," +
                "linear-gradient(to bottom, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.02));" +
            "-fx-background-insets: 0, 1;" +
            "-fx-background-radius: 10, 9;" +
            "-fx-border-color: rgba(242, 210, 122, 0.85);" +
            "-fx-border-width: 1.6px;" +
            "-fx-border-radius: 10px;" +
            "-fx-text-fill: #F5E6B3;" +
            "-fx-font-family: " + BUTTON_FONT_FAMILY + ";" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-letter-spacing: 0.5px;" +
            "-fx-padding: 8 18 8 18;";
    }

    private String luxuryButtonHoverStyle(double minWidth, double minHeight) {
        return
            "-fx-min-width: " + minWidth + "px;" +
            "-fx-min-height: " + minHeight + "px;" +
            "-fx-background-color: " +
                "linear-gradient(to bottom, rgba(243, 210, 122, 0.98), rgba(173, 124, 35, 0.98))," +
                "linear-gradient(to bottom, rgba(255, 255, 255, 0.18), rgba(255, 255, 255, 0.06));" +
            "-fx-background-insets: 0, 1;" +
            "-fx-background-radius: 10, 9;" +
            "-fx-border-color: rgba(255, 239, 190, 0.95);" +
            "-fx-border-width: 1.6px;" +
            "-fx-border-radius: 10px;" +
            "-fx-text-fill: #1a0a00;" +
            "-fx-font-family: " + BUTTON_FONT_FAMILY + ";" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-letter-spacing: 0.5px;" +
            "-fx-padding: 8 18 8 18;";
    }

    /**
     * Returns the currently selected mode value.
     *
     * @return selected mode value (true for online, false for offline)
     */
    public boolean getSelectedModeValue() {
        return this.selectedModeValue;
    }
}
