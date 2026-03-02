package edu.cs102.g04t06.game.presentation.console;

import edu.cs102.g04t06.App;
import edu.cs102.g04t06.game.presentation.console.layout.BaseStack;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Onboarding view shown when the application starts.
 *
 * <p>This view renders a full-screen background image and an action row
 * containing mode selection plus a start button.</p>
 */
public class OnBoardingUI extends BaseStack {

    private static final String BG_IMAGE_URL = "/images/menuImg.jpeg";
    private static final double BUTTON_GROUP_BOTTOM_OFFSET_RATIO = -0.20;
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
        startBtn.setOnAction(e -> this.application.showLobby(this.selectedModeValue));
        return startBtn;
    }

    /**
     * Creates the mode selector dropdown.
     *
     * <p>The menu item labels are shown to the user while each item stores an
     * boolean mode value in {@code userData}. Selecting an item updates both
     * the button text and {@code selectedModeValue}.</p>
     *
     * @return configured mode selection menu with value-backed items
     */
    private MenuButton createModeOfPlayDropdown() {
        MenuButton menuButton = new MenuButton("Mode Of Play");
        menuButton.setPrefWidth(130);
        menuButton.getItems().addAll(
                createModeMenuItem(menuButton, "Online", true),
                createModeMenuItem(menuButton, "Offline", false)
        );
        return menuButton;
    }

    /**
     * Creates a single dropdown item with an associated boolean value.
     *
     * @param menuButton the parent button whose label is updated on selection
     * @param optionName the text shown for this selectable option
     * @param optionValue the internal boolean value to store for this option
     * @return configured menu item with width binding and selection handler
     */
    private CustomMenuItem createModeMenuItem(MenuButton menuButton, String optionName, boolean optionValue) {
        Label optionLabel = new Label(optionName);
        optionLabel.setAlignment(Pos.CENTER_LEFT);
        optionLabel.setMaxWidth(Double.MAX_VALUE);
        optionLabel.prefWidthProperty().bind(menuButton.widthProperty().subtract(16));

        CustomMenuItem optionItem = new CustomMenuItem(optionLabel, true);
        optionItem.setUserData(optionValue);
        optionItem.setOnAction(e -> {
            menuButton.setText(optionName);
            this.selectedModeValue = (boolean) optionItem.getUserData();
        });
        return optionItem;
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
