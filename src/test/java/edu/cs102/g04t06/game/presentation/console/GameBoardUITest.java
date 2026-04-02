package edu.cs102.g04t06.game.presentation.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
        if (costMap.isEmpty()) {
            costMap = Map.of(GemColor.WHITE, 1);
        }
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
        assertTrue(out.contains("B:2"), "Bank should show BLUE count from GameState using new 'B' code");
    }

    @Test
    void promptNetworkTurn_showsHelpLocallyAndReturnsNextCommand() {
        GameBoardUI networkBoard = new GameBoardUI(
                new Scanner(new ByteArrayInputStream("?\nclose-help\npass\n".getBytes())));

        String input = networkBoard.promptNetworkTurn(
                makeGameState(),
                "Your turn.",
                ThemeStyleSheet.CYAN,
                List.of("Alice: took gems"));

        assertEquals("pass", input, "Network prompt should keep reading after showing local help");

        String out = plainOutput();
        assertTrue(out.contains("SPLENDOR HELP"), "Help overlay should render for the local network player");
        assertTrue(out.contains("q exits the current game or LAN session."),
                "Help overlay should explain that q exits the current session");
        assertTrue(out.contains("Press any key, then Enter, to return to the game board."),
                "Help overlay should be dismissible locally before resuming the turn prompt");
    }

    @Test
    void displayGameState_includesPlayersFromState() {
        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertTrue(out.contains("Alice's turn"), "Current player should be labelled with 's turn");
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
        assertTrue(out.contains("[?] Help    [Q] Exit"), "Board header should advertise q as exit");
    }

    @Test
    void displayGameState_rendersOnlyThreeMostRecentLogEntries() throws Exception {
        // formatLogLines shows last 3 actions from actionLog
        Field alField = GameBoardUI.class.getDeclaredField("actionLog");
        alField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> log = (List<String>) alField.get(boardUI);
        log.add("entry 1");
        log.add("entry 2");
        log.add("entry 3");
        log.add("entry 4");

        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertFalse(out.contains("entry 1"), "Oldest log entry should be omitted when more than three exist");
        assertTrue(out.contains("entry 2"), "Second oldest retained log entry should remain visible");
        assertTrue(out.contains("entry 3"), "Recent retained log entry should remain visible");
        assertTrue(out.contains("entry 4"), "Most recent log entry should remain visible");
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
        assertTrue(ownerView.contains("w2b1"), "Owner view should show reserved card costs using new gem codes");
        assertFalse(opponentView.contains("w2b1"), "Opponent view should not reveal reserved card costs");
    }

    @Test
    void displayGameState_colorCodesCardRequirementsByGemColor() {
        Card marketCard = makeCard(1, 1, GemColor.WHITE, Map.of(GemColor.RED, 2, GemColor.BLUE, 1));
        Player alice = makePlayer("Alice", 0);
        alice.addGems(new GemCollection().add(GemColor.BLUE, 1));
        Card reservedCard = makeCard(1, 1, GemColor.RED, Map.of(GemColor.WHITE, 2, GemColor.BLUE, 1));

        try {
            Method marketCostMethod = GameBoardUI.class.getDeclaredMethod("formatCardCost", Card.class);
            marketCostMethod.setAccessible(true);
            String marketCost = (String) marketCostMethod.invoke(boardUI, marketCard);

            Method reservedCostMethod = GameBoardUI.class.getDeclaredMethod("formatReservedCost", Player.class, Card.class);
            reservedCostMethod.setAccessible(true);
            String reservedCost = (String) reservedCostMethod.invoke(boardUI, alice, reservedCard);

            assertTrue(marketCost.contains("\u001B[31mr2\u001B[0m"),
                    "Market card requirements should color red gem costs red");
            assertTrue(marketCost.contains("\u001B[34mb1\u001B[0m"),
                    "Market card requirements should color blue gem costs blue");
            assertTrue(reservedCost.contains("\u001B[37mw2\u001B[0m"),
                    "Reserved card requirements should color white gem costs white");
            assertTrue(reservedCost.contains("\u001B[34m\u001B[1mb1\u001B[0m"),
                    "Affordable reserved requirements should keep their gem color and add emphasis");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to invoke GameBoardUI cost formatters", e);
        }
    }

    // ==================== 8b: Gem Acronym Rename ====================

    @Test
    void displayGameState_bankShowsNewGemCodes() {
        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertTrue(out.contains("B:"), "Bank should use 'B' for Blue gem");
        assertTrue(out.contains("D:"), "Bank should use 'D' for Black gem");
        assertFalse(out.contains("U:"), "Old 'U' code for Blue should no longer appear");
        assertFalse(out.contains(" K:"), "Old 'K' code for Black should no longer appear");
    }

    // ==================== 8c: Gem Legend ====================

    @Test
    void displayGameState_bankIncludesGemLegend() {
        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertTrue(out.contains("W=White"), "Gem legend should define W");
        assertTrue(out.contains("R=Red"),   "Gem legend should define R");
        assertTrue(out.contains("B=Blue"),  "Gem legend should define B");
        assertTrue(out.contains("G=Green"), "Gem legend should define G");
        assertTrue(out.contains("D=Black"), "Gem legend should define D");
        assertTrue(out.contains("*=Gold"),  "Gem legend should define *");
    }

    // ==================== 8a: Player Turn Label ====================

    @Test
    void displayGameState_currentPlayerShowsCurrentPlayerLabel() {
        boardUI.displayGameState(makeGameState());
        String out = plainOutput();

        assertTrue(out.contains("'s turn"),
                "Current player label should show 's turn for both human and AI turns");
        assertFalse(out.contains("(you)"),
                "Old '(you)' label should no longer appear");
    }

    // ==================== 8d: Game-Over Screen ====================

    /** Builds a GameState already in game-over state with a named winner. */
    private GameState makeGameOverState(String winnerName, int winnerPts, int winnerCards,
                                        String loserName, int loserPts, int loserCards) {
        Player winner = new Player(winnerName, 0);
        Player loser  = new Player(loserName, 1);

        for (int i = 0; i < winnerCards; i++) {
            winner.addCard(makeCard(1, 0, GemColor.WHITE, Map.of()));
        }
        int ptsFromCards = winnerCards; // each addCard gives 0 pts above; award via nobles instead
        // Give winner points via cards that carry points
        winner = new Player(winnerName, 0);
        loser  = new Player(loserName, 1);
        for (int i = 0; i < winnerCards - 1; i++) {
            winner.addCard(makeCard(1, 0, GemColor.WHITE, Map.of()));
        }
        winner.addCard(makeCard(1, winnerPts, GemColor.WHITE, Map.of()));
        for (int i = 0; i < loserCards; i++) {
            loser.addCard(makeCard(1, i == 0 ? loserPts : 0, GemColor.WHITE, Map.of()));
        }

        List<Card> tier1 = new ArrayList<>();
        while (tier1.size() < 20) tier1.add(makeCard(1, 0, GemColor.WHITE, Map.of()));
        List<Card> tier2 = new ArrayList<>();
        while (tier2.size() < 20) tier2.add(makeCard(2, 0, GemColor.BLUE, Map.of()));
        List<Card> tier3 = new ArrayList<>();
        while (tier3.size() < 20) tier3.add(makeCard(3, 0, GemColor.GREEN, Map.of()));

        CardMarket market = new CardMarket(tier1, tier2, tier3);
        GemCollection bank = new GemCollection()
                .add(GemColor.WHITE, 4).add(GemColor.RED, 4)
                .add(GemColor.BLUE, 4).add(GemColor.GREEN, 4)
                .add(GemColor.BLACK, 4).add(GemColor.GOLD, 5);

        GameState state = new GameState(List.of(winner, loser), market, bank, new ArrayList<>(), 15);
        state.triggerFinalRound();
        state.advanceToNextPlayer(); // 0 → 1
        state.advanceToNextPlayer(); // 1 → 0, game over
        return state;
    }

    private String runShowAndCapture(GameState state) {
        capture.reset();
        Scanner testScanner = new Scanner(new java.io.ByteArrayInputStream("\n".getBytes()));
        GameBoardUI testUI = new GameBoardUI(testScanner);
        testUI.show(state);
        return capture.toString().replaceAll("\u001B\\[[;\\d]*m", "");
    }

    @Test
    void show_scenario1_clearWinnerByPoints() {
        // Alice 17 pts / 3 cards, Bob 15 pts / 2 cards — Alice wins by higher score
        GameState state = makeGameOverState("Alice", 17, 3, "Bob", 15, 2);
        String out = runShowAndCapture(state);

        assertTrue(out.contains("G A M E   O V E R"), "Game-over banner must appear");
        assertTrue(out.contains("W I N N E R"), "Winner announcement must appear");
        assertTrue(out.contains("Alice"), "Winner name must appear");
        assertTrue(out.contains("FINAL STANDINGS"), "Final standings section must appear");
        assertTrue(out.contains("HOW THE GAME WAS WON"), "Victory explanation must appear");
        assertTrue(out.contains("highest score"), "Scenario 1 must mention highest score");
    }

    @Test
    void show_scenario2_tiebreakByFewestCards() {
        // Alice 15 pts / 2 cards, Bob 15 pts / 4 cards — Alice wins tiebreak
        GameState state = makeGameOverState("Alice", 15, 2, "Bob", 15, 4);
        String out = runShowAndCapture(state);

        assertTrue(out.contains("G A M E   O V E R"), "Game-over banner must appear");
        assertTrue(out.contains("W I N N E R"), "Winner announcement must appear");
        assertTrue(out.contains("Alice"), "Tiebreak winner name must appear");
        assertTrue(out.contains("tie-break"), "Scenario 2 must mention tie-break");
        assertTrue(out.contains("fewest cards"), "Scenario 2 must mention fewest cards");
    }

    @Test
    void show_scenario3_sharedVictory() {
        // Alice 15 pts / 3 cards, Bob 15 pts / 3 cards — shared victory
        GameState state = makeGameOverState("Alice", 15, 3, "Bob", 15, 3);
        String out = runShowAndCapture(state);

        assertTrue(out.contains("G A M E   O V E R"), "Game-over banner must appear");
        assertTrue(out.contains("S H A R E D   V I C T O R Y"), "Scenario 3 must show SHARED VICTORY");
        assertTrue(out.contains("draw"), "Scenario 3 must mention draw");
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
