package edu.cs102.g04t06.game.presentation.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
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
        assertTrue(out.contains("reserve deck t1"), "Actions guide should show the hidden deck reserve syntax");
    }

    @Test
    void displayGameState_rendersOnlyFiveMostRecentLogEntries() throws Exception {
        actionLog(boardUI).add("entry 1");
        actionLog(boardUI).add("entry 2");
        actionLog(boardUI).add("entry 3");
        actionLog(boardUI).add("entry 4");
        actionLog(boardUI).add("entry 5");
        actionLog(boardUI).add("entry 6");

        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertFalse(out.contains("entry 1"), "Oldest log entry should be omitted when more than five exist");
        assertTrue(out.contains("entry 2"), "Second oldest retained log entry should remain visible");
        assertTrue(out.contains("entry 3"), "Middle retained log entry should remain visible");
        assertTrue(out.contains("entry 4"), "Recent retained log entry should remain visible");
        assertTrue(out.contains("entry 5"), "Recent retained log entry should remain visible");
        assertTrue(out.contains("entry 6"), "Most recent log entry should remain visible");
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
        assertTrue(out.contains("Reserved: [t2]"), "Sidebar should show reserved cards with their owner panel");
        assertTrue(out.contains("W:1"), "Sidebar should include full gem or bonus tokens, not only totals");
        assertTrue(out.contains("G:2"), "Sidebar should include all gem colors for other players");
    }

    @Test
    void displayGameState_highlightsAffordableCardsOnlyForPerspectivePlayer() {
        GameState state = makeGameState();
        Player alice = state.getPlayers().get(0);
        Player bob = state.getPlayers().get(1);
        alice.addGems(new GemCollection().add(GemColor.RED, 2).add(GemColor.BLUE, 1));
        bob.deductGems(bob.getGems());

        boardUI.setPerspectivePlayerName("Alice");
        boardUI.displayGameState(state);
        String aliceView = capture.toString();

        capture.reset();

        boardUI.setPerspectivePlayerName("Bob");
        boardUI.displayGameState(state);
        String bobView = capture.toString();

        assertTrue(aliceView.contains("\u001B[32m┌────────────┐\u001B[0m"),
                "Affordable market cards should be highlighted for the viewing player");
        assertFalse(bobView.contains("\u001B[32m┌────────────┐\u001B[0m"),
                "Opponents should not see another player's affordable-card highlight");
    }

    @Test
    void displayGameState_showsReservedCardCostsOnlyForPerspectivePlayer() {
        GameState state = makeGameState();
        Player alice = state.getPlayers().get(0);
        alice.addReservedCard(makeCard(1, 1, GemColor.RED, Map.of(GemColor.WHITE, 2, GemColor.BLUE, 1)));

        boardUI.setPerspectivePlayerName("Alice");
        boardUI.displayGameState(state);
        String ownerView = plainOutput();

        capture.reset();

        boardUI.setPerspectivePlayerName("Bob");
        boardUI.displayGameState(state);
        String opponentView = plainOutput();

        assertTrue(ownerView.contains("R1"), "Owner view should list reserved card slots with costs");
        assertTrue(ownerView.contains("w2u1"), "Owner view should show reserved card costs");
        assertFalse(opponentView.contains("w2u1"), "Opponent view should not reveal reserved card costs");
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildNoblePanelLines_wrapsFiveNoblesAcrossMultipleRows() throws Exception {
        List<Noble> nobles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<GemColor, Integer> req = new EnumMap<>(GemColor.class);
            req.put(GemColor.RED, 3);
            nobles.add(new Noble(i + 1, "Noble" + (i + 1), req));
        }

        Method method = GameBoardUI.class.getDeclaredMethod("buildNoblePanelLines", List.class);
        method.setAccessible(true);
        List<String> rows = (List<String>) method.invoke(boardUI, nobles);

        assertTrue(rows.size() > 4, "Five nobles should wrap into more than one visual row block");
    }

}
