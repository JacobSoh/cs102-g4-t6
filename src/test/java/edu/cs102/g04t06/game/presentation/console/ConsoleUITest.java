package edu.cs102.g04t06.game.presentation.console;

// =============================================================================
// ConsoleUITest.java
// Written by Claude Sonnet 4.6 (Anthropic)
//
// JUnit 5 tests for the routing-layer ConsoleUI.
// ConsoleUI is a pure navigator: its public API (showLoadScreen, showLobby,
// showGame) all terminate with System.exit(0), so routing behaviour is tested
// by invoking the private handler methods directly via reflection.
// =============================================================================

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.presentation.console.PlayerSetupUI.PlayerSetupResult;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Player;

/**
 * Unit tests for {@link ConsoleUI}.
 *
 * <p>Because all public entry points ({@code showLoadScreen}, {@code showLobby},
 * {@code showGame}) eventually call {@code System.exit(0)}, the private handler
 * methods are exercised via reflection so individual routing decisions can be
 * asserted without killing the JVM.
 *
 * <p>{@link System#in} is replaced before constructing each {@code ConsoleUI}
 * so that all sub-UI scanners (which are initialised in the constructor) read
 * from the injected byte stream rather than the real terminal.
 */
class ConsoleUITest {

    private ByteArrayOutputStream capturedOut;
    private PrintStream            originalOut;
    private InputStream            originalIn;

    @BeforeEach
    void redirectIO() {
        originalOut = System.out;
        originalIn  = System.in;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void restoreIO() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Sets System.in to a stream of the given lines, then returns a freshly
     * constructed ConsoleUI so all sub-UI scanners pick up the injected input.
     */
    private ConsoleUI uiWithInput(String... lines) {
        String joined = String.join("\n", lines) + "\n";
        System.setIn(new ByteArrayInputStream(joined.getBytes()));
        return new ConsoleUI();
    }

    /** ANSI-stripped captured output. */
    private String out() {
        return capturedOut.toString().replaceAll("\u001B\\[[;\\d]*m", "");
    }

    /**
     * Reflectively invokes a private no-arg method on {@code target} and
     * returns whatever the method returns (may be {@code null} for void).
     */
    private Object invokePrivate(Object target, String methodName) {
        return invokePrivate(target, methodName, new Class[0]);
    }

    /**
     * Reflectively invokes a private method with the given parameter types and
     * arguments. Unwraps {@link InvocationTargetException} so that thrown
     * exceptions propagate naturally in tests.
     */
    private Object invokePrivate(Object target, String methodName,
                                 Class<?>[] paramTypes, Object... args) {
        try {
            Method m = target.getClass().getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return m.invoke(target, args);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================================
    // 1. Constructor
    // =========================================================================

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("new ConsoleUI() succeeds and returns a non-null instance")
        void constructorSucceeds() {
            assertNotNull(uiWithInput());
        }
    }

    // =========================================================================
    // 2. PlayerSetupResult — the core data object ConsoleUI initialises
    // =========================================================================

    @Nested
    @DisplayName("PlayerSetupResult")
    class PlayerSetupResultTests {

        private PlayerSetupResult make(String local, boolean online,
                                       List<Player> players, List<Boolean> humans) {
            List<String> diffs = new ArrayList<>();
            for (Boolean h : humans) diffs.add(h ? null : "EASY");
            return new PlayerSetupResult(local, online, players, humans, diffs);
        }

        @Test
        @DisplayName("2-player offline setup stores localPlayerName correctly")
        void twoPlayerOfflineStoresLocalName() {
            var r = make("Alice", false,
                    List.of(new Player("Alice", 0), new Player("CPU-1", 1)),
                    List.of(true, false));
            assertEquals("Alice", r.localPlayerName);
        }

        @Test
        @DisplayName("2-player offline setup stores isOnline = false")
        void twoPlayerOfflineStoresMode() {
            var r = make("Alice", false,
                    List.of(new Player("Alice", 0), new Player("CPU-1", 1)),
                    List.of(true, false));
            assertFalse(r.isOnline);
        }

        @Test
        @DisplayName("4-player online setup stores isOnline = true")
        void fourPlayerOnlineStoresMode() {
            List<Player> players = List.of(
                    new Player("Alice", 0), new Player("Bob", 1),
                    new Player("Carol", 2), new Player("Dave", 3));
            var r = make("Alice", true, players, List.of(true, true, true, true));
            assertTrue(r.isOnline);
        }

        @Test
        @DisplayName("totalPlayers equals the size of the players list")
        void totalPlayersMatchesListSize() {
            List<Player> p = List.of(new Player("A", 0), new Player("B", 1), new Player("C", 2));
            var r = make("A", false, p, List.of(true, false, false));
            assertEquals(p.size(), r.totalPlayers);
        }

        @Test
        @DisplayName("mismatched players/isHuman sizes throw IllegalArgumentException")
        void mismatchedSizesThrow() {
            List<Player>  players = List.of(new Player("Alice", 0));
            List<Boolean> humans  = List.of(true, false); // one too many
            assertThrows(IllegalArgumentException.class,
                    () -> make("Alice", false, players, humans));
        }

        @Test
        @DisplayName("players list is unmodifiable after construction")
        void playersListIsUnmodifiable() {
            List<Player>  p = new ArrayList<>(List.of(new Player("A", 0), new Player("B", 1)));
            List<Boolean> h = new ArrayList<>(List.of(true, false));
            var r = make("A", false, p, h);
            assertThrows(UnsupportedOperationException.class,
                    () -> r.players.add(new Player("C", 2)));
        }

        @Test
        @DisplayName("isHuman list is unmodifiable after construction")
        void isHumanListIsUnmodifiable() {
            List<Player>  p = new ArrayList<>(List.of(new Player("A", 0), new Player("B", 1)));
            List<Boolean> h = new ArrayList<>(List.of(true, true));
            var r = make("A", true, p, h);
            assertThrows(UnsupportedOperationException.class, () -> r.isHuman.add(false));
        }

        @Test
        @DisplayName("toString() includes 'Offline' for offline mode")
        void toStringIncludesOfflineMode() {
            var r = make("A", false,
                    List.of(new Player("A", 0), new Player("B", 1)),
                    List.of(true, false));
            assertTrue(r.toString().contains("Offline"));
        }

        @Test
        @DisplayName("toString() includes 'Online' for online mode")
        void toStringIncludesOnlineMode() {
            var r = make("A", true,
                    List.of(new Player("A", 0), new Player("B", 1)),
                    List.of(true, true));
            assertTrue(r.toString().contains("Online"));
        }

        @Test
        @DisplayName("toString() includes all player names")
        void toStringIncludesPlayerNames() {
            var r = make("Zoë", false,
                    List.of(new Player("Zoë", 0), new Player("CPU-2", 1)),
                    List.of(true, false));
            String s = r.toString();
            assertTrue(s.contains("Zoë"));
            assertTrue(s.contains("CPU-2"));
        }

        @Test
        @DisplayName("toString() marks human players with [Human]")
        void toStringMarksHumans() {
            var r = make("Alice", true,
                    List.of(new Player("Alice", 0), new Player("Bob", 1)),
                    List.of(true, true));
            assertTrue(r.toString().contains("[Human]"));
        }

        @Test
        @DisplayName("toString() marks CPU players with [CPU]")
        void toStringMarksCPU() {
            var r = make("Alice", false,
                    List.of(new Player("Alice", 0), new Player("CPU-1", 1)),
                    List.of(true, false));
            assertTrue(r.toString().contains("[CPU"), "toString should mark CPU players");
        }

        @Test
        @DisplayName("toString() includes the player count")
        void toStringIncludesCount() {
            List<Player>  p = List.of(new Player("A", 0), new Player("B", 1), new Player("C", 2));
            List<Boolean> h = List.of(true, false, false);
            var r = make("A", false, p, h);
            assertTrue(r.toString().contains("3"));
        }
    }

    // =========================================================================
    // 3. handleLoadScreen
    // =========================================================================

    @Nested
    @DisplayName("handleLoadScreen")
    class HandleLoadScreenTests {

        @Test
        @DisplayName("always returns MAIN_MENU after showing the load screen")
        void returnsMainMenu() {
            // LoadScreenUI.waitForEnter() reads one blank line then returns
            ConsoleUI ui = uiWithInput("");
            Object result = invokePrivate(ui, "handleLoadScreen");
            assertEquals("MAIN_MENU", result.toString());
        }

        @Test
        @DisplayName("load screen output contains the game title or version")
        void outputContainsVersionOrTitle() {
            ConsoleUI ui = uiWithInput("");
            invokePrivate(ui, "handleLoadScreen");
            String o = out();
            assertTrue(o.contains("SPLENDOR") || o.contains("v1.0.0"),
                    "Load screen should mention SPLENDOR or version string");
        }
    }

    // =========================================================================
    // 4. handleMainMenu
    // =========================================================================

    @Nested
    @DisplayName("handleMainMenu")
    class HandleMainMenuTests {

        @Test
        @DisplayName("'q' input returns EXIT")
        void quitInputReturnsExit() {
            ConsoleUI ui = uiWithInput("q");
            assertEquals("EXIT", invokePrivate(ui, "handleMainMenu").toString());
        }

        @Test
        @DisplayName("'Q' (uppercase) also returns EXIT")
        void uppercaseQReturnsExit() {
            ConsoleUI ui = uiWithInput("Q");
            assertEquals("EXIT", invokePrivate(ui, "handleMainMenu").toString());
        }

        @Test
        @DisplayName("'o' input returns PLAYER_SETUP")
        void newGameInputReturnsPlayerSetup() {
            ConsoleUI ui = uiWithInput("o");
            assertEquals("PLAYER_SETUP", invokePrivate(ui, "handleMainMenu").toString());
        }

        @Test
        @DisplayName("'l' input is now invalid and the menu continues until a valid choice")
        void removedLoadGameInputIsRejected() {
            ConsoleUI ui = uiWithInput("l", "q");
            Object result = invokePrivate(ui, "handleMainMenu");
            assertEquals("EXIT", result.toString());
            assertTrue(out().contains("Invalid choice"),
                    "Removed load-game key should now be treated as invalid input");
        }
    }

    // =========================================================================
    // 5. handleGameBoard
    // =========================================================================

    @Nested
    @DisplayName("handleGameBoard")
    class HandleGameBoardTests {

        @Test
        @DisplayName("null gameState prints the 'not implemented' stub and returns MAIN_MENU")
        void nullGameStateReturnsMainMenuAndPrintsStub() {
            ConsoleUI ui = uiWithInput();
            // gameState field is null by default after construction
            Object result = invokePrivate(ui, "handleGameBoard");
            assertEquals("MAIN_MENU", result.toString());
            assertTrue(out().contains("not implemented"),
                    "Stub message should appear when gameState is null");
        }

        @Test
        @DisplayName("null gameState outputs the 'Returning to main menu' notice")
        void nullGameStateOutputsReturnNotice() {
            ConsoleUI ui = uiWithInput();
            invokePrivate(ui, "handleGameBoard");
            assertTrue(out().toLowerCase().contains("main menu"));
        }
    }

    // =========================================================================
    // 6. handlePlayerSetup
    // =========================================================================

    @Nested
    @DisplayName("handlePlayerSetup")
    class HandlePlayerSetupTests {

        @Test
        @DisplayName("completing setup returns GAME_BOARD")
        void completedSetupReturnsGameBoard() {
            // PlayerSetupUI.show() sequence: name → birthday → total players → difficulty → Enter to continue
            ConsoleUI ui = uiWithInput("Alice", "2000-01-01", "2", "1", "");
            Object result = invokePrivate(ui, "handlePlayerSetup");
            assertEquals("GAME_BOARD", result.toString());
        }
    }

    @Nested
    @DisplayName("Player setup summary")
    class PlayerSetupSummaryTests {

        @Test
        @DisplayName("marks the local human player as 'you' even when sorted after a CPU")
        void summaryMarksLocalPlayerInsteadOfFirstPlayer() {
            System.setIn(new ByteArrayInputStream("\n".getBytes()));
            PlayerSetupUI setupUI = new PlayerSetupUI();

            List<Player> players = List.of(
                    new Player("CPU-1", 0),
                    new Player("dongey", 1));
            PlayerSetupResult result = new PlayerSetupResult(
                    "dongey",
                    false,
                    players,
                    List.of(false, true),
                    Arrays.asList("HARD", null));

            invokePrivate(setupUI, "showSummary", new Class[]{PlayerSetupResult.class}, result);

            String output = out();
            assertTrue(output.contains("CPU-1  [CPU - HARD]"),
                    "CPU entry should still be shown as CPU.");
            assertTrue(output.contains("dongey  [Human] ← you"),
                    "The local player should receive the 'you' marker.");
            assertFalse(output.contains("CPU-1  [CPU - HARD] ← you"),
                    "The first player should not be marked as 'you' when they are a CPU.");
        }
    }

    // =========================================================================
    // 7. printLoadGameStub
    // =========================================================================

    @Nested
    @DisplayName("printLoadGameStub")
    class PrintLoadGameStubTests {

        @Test
        @DisplayName("outputs the 'not implemented' message")
        void outputsNotImplementedMessage() {
            ConsoleUI ui = uiWithInput();
            invokePrivate(ui, "printLoadGameStub");
            assertTrue(out().contains("not implemented"));
        }

        @Test
        @DisplayName("outputs the 'Returning to main menu' notice")
        void outputsReturningToMainMenuNotice() {
            ConsoleUI ui = uiWithInput();
            invokePrivate(ui, "printLoadGameStub");
            assertTrue(out().toLowerCase().contains("main menu"));
        }
    }

    // =========================================================================
    // 8. createInitialGameState — full CSV → GameEngine pipeline
    // =========================================================================

    @Nested
    @DisplayName("createInitialGameState")
    class CreateInitialGameStateTests {

        /** Builds a 2-player offline PlayerSetupResult for Alice + CPU-1. */
        private PlayerSetupResult twoPlayerOffline() {
            List<Player>  players = new ArrayList<>();
            players.add(new Player("Alice", 0));
            players.add(new Player("CPU-1", 1));
            return new PlayerSetupResult(
                    "Alice", false, players, List.of(true, false), Arrays.asList(null, "EASY"));
        }

        private GameState init(PlayerSetupResult setup) {
            ConsoleUI ui = uiWithInput();
            return (GameState) invokePrivate(ui, "createInitialGameState",
                    new Class[]{PlayerSetupResult.class}, setup);
        }

        @Test
        @DisplayName("returns a non-null GameState")
        void returnsNonNull() {
            assertNotNull(init(twoPlayerOffline()));
        }

        @Test
        @DisplayName("GameState has the correct player count (2)")
        void playerCountIsCorrect() {
            assertEquals(2, init(twoPlayerOffline()).getPlayers().size());
        }

        @Test
        @DisplayName("players carry the names given in the setup")
        void playerNamesMatchSetup() {
            GameState state = init(twoPlayerOffline());
            List<String> names = state.getPlayers().stream()
                    .map(Player::getName).toList();
            assertTrue(names.contains("Alice"));
            assertTrue(names.contains("CPU-1"));
        }

        @Test
        @DisplayName("gem bank is non-empty at game start")
        void gemBankIsNonEmpty() {
            assertTrue(init(twoPlayerOffline()).getGemBank().getTotalCount() > 0);
        }

        @Test
        @DisplayName("market has at least one visible tier-1 card")
        void marketHasVisibleCards() {
            GameState state = init(twoPlayerOffline());
            assertNotNull(state.getMarket());
            assertNotNull(state.getMarket().getVisibleCard(1, 0));
        }

        @Test
        @DisplayName("nobles pool is non-empty")
        void noblesPoolIsNonEmpty() {
            assertFalse(init(twoPlayerOffline()).getAvailableNobles().isEmpty());
        }

        @Test
        @DisplayName("winning threshold is 15 (from config.properties)")
        void winningThresholdIsFromConfig() {
            assertEquals(15, init(twoPlayerOffline()).getWinningThreshold());
        }

        @Test
        @DisplayName("4-player game has exactly 5 nobles available")
        void fourPlayerGameHasFiveNobles() {
            List<Player>  players = new ArrayList<>();
            List<Boolean> humans  = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                players.add(new Player("CPU-" + i, i));
                humans.add(false);
            }
            List<String> diffs = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) diffs.add("EASY");
            PlayerSetupResult setup = new PlayerSetupResult(
                    "CPU-0", false, players, humans, diffs);
            assertEquals(5, init(setup).getAvailableNobles().size());
        }

        @Test
        @DisplayName("game starts in round 1")
        void roundStartsAtOne() {
            assertEquals(1, init(twoPlayerOffline()).getRoundNumber());
        }

        @Test
        @DisplayName("game is not over at start")
        void gameNotOverAtStart() {
            assertFalse(init(twoPlayerOffline()).isGameOver());
        }

        @Test
        @DisplayName("first player has no gems at start")
        void firstPlayerStartsWithNoGems() {
            GameState state = init(twoPlayerOffline());
            assertEquals(0, state.getPlayers().get(0).getGemCount());
        }

        @Test
        @DisplayName("first player has no purchased cards at start")
        void firstPlayerStartsWithNoCards() {
            GameState state = init(twoPlayerOffline());
            assertTrue(state.getPlayers().get(0).getPurchasedCards().isEmpty());
        }
    }
}
