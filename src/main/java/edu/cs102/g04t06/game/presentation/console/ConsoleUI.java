package edu.cs102.g04t06.game.presentation.console;

import edu.cs102.g04t06.App;
import edu.cs102.g04t06.game.presentation.console.MainMenuUI.MenuChoice;
import edu.cs102.g04t06.game.presentation.console.PlayerSetupUI.PlayerSetupResult;

/**
 * Routes console screens through one navigation flow.
 */
public class ConsoleUI {
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String RED   = "\u001B[31m";

    private enum Route {
        LOAD_SCREEN,
        MAIN_MENU,
        PLAYER_SETUP,
        GAME_BOARD,
        EXIT
    }

    @SuppressWarnings("unused")
    private final App application;
    private final LoadScreenUI loadScreenUI;
    private final MainMenuUI mainMenuUI;
    private final PlayerSetupUI playerSetupUI;
    private final GameBoardUI gameBoardUI;

    public ConsoleUI(App application) {
        this.application = application;
        this.loadScreenUI = new LoadScreenUI();
        this.mainMenuUI = new MainMenuUI();
        this.playerSetupUI = new PlayerSetupUI();
        this.gameBoardUI = new GameBoardUI();
    }

    /**
     * Primary app entry flow:
     * load screen -> main menu -> setup/new game -> game board.
     */
    public void showOnBoarding() {
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
            case NEW_GAME -> Route.PLAYER_SETUP;
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
        return Route.GAME_BOARD;
    }

    private Route handleGameBoard() {
        gameBoardUI.show();
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
}
