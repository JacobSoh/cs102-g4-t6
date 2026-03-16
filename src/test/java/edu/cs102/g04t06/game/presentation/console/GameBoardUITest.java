package edu.cs102.g04t06.game.presentation.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Unit tests for {@link GameBoardUI} rendering from real {@link GameState} data.
 */
class GameBoardUITest {

    private ByteArrayOutputStream capture;
    private PrintStream originalOut;
    private GameBoardUI boardUI;

    @BeforeEach
    void setUp() {
        capture = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(capture));
        boardUI = new GameBoardUI();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private String plainOutput() {
        return capture.toString().replaceAll("\u001B\\[[;\\d]*m", "");
    }

    private Player makePlayer(String name, int order) {
        Player p = new Player(name, order);
        p.addGems(new GemCollection().add(GemColor.WHITE, 1).add(GemColor.RED, 2));
        return p;
    }

    private Card makeCard(int tier, int points, GemColor bonus, Map<GemColor, Integer> costMap) {
        return new Card(tier, points, bonus, new Cost(costMap));
    }

    private GameState makeGameState() {
        Player alice = makePlayer("Alice", 0);
        Player bob = makePlayer("Bob", 1);

        List<Card> tier1 = new ArrayList<>();
        tier1.add(makeCard(1, 1, GemColor.WHITE, Map.of(GemColor.RED, 2, GemColor.BLUE, 1)));
        while (tier1.size() < 20) {
            tier1.add(makeCard(1, 0, GemColor.BLUE, Map.of(GemColor.WHITE, 1)));
        }

        List<Card> tier2 = new ArrayList<>();
        tier2.add(makeCard(2, 2, GemColor.GREEN, Map.of(GemColor.BLACK, 3, GemColor.RED, 2)));
        while (tier2.size() < 20) {
            tier2.add(makeCard(2, 1, GemColor.RED, Map.of(GemColor.BLUE, 2)));
        }

        List<Card> tier3 = new ArrayList<>();
        tier3.add(makeCard(3, 4, GemColor.BLACK, Map.of(GemColor.WHITE, 3, GemColor.GREEN, 3)));
        while (tier3.size() < 20) {
            tier3.add(makeCard(3, 3, GemColor.GREEN, Map.of(GemColor.BLACK, 3)));
        }

        CardMarket market = new CardMarket(tier1, tier2, tier3);

        GemCollection bank = new GemCollection()
                .add(GemColor.WHITE, 4)
                .add(GemColor.RED, 3)
                .add(GemColor.BLUE, 2)
                .add(GemColor.GREEN, 1)
                .add(GemColor.BLACK, 2)
                .add(GemColor.GOLD, 5);

        Map<GemColor, Integer> req = new EnumMap<>(GemColor.class);
        req.put(GemColor.WHITE, 3);
        req.put(GemColor.RED, 3);
        Noble noble = new Noble(1, "Test Noble", req);

        return new GameState(List.of(alice, bob), market, bank, new ArrayList<>(List.of(noble)), 15);
    }

    @SuppressWarnings("unchecked")
    private List<String> actionLog(GameBoardUI ui) throws Exception {
        Field actionLogField = GameBoardUI.class.getDeclaredField("actionLog");
        actionLogField.setAccessible(true);
        return (List<String>) actionLogField.get(ui);
    }

    private void invokePrivate(GameBoardUI ui, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = GameBoardUI.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(ui, args);
    }

    @Test
    void displayGameState_producesNonEmptyOutput() {
        boardUI.displayGameState(makeGameState());
        assertFalse(plainOutput().isBlank(), "displayGameState should render non-empty output");
    }

    @Test
    void displayGameState_includesSections() {
        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertTrue(out.contains("NOBLES"), "Board should include nobles section");
        assertTrue(out.contains("TIER 3"), "Board should include tier 3 section");
        assertTrue(out.contains("TIER 2"), "Board should include tier 2 section");
        assertTrue(out.contains("TIER 1"), "Board should include tier 1 section");
        assertTrue(out.contains("BANK GEMS"), "Board should include bank gems section");
    }

    @Test
    void displayGameState_includesGemBankDataFromState() {
        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertTrue(out.contains("W:4"), "Bank should show WHITE count from GameState");
        assertTrue(out.contains("R:3"), "Bank should show RED count from GameState");
        assertTrue(out.contains("U:2"), "Bank should show BLUE count from GameState");
    }

    @Test
    void displayGameState_includesPlayersFromState() {
        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertTrue(out.contains("Alice (you)"), "Current player should be rendered as current");
        assertTrue(out.contains("Bob"), "Other player should be rendered");
    }

    @Test
    void displayGameState_includesNobleRequirementsFromState() {
        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertTrue(out.contains("W:3"), "Noble white requirement should be rendered");
        assertTrue(out.contains("R:3"), "Noble red requirement should be rendered");
    }

    @Test
    void displayGameState_showsUpdatedReserveBuyGuide() {
        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertTrue(out.contains("buy reserve 1"), "Actions guide should show the reserve slot syntax");
    }

    @Test
    void displayGameState_rendersOnlyTwoMostRecentLogEntries() throws Exception {
        actionLog(boardUI).add("old entry");
        actionLog(boardUI).add("middle entry");
        actionLog(boardUI).add("latest entry");

        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertFalse(out.contains("old entry"), "Oldest log entry should be omitted when more than two exist");
        assertTrue(out.contains("middle entry"), "Second most recent log entry should remain visible");
        assertTrue(out.contains("latest entry"), "Most recent log entry should remain visible");
    }

    @Test
    void displayGameState_rendersFullOtherPlayerDetailsIncludingReservedCards() {
        GameState state = makeGameState();
        Player bob = state.getPlayers().get(1);
        bob.addGems(new GemCollection()
                .add(GemColor.BLUE, 1)
                .add(GemColor.GREEN, 2)
                .add(GemColor.BLACK, 1)
                .add(GemColor.GOLD, 1));
        bob.addCard(makeCard(1, 0, GemColor.WHITE, Map.of(GemColor.RED, 1)));
        bob.addCard(makeCard(1, 1, GemColor.GREEN, Map.of(GemColor.BLUE, 2)));
        bob.addReservedCard(makeCard(2, 1, GemColor.RED, Map.of(GemColor.WHITE, 2)));

        boardUI.displayGameState(state);
        String out = plainOutput();

        assertTrue(out.contains("PLAYERS"), "Sidebar should include the player lineup heading");
        assertTrue(out.contains("Bonus"), "Sidebar should render bonus details");
        assertTrue(out.contains("Reserved: [R1]"), "Sidebar should show reserved cards with their owner panel");
        assertTrue(out.contains("W:1"), "Sidebar should include full gem or bonus tokens, not only totals");
        assertTrue(out.contains("G:2"), "Sidebar should include all gem colors for other players");
    }

    @Test
    void endOfTurnWinCheck_triggersFinalRoundAndEndsOnlyAfterRoundWrap() throws Exception {
        GameState state = makeGameState();
        Player alice = state.getPlayers().get(0);
        for (int i = 0; i < 15; i++) {
            alice.addCard(makeCard(1, 1, GemColor.WHITE, Map.of(GemColor.RED, 1)));
        }

        invokePrivate(boardUI, "handleWinCheck", new Class<?>[]{GameState.class, Player.class}, state, alice);

        assertTrue(state.isFinalRoundTriggered(), "Reaching the threshold should trigger the final round");
        assertFalse(state.isGameOver(), "Game should not end immediately when the threshold is reached");

        state.advanceToNextPlayer();
        assertFalse(state.isGameOver(), "Game should continue for remaining players in the round");

        state.advanceToNextPlayer();
        assertTrue(state.isGameOver(), "Game should end only after the round wraps back to player 1");
    }

    @Test
    void handleGameEnd_picksHighestScoringEligibleWinnerAtRoundEnd() throws Exception {
        GameState state = makeGameState();
        Player alice = state.getPlayers().get(0);
        Player bob = state.getPlayers().get(1);
        for (int i = 0; i < 15; i++) {
            alice.addCard(makeCard(1, 1, GemColor.WHITE, Map.of(GemColor.RED, 1)));
        }
        for (int i = 0; i < 16; i++) {
            bob.addCard(makeCard(1, 1, GemColor.BLUE, Map.of(GemColor.WHITE, 1)));
        }

        state.triggerFinalRound();
        state.setGameOver(true);

        invokePrivate(boardUI, "handleGameEnd", new Class<?>[]{GameState.class}, state);

        assertTrue(actionLog(boardUI).stream().anyMatch(entry -> entry.contains("Winner: Bob")),
                "Final winner should be chosen from all eligible players at round end");
        assertTrue(plainOutput().isBlank(), "Winner announcement should be kept in UI state rather than printed directly");
    }
}
