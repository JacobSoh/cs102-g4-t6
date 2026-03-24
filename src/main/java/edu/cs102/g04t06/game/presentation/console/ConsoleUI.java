package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.execution.GameStateFactory;
import edu.cs102.g04t06.game.execution.GameEngine;
import edu.cs102.g04t06.game.execution.ai.AIPlayer;
import edu.cs102.g04t06.game.execution.ai.AIStrategy;
import edu.cs102.g04t06.game.execution.ai.EasyAIStrategy;
import edu.cs102.g04t06.game.execution.ai.HardAIStrategy;
import edu.cs102.g04t06.game.infrastructure.config.ConfigLoader;
import edu.cs102.g04t06.game.infrastructure.config.ExcelDataLoader;
import edu.cs102.g04t06.game.presentation.console.MainMenuUI.MenuChoice;
import edu.cs102.g04t06.game.presentation.console.PlayerSetupUI.PlayerSetupResult;
import edu.cs102.g04t06.game.presentation.network.LanGameClient;
import edu.cs102.g04t06.game.presentation.network.LanGameServer;
import edu.cs102.g04t06.game.presentation.network.LanSetupUI;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Player;

/**
 * Routes console screens through one navigation flow.
 */
public class ConsoleUI implements ThemeStyleSheet {

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

    private Route handleLoadScreen() {
        loadScreenUI.show();
        return Route.MAIN_MENU;
    }

    private Route handleMainMenu() {
        MenuChoice choice = mainMenuUI.show();
        return switch (choice) {
            case OFFLINE_PLAY -> Route.PLAYER_SETUP;
            case HOST_LAN -> Route.HOST_LAN_SETUP;
            case JOIN_LAN -> Route.JOIN_LAN_SETUP;
            case LOAD_GAME -> {
                printLoadGameStub();
                yield Route.MAIN_MENU;
            }
            case QUIT -> Route.EXIT;
        };
    }

    private Route handlePlayerSetup() {
        PlayerSetupResult setup = playerSetupUI.show();
        if (setup == null) {
            return Route.MAIN_MENU;
        }
        this.gameState = createInitialGameState(setup);

        // Register AI players for CPU slots
        List<Player> gamePlayers = this.gameState.getPlayers();
        Map<Player, AIPlayer> aiPlayerMap = new HashMap<>();
        AIStrategy strategy = setup.aiDifficulty.equals("EASY")
                ? new EasyAIStrategy()
                : new HardAIStrategy();
        for (int i = 0; i < gamePlayers.size(); i++) {
            if (i < setup.isHuman.size() && !setup.isHuman.get(i)) {
                Player p = gamePlayers.get(i);
                aiPlayerMap.put(p, new AIPlayer(p, strategy));
            }
        }
        gameBoardUI.registerAIPlayers(aiPlayerMap);

        return Route.GAME_BOARD;
    }

    private Route handleGameBoard() {
        if (gameState == null) {
            printLoadGameStub();
            return Route.MAIN_MENU;
        }
        gameBoardUI.show(gameState);
        return Route.MAIN_MENU;
    }

    private Route handleHostLanSetup() {
        LanSetupUI.HostSetup setup = lanSetupUI.promptHostSetup();
        new LanGameServer(setup.port, setup.totalPlayers, setup.hostPlayerName).run();
        return Route.MAIN_MENU;
    }

    private Route handleJoinLanSetup() {
        LanSetupUI.JoinSetup setup = lanSetupUI.promptJoinSetup();
        new LanGameClient(setup.playerName, setup.hostAddress, setup.port).run();
        return Route.MAIN_MENU;
    }

    private void printLoadGameStub() {
        System.out.println();
        System.out.println(RED + BOLD + "Load Game is not implemented yet." + RESET);
        System.out.println("Returning to main menu...");
        sleep(1200);
    }

    private void exitProgram() {
        System.out.println("Exiting Splendor.");
        System.exit(0);
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private GameState createInitialGameState(PlayerSetupResult setup) {
        List<String> playerNames = new ArrayList<>();
        for (Player p : setup.players) {
            playerNames.add(p.getName());
        }

        return gameStateFactory.createInitialGameState(setup.totalPlayers, playerNames);
    }
}
