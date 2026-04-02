package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.List;

import edu.cs102.g04t06.game.execution.GameStateFactory;
import edu.cs102.g04t06.game.execution.ai.AIPlayer;
import edu.cs102.g04t06.game.execution.ai.AIStrategy;
import edu.cs102.g04t06.game.execution.ai.EasyAIStrategy;
import edu.cs102.g04t06.game.execution.ai.HardAIStrategy;
import edu.cs102.g04t06.game.presentation.console.MainMenuUI.MenuChoice;
import edu.cs102.g04t06.game.presentation.console.PlayerSetupUI.PlayerSetupResult;
import edu.cs102.g04t06.game.presentation.network.LanGameClient;
import edu.cs102.g04t06.game.presentation.network.LanGameServer;
import edu.cs102.g04t06.game.presentation.network.LanSetupUI;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Player;

/**
 * Coordinates the top-level console experience for the application.
 *
 * This controller owns the high-level navigation loop that connects the
 * splash screen, main menu, offline setup, LAN setup, and the main game board.
 * Individual screens remain responsible for collecting input and rendering
 * themselves, while this class decides which screen should run next and how
 * the resulting data should be turned into a playable game session.
 */
public class ConsoleUI implements ThemeStyleSheet {

    /**
     * Internal navigation targets for the console flow.
     */
    private enum Route {
        LOAD_SCREEN,
        MAIN_MENU,
        PLAYER_SETUP,
        HOST_LAN_SETUP,
        JOIN_LAN_SETUP,
        GAME_BOARD,
        EXIT
    }

    private final LoadScreenUI loadScreenUI;
    private final MainMenuUI mainMenuUI;
    private final PlayerSetupUI playerSetupUI;
    private final GameBoardUI gameBoardUI;
    private final LanSetupUI lanSetupUI;
    private final GameStateFactory gameStateFactory;
    private GameState gameState;

    /**
     * Creates the console controller and all screen dependencies needed for
     * interactive play.
     */
    public ConsoleUI() {
        this.loadScreenUI = new LoadScreenUI();
        this.mainMenuUI = new MainMenuUI();
        this.playerSetupUI = new PlayerSetupUI();
        this.gameBoardUI = new GameBoardUI();
        this.lanSetupUI = new LanSetupUI();
        this.gameStateFactory = new GameStateFactory();
    }

    /**
     * Primary app entry flow:
     * load screen -> main menu -> setup/new game -> game board.
     */
    public void showLoadScreen() {
        route(Route.LOAD_SCREEN);
    }

    /**
     * Compatibility entry point.
     * There is no separate lobby screen in the console flow, so this routes to setup.
     */
    public void showLobby(boolean modeOfPlay) {
        route(Route.PLAYER_SETUP);
    }

    /**
     * Compatibility entry point to open game board directly.
     */
    public void showGame() {
        route(Route.GAME_BOARD);
    }

    /**
     * Runs the console navigation loop starting from the provided route.
     *
     * @param start the first route to execute
     */
    private void route(Route start) {
        Route current = start;
        while (current != Route.EXIT) {
            current = switch (current) {
                case LOAD_SCREEN -> handleLoadScreen();
                case MAIN_MENU -> handleMainMenu();
                case PLAYER_SETUP -> handlePlayerSetup();
                case HOST_LAN_SETUP -> handleHostLanSetup();
                case JOIN_LAN_SETUP -> handleJoinLanSetup();
                case GAME_BOARD -> handleGameBoard();
                case EXIT -> Route.EXIT;
            };
        }
        exitProgram();
    }

    /**
     * Shows the splash screen and advances to the main menu.
     *
     * @return the next route to execute
     */
    private Route handleLoadScreen() {
        loadScreenUI.show();
        return Route.MAIN_MENU;
    }

    /**
     * Displays the main menu and converts the selected action into the next
     * controller route.
     *
     * @return the route selected by the user
     */
    private Route handleMainMenu() {
        MenuChoice choice = mainMenuUI.show();
        return switch (choice) {
            case OFFLINE_PLAY -> Route.PLAYER_SETUP;
            case HOST_LAN -> Route.HOST_LAN_SETUP;
            case JOIN_LAN -> Route.JOIN_LAN_SETUP;
            case QUIT -> Route.EXIT;
        };
    }

    /**
     * Collects offline player setup, initializes a new local game state, and
     * attaches AI controllers for any non-human players.
     *
     * @return the game board route when setup succeeds, otherwise the main menu
     */
    private Route handlePlayerSetup() {
        PlayerSetupResult setup = playerSetupUI.show();
        if (setup == null) {
            return Route.MAIN_MENU;
        }
        this.gameBoardUI.setPerspectivePlayerName(setup.localPlayerName);
        this.gameState = createInitialGameState(setup);

        List<AIPlayer> aiPlayerList = new ArrayList<>();
        List<Player> statePlayers = this.gameState.getPlayers();
        for (int i = 0; i < setup.isHuman.size(); i++) {
            if (!setup.isHuman.get(i)) {
                AIStrategy strategy = "HARD".equals(setup.aiDifficulties.get(i))
                        ? new HardAIStrategy()
                        : new EasyAIStrategy();
                aiPlayerList.add(new AIPlayer(statePlayers.get(i), strategy));
            }
        }
        this.gameBoardUI.setAIPlayers(aiPlayerList);

        return Route.GAME_BOARD;
    }

    /**
     * Opens the game board if a local game state is available.
     *
     * @return the next route after the board exits
     */
    private Route handleGameBoard() {
        if (gameState == null) {
            printLoadGameStub();
            return Route.MAIN_MENU;
        }
        gameBoardUI.show(gameState);
        return Route.MAIN_MENU;
    }

    /**
     * Runs the LAN host setup flow and starts a host-owned game session.
     *
     * @return the main menu route after the LAN session ends
     */
    private Route handleHostLanSetup() {
        LanSetupUI.HostSetup setup = lanSetupUI.promptHostSetup();
        new LanGameServer(setup.port, setup.totalPlayers, setup.hostPlayerName, setup.hostPlayerAge).run();
        return Route.MAIN_MENU;
    }

    /**
     * Runs the LAN join flow and connects to an existing host session.
     *
     * @return the main menu route after the LAN session ends
     */
    private Route handleJoinLanSetup() {
        LanSetupUI.JoinSetup setup = lanSetupUI.promptJoinSetup();
        new LanGameClient(setup.playerName, setup.playerAge, setup.hostAddress, setup.port).run();
        return Route.MAIN_MENU;
    }

    /**
     * Displays the placeholder message used when the board is requested before
     * any game state has been prepared.
     */
    private void printLoadGameStub() {
        System.out.println();
        System.out.println(RED + BOLD + "Load Game is not implemented yet." + RESET);
        System.out.println("Returning to main menu...");
        sleep(1200);
    }

    /**
     * Terminates the application after the navigation loop has finished.
     */
    private void exitProgram() {
        System.out.println("Exiting Splendor.");
        System.exit(0);
    }

    /**
     * Pauses execution briefly to keep transient status messages visible.
     *
     * @param ms the delay duration in milliseconds
     */
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Builds an initial game state from the ordered players collected during
     * offline setup.
     *
     * @param setup the offline setup result containing player identities
     * @return a newly initialized game state
     */
    private GameState createInitialGameState(PlayerSetupResult setup) {
        List<String> playerNames = new ArrayList<>();
        for (Player p : setup.players) {
            playerNames.add(p.getName());
        }

        return gameStateFactory.createInitialGameState(setup.totalPlayers, playerNames);
    }
}
