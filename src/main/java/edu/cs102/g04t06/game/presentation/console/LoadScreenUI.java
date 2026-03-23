package edu.cs102.g04t06.game.presentation.console;

import java.util.Scanner;

/**
 * LoadScreenUI
 *
 * The splash/intro screen displayed when the game is first launched.
 * Shows the game title in ASCII art, a brief description of Splendor,
 * and waits for the user to press any key before proceeding to the Main Menu.
 */
public class LoadScreenUI implements ThemeStyleSheet {

    // -------------------------------------------------------------------------
    // Layout constants
    // -------------------------------------------------------------------------
    private static final int    BOX_WIDTH   = 72;   // minimum inner width of the panel
    private static final String VERSION     = "v1.0.0";
    private final Scanner scanner = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    /**
     * Displays the load screen and blocks until the user presses any key.
     * Call this once at application startup before showing the Main Menu.
     */
    public void show() {
        clearScreen();
        printTitle();
        printDivider();
        printDescriptionBox();
        printFooter();
        waitForEnter();
    }

    // -------------------------------------------------------------------------
    // Private rendering helpers
    // -------------------------------------------------------------------------

    /** Clears the terminal using ANSI escape codes. */
    private void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    /** Prints the ASCII art title in gold/amber. */
    private void printTitle() {
        System.out.println();
        for (String line : TITLE_ART) {
            System.out.println(GOLD + BOLD + line + RESET);
        }
        System.out.println();
    }

    /** Prints a thin horizontal divider line. */
    private void printDivider() {
        System.out.println(DIM + WHITE + "  " + "─".repeat(BOX_WIDTH) + RESET);
        System.out.println();
    }

    /**
     * Prints a bordered box containing the game description.
     * Edit the lines inside DESCRIPTION to change the blurb.
     */
    private void printDescriptionBox() {
        String[] description = {
            "  Welcome to SPLENDOR - a game of gem trading and card development.",
            "",
            "  Collect gems, acquire development cards, and attract noble patrons",
            "  to build the most prestigious jewellery empire.",
            "",
            "  Be the first player to reach " + GOLD + "15 prestige points" + WHITE + " to win."
        };
        int contentWidth = BOX_WIDTH;
        for (String line : description) {
            contentWidth = Math.max(contentWidth, stripAnsi(line).length() + 1);
        }

        // Top border
        System.out.println(WHITE + "  ┌" + "─".repeat(contentWidth) + "┐" + RESET);

        // Description lines
        for (String line : description) {
            int visibleLen = stripAnsi(line).length();
            int padding    = contentWidth - visibleLen - 1;
            if (padding < 0) padding = 0;
            System.out.println(WHITE + "  │ " + RESET + WHITE + line
                    + " ".repeat(padding) + WHITE + "│" + RESET);
        }

        // Bottom border
        System.out.println(WHITE + "  └" + "─".repeat(contentWidth) + "┘" + RESET);
        System.out.println();
    }

    /** Prints the version number and the "press Enter" prompt. */
    private void printFooter() {
        String versionStr  = DIM + WHITE + VERSION + RESET;
        String promptStr   = GREEN + BOLD + "  Press Enter to continue..." + RESET;

        System.out.println("  " + versionStr);
        System.out.println();
        System.out.print(promptStr + " ");
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    /** Blocks until the user submits an empty line (Enter only). */
    private void waitForEnter() {
        while (true) {
            String input = scanner.nextLine();
            if (input.isBlank()) {
                return;
            }
            System.out.println(RED + "  Please press Enter only to continue." + RESET);
            System.out.print(GREEN + "  > " + RESET);
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Strips ANSI escape sequences from a string so we can measure its
     * visible length for padding calculations.
     */
    private String stripAnsi(String s) {
        return s.replaceAll(ANSI_REGEX, "");
    }

}
