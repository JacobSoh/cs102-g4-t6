package edu.cs102.g04t06.game.presentation.console;

import edu.cs102.g04t06.App;
import edu.cs102.g04t06.game.presentation.console.layout.BaseStack;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Lobby UI – player slot selection screen for the Splendor game.
 * Shows up to 4 player slots: Player 1 is always the human player;
 * slots 2–4 start as "Waiting…" and can be filled by an NPC.
 *
 * command to run: mvn clean javafx:run
 */
public class LobbyUI extends BaseStack {

    // ── Styling constants ──────────────────────────────────────────────────
    private static final String CARD_BG       = "#1e1e1e";
    private static final String CARD_BORDER   = "#2a2a2a";
    private static final String HIGHLIGHT_CLR = "#8b5cf6";   // purple
    private static final String TEXT_COLOR    = "#e0e0e0";
    private static final String BTN_BG        = "#f0f0f0";
    private static final String BTN_TEXT      = "#1a1a1a";

    private static final String BG_IMAGE_URL  = "/images/menuImg.jpeg";
    private static final double BG_OPACITY    = 0.30;

    private static final int  SLOT_COUNT      = 4;
    private static final int  CARD_HEIGHT     = 110;
    private static final int  CARD_RADIUS     = 12;
    private static final int  CARD_SPACING    = 18;

    /** Which slot is currently highlighted (0-based). -1 = none. */
    private int selectedSlot = -1;

    /** Names for each slot; null = empty / waiting. */
    private final String[] slotNames = { "Player 1 (Me)", null, null, null };

    /** The visual card nodes so we can re-border them on selection. */
    private final StackPane[] cards = new StackPane[SLOT_COUNT];

    public LobbyUI(App application, boolean modeOfPlay) {
        buildUI(application, modeOfPlay);
    }

    // ── UI construction ────────────────────────────────────────────────────

    private void buildUI(App application, boolean modeOfPlay) {

        // ── Background image ───────────────────────────────────────────────
        // Place your background art at src/main/resources/images/lobby_bg.png
        // (or adjust the path below). Falls back to a plain dark gradient.
        StackPane backgroundPane = createBackground();

        // ── Central column of slot cards ───────────────────────────────────
        VBox cardColumn = new VBox(CARD_SPACING);
        cardColumn.setAlignment(Pos.CENTER);
        cardColumn.setMaxWidth(900);
        cardColumn.setPadding(new Insets(40, 0, 40, 0));

        for (int i = 0; i < SLOT_COUNT; i++) {
            StackPane card = buildSlotCard(i);
            cards[i] = card;
            cardColumn.getChildren().add(card);
        }

        // Wrap in a centred container that fills the scene
        StackPane centreWrapper = new StackPane(cardColumn);
        StackPane.setAlignment(cardColumn, Pos.CENTER);

        backgroundPane.getChildren().add(centreWrapper);
        this.root.getChildren().add(backgroundPane);
    }

    // ── Background ─────────────────────────────────────────────────────────

    private StackPane createBackground() {
        StackPane pane = new StackPane();
        pane.prefWidthProperty().bind(this.root.widthProperty());
        pane.prefHeightProperty().bind(this.root.heightProperty());

        try {
            // Attempt to load the game background art
            var bgUrl = getClass().getResource(BG_IMAGE_URL);
            if (bgUrl == null) {
                throw new IllegalStateException("Missing background resource: " + BG_IMAGE_URL);
            }

            ImageView iv = new ImageView(new Image(bgUrl.toExternalForm()));
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            iv.setOpacity(BG_OPACITY);
            iv.fitWidthProperty().bind(this.root.widthProperty());
            iv.fitHeightProperty().bind(this.root.heightProperty());

            pane.getChildren().add(iv);
        } catch (Exception e) {
            // Fallback: dark gradient background
            pane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1a2a3a, #0d1520);"
            );
        }

        return pane;
    }

    // ── Slot card ──────────────────────────────────────────────────────────

    /**
     * Builds one player-slot card.
     *
     * @param index 0-based slot index
     */
    private StackPane buildSlotCard(int index) {
        StackPane card = new StackPane();
        card.setMinHeight(CARD_HEIGHT);
        card.setMaxHeight(CARD_HEIGHT);
        card.setMaxWidth(Double.MAX_VALUE);

        applyCardStyle(card, false);

        // ── Content ────────────────────────────────────────────────────────
        HBox content = new HBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(0, 32, 0, 32));

        if (index == 0) {
            // Human player slot – name only, centred
            Label nameLabel = styledLabel(slotNames[0], 16, FontWeight.NORMAL);
            content.setAlignment(Pos.CENTER);
            content.getChildren().add(nameLabel);
        } else {
            addNpcSlotContent(content, index);
        }

        card.getChildren().add(content);

        // ── Click to select ────────────────────────────────────────────────
        final int slotIndex = index;
        card.setOnMouseClicked(e -> selectSlot(slotIndex));
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");

        return card;
    }

    // ── Interaction ────────────────────────────────────────────────────────

    private void selectSlot(int index) {
        // Deselect previous
        if (selectedSlot >= 0 && selectedSlot < SLOT_COUNT) {
            applyCardStyle(cards[selectedSlot], false);
        }

        selectedSlot = (selectedSlot == index) ? -1 : index;  // toggle

        if (selectedSlot >= 0) {
            applyCardStyle(cards[selectedSlot], true);
        }
    }

    private void handleAddNpc(int slotIndex) {
        // Replace "Waiting…" with an NPC name and rebuild card content
        slotNames[slotIndex] = "NPC " + slotIndex;
        rebuildSlotContent(slotIndex);
    }

    private void handleRemoveNpc(int slotIndex) {
        slotNames[slotIndex] = null;
        rebuildSlotContent(slotIndex);
    }

    /**
     * Rebuilds the interior content of a slot after state changes.
     * (Simple approach: clear and re-add children.)
     */
    private void rebuildSlotContent(int index) {
        StackPane card = cards[index];
        card.getChildren().clear();

        HBox content = new HBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(0, 32, 0, 32));

        if (index == 0) {
            Label nameLabel = styledLabel(slotNames[0], 16, FontWeight.NORMAL);
            content.getChildren().add(nameLabel);
        } else {
            addNpcSlotContent(content, index);
        }

        card.getChildren().add(content);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void addNpcSlotContent(HBox content, int index) {
        if (slotNames[index] == null) {
            Label waitingLabel = styledLabel("Waiting...", 15, FontWeight.NORMAL);

            Button addNpcBtn = new Button("Add NPC");
            applyPillButtonStyle(addNpcBtn);
            addNpcBtn.setOnAction(e -> handleAddNpc(index));

            content.getChildren().addAll(waitingLabel, addNpcBtn);
            return;
        }

        Label npcLabel = styledLabel(slotNames[index], 16, FontWeight.NORMAL);

        Button removeNpcBtn = new Button("Cancel");
        applyPillButtonStyle(removeNpcBtn);
        removeNpcBtn.setOnAction(e -> handleRemoveNpc(index));

        content.getChildren().addAll(npcLabel, removeNpcBtn);
    }

    private void applyPillButtonStyle(Button button) {
        button.setStyle(
            "-fx-background-color: " + BTN_BG + ";" +
            "-fx-text-fill: "       + BTN_TEXT + ";" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 6 18 6 18;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;"
        );
    }

    private void applyCardStyle(StackPane card, boolean highlighted) {
        card.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-background-radius: " + CARD_RADIUS + ";" +
            "-fx-border-color: " + (highlighted ? HIGHLIGHT_CLR : CARD_BORDER) + ";" +
            "-fx-border-width: " + (highlighted ? "2" : "1") + ";" +
            "-fx-border-radius: " + CARD_RADIUS + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0, 0, 4);"
        );
    }

    private Label styledLabel(String text, int fontSize, FontWeight weight) {
        Label label = new Label(text);
        label.setFont(javafx.scene.text.Font.font("System", weight, fontSize));
        label.setTextFill(javafx.scene.paint.Color.web(TEXT_COLOR));
        return label;
    }
}
