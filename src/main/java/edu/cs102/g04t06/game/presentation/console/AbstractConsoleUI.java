package edu.cs102.g04t06.game.presentation.console;

import java.util.Scanner;

/**
 * Abstract base class for all console UI screens.
 * Provides shared utilities: screen clearing, sleeping, ANSI stripping,
 * error printing, and waiting for Enter — eliminating duplication across
 * LoadScreenUI, MainMenuUI, PlayerSetupUI, and GameBoardUI.
 */
public abstract class AbstractConsoleUI implements ThemeStyleSheet {

    protected final Scanner scanner;

    /** Default constructor — reads from standard input. */
    protected AbstractConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    /** Constructor for injecting a custom scanner (e.g. in tests). */
    protected AbstractConsoleUI(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Clears the terminal using ANSI escape codes.
     */
    protected void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    /**
     * Pauses execution for the given number of milliseconds.
     *
     * @param ms duration in milliseconds
     */
    protected void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Strips ANSI escape sequences from a string so its visible length
     * can be measured for padding calculations.
     *
     * @param s the string to strip
     * @return the plain-text version of the string
     */
    protected String stripAnsi(String s) {
        return s.replaceAll(ANSI_REGEX, "");
    }

    /**
     * Prints a styled error message prefixed with a cross symbol.
     *
     * @param msg the error message to display
     */
    protected void printError(String msg) {
        System.out.println();
        System.out.println(RED + "  \u2716  " + msg + RESET);
    }

    /**
     * Blocks until the user submits an empty line (Enter only).
     */
    protected void waitForEnter() {
        while (true) {
            String input = scanner.nextLine();
            if (input.isBlank()) {
                return;
            }
            System.out.println(RED + "  Please press Enter only to continue." + RESET);
            System.out.print(GREEN + "  > " + RESET);
        }
    }
}
