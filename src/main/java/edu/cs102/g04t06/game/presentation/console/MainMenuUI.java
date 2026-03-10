package edu.cs102.g04t06.game.presentation.console;

import java.util.Scanner;

/**
 * MainMenuUI
 *
 * Displays the main menu after the load screen.
 * Presents three options: New Game, Load Game, and Quit.
 * Returns a MenuChoice enum so the caller (ConsoleUI / App) can
 * decide what screen to navigate to next.
 */
public class MainMenuUI implements ThemeStyleSheet {

    // -------------------------------------------------------------------------
    // ASCII Art Title (smaller than load screen, suits a menu)
    // -------------------------------------------------------------------------
    private static final String[] TITLE_ART = {
        "  ███████╗██████╗ ██╗     ███████╗███╗   ██╗██████╗  ██████╗ ██████╗  ",
        "  ██╔════╝██╔══██╗██║     ██╔════╝████╗  ██║██╔══██╗██╔═══██╗██╔══██╗ ",
        "  ███████╗██████╔╝██║     █████╗  ██╔██╗ ██║██║  ██║██║   ██║██████╔╝ ",
        "  ╚════██║██╔═══╝ ██║     ██╔══╝  ██║╚██╗██║██║  ██║██║   ██║██╔══██╗ ",
        "  ███████║██║     ███████╗███████╗██║ ╚████║██████╔╝╚██████╔╝██║  ██║ ",
        "  ╚══════╝╚═╝     ╚══════╝╚══════╝╚═╝  ╚═══╝╚═════╝  ╚═════╝ ╚═╝  ╚═╝ "
    };

    private static final int    BOX_WIDTH = 30;
    private static final String VERSION   = "v1.0.0";
    private final Scanner scanner = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // Menu choice — returned to the caller after the user picks an option
    // -------------------------------------------------------------------------
    public enum MenuChoice {
        NEW_GAME,
        LOAD_GAME,
        QUIT
    }

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    /**
     * Displays the main menu and blocks until the user enters a valid option.
     *
     * @return the MenuChoice corresponding to what the user selected
     */
    public MenuChoice show() {
        while (true) {
            clearScreen();
            printTitle();
            printMenu();
            printFooter();

            char key = readChoice();

            switch (Character.toLowerCase(key)) {
                case 'n': return MenuChoice.NEW_GAME;
                case 'l': return MenuChoice.LOAD_GAME;
                case 'q': return MenuChoice.QUIT;
                default:
                    // Invalid key — just re-render the menu
                    printInvalidKey();
                    break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private rendering helpers
    // -------------------------------------------------------------------------

    private void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    private void printTitle() {
        System.out.println();
        for (String line : TITLE_ART) {
            System.out.println(GOLD + BOLD + line + RESET);
        }
        System.out.println();
    }

    /**
     * Renders the bordered menu box with all three options.
     * Each option is colour-coded to match the screenshot aesthetic:
     *   N → green, L → blue, Q → white/dim
     */
    private void printMenu() {
        String top    = "  ┌" + "─".repeat(BOX_WIDTH) + "┐";
        String div    = "  ├" + "─".repeat(BOX_WIDTH) + "┤";
        String bottom = "  └" + "─".repeat(BOX_WIDTH) + "┘";

        String title     = centreInBox("MAIN MENU", BOX_WIDTH);
        String newGame   = menuLine(GREEN,  "N", "New Game",   BOX_WIDTH);
        String loadGame  = menuLine(BLUE,   "L", "Load Game",  BOX_WIDTH);
        String quit      = menuLine(WHITE,  "Q", "Quit",       BOX_WIDTH);

        System.out.println(WHITE + top    + RESET);
        System.out.println(WHITE + "  │" + BOLD + WHITE + title   + RESET + WHITE + "│" + RESET);
        System.out.println(WHITE + div    + RESET);
        System.out.println(WHITE + "  │" + newGame  + WHITE + "│" + RESET);
        System.out.println(WHITE + "  │" + loadGame + WHITE + "│" + RESET);
        System.out.println(WHITE + "  │" + quit     + WHITE + "│" + RESET);
        System.out.println(WHITE + bottom + RESET);
        System.out.println();
    }

    private void printFooter() {
        System.out.println(DIM + WHITE + "  " + VERSION + RESET);
        System.out.println();
        System.out.print(WHITE + "  Enter choice and press Enter: " + RESET);
    }

    private void printInvalidKey() {
        System.out.println(RED + "  Invalid choice. Enter N, L, or Q then press Enter." + RESET);
        sleep(1000);
    }

    // -------------------------------------------------------------------------
    // Layout helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a single menu row, e.g.:  "  [ N ]  New Game           "
     * The key bracket is coloured, the label is white.
     */
    private String menuLine(String colour, String key, String label, int width) {
        String bracket = colour + BOLD + "[ " + key + " ]" + RESET;
        String text    = WHITE + "  " + label + RESET;

        // Calculate visible length for padding
        String visible = "  [ " + key + " ]  " + label;
        int padding    = width - visible.length();
        if (padding < 0) padding = 0;

        return "  " + bracket + text + " ".repeat(padding);
    }

    /**
     * Centres a plain string inside a box of the given inner width,
     * padding with spaces on both sides.
     */
    private String centreInBox(String text, int width) {
        int totalPad = width - text.length();
        int left     = totalPad / 2;
        int right    = totalPad - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    /**
     * Reads one line and returns the first non-whitespace character.
     * Returns '\0' when the line is empty.
     */
    private char readChoice() {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return '\0';
        }
        return input.charAt(0);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // -------------------------------------------------------------------------
    // Temporary main — remove once wired into App.java
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        MainMenuUI menu = new MainMenuUI();
        MenuChoice choice = menu.show();
        System.out.println("\nYou selected: " + choice);
    }
}
