package edu.cs102.g04t06.game.presentation.console;

import java.io.IOException;

/**
 * LoadScreenUI
 *
 * The splash/intro screen displayed when the game is first launched.
 * Shows the game title in ASCII art, a brief description of Splendor,
 * and waits for the user to press any key before proceeding to the Main Menu.
 */
public class LoadScreenUI {

    // -------------------------------------------------------------------------
    // ANSI Colour Codes
    // -------------------------------------------------------------------------
    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String GOLD    = "\u001B[38;5;220m";   // amber/gold title
    private static final String WHITE   = "\u001B[37m";
    private static final String DIM     = "\u001B[2m";
    private static final String CYAN    = "\u001B[36m";
    private static final String GREEN   = "\u001B[32m";

    // -------------------------------------------------------------------------
    // ASCII Art Title
    // -------------------------------------------------------------------------
    private static final String[] TITLE_ART = {
        "  ███████╗██████╗ ██╗     ███████╗███╗   ██╗██████╗  ██████╗ ██████╗  ",
        "  ██╔════╝██╔══██╗██║     ██╔════╝████╗  ██║██╔══██╗██╔═══██╗██╔══██╗ ",
        "  ███████╗██████╔╝██║     █████╗  ██╔██╗ ██║██║  ██║██║   ██║██████╔╝ ",
        "  ╚════██║██╔═══╝ ██║     ██╔══╝  ██║╚██╗██║██║  ██║██║   ██║██╔══██╗ ",
        "  ███████║██║     ███████╗███████╗██║ ╚████║██████╔╝╚██████╔╝██║  ██║ ",
        "  ╚══════╝╚═╝     ╚══════╝╚══════╝╚═╝  ╚═══╝╚═════╝  ╚═════╝ ╚═╝  ╚═╝ "
    };

    // -------------------------------------------------------------------------
    // Layout constants
    // -------------------------------------------------------------------------
    private static final int    BOX_WIDTH   = 72;   // inner width of the panel
    private static final String VERSION     = "v1.0.0";

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
        waitForKeyPress();
    }

    // -------------------------------------------------------------------------
    // Private rendering helpers
    // -------------------------------------------------------------------------

    /** Clears the terminal using ANSI escape codes. */
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
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
            "  Welcome to SPLENDOR — a game of gem trading and card development.",
            "",
            "  Collect gems, acquire development cards, and attract noble patrons",
            "  to build the most prestigious jewellery empire.",
            "",
            "  Be the first player to reach " + GOLD + "15 prestige points" + WHITE + " to win."
        };

        // Top border
        System.out.println(WHITE + "  ┌" + "─".repeat(BOX_WIDTH) + "┐" + RESET);

        // Description lines
        for (String line : description) {
            int visibleLen = stripAnsi(line).length();
            int padding    = BOX_WIDTH - visibleLen - 2; // -2 for leading spaces
            if (padding < 0) padding = 0;
            System.out.println(WHITE + "  │ " + RESET + WHITE + line
                    + " ".repeat(padding) + "│" + RESET);
        }

        // Bottom border
        System.out.println(WHITE + "  └" + "─".repeat(BOX_WIDTH) + "┘" + RESET);
        System.out.println();
    }

    /** Prints the version number and the "press any key" prompt. */
    private void printFooter() {
        String versionStr  = DIM + WHITE + VERSION + RESET;
        String promptStr   = GREEN + BOLD + "  Press any key to continue..." + RESET;

        System.out.println("  " + versionStr);
        System.out.println();
        System.out.println(promptStr);
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    /** Blocks until the user presses any key, then returns. */
    private void waitForKeyPress() {
        try {
            // Switch terminal to raw mode so we don't need Enter pressed
            new ProcessBuilder("sh", "-c", "stty raw -echo </dev/tty")
                    .inheritIO().start().waitFor();
            System.in.read(); // wait for single keypress
        } catch (IOException | InterruptedException e) {
            // Fallback: just wait for Enter if raw mode unavailable
            try { System.in.read(); }
            catch (IOException ex) { Thread.currentThread().interrupt(); }
        } finally {
            // Restore terminal to normal cooked mode
            try {
                new ProcessBuilder("sh", "-c", "stty sane </dev/tty")
                        .inheritIO().start().waitFor();
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    // -------------------------------------------------------------------------
    // Temporary main — remove once wired into App.java
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
    new LoadScreenUI().show();
    }
}
