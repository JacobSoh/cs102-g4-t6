package edu.cs102.g04t06.game.presentation.console;

/**
 * LoadScreenUI
 *
 * The splash/intro screen displayed when the game is first launched.
 * Shows the game title in ASCII art, a brief description of Splendor,
 * and waits for the user to press any key before proceeding to the Main Menu.
 */
public class LoadScreenUI extends AbstractConsoleUI {

    private static final int    BOX_WIDTH = 72;
    private static final String VERSION   = "v1.0.0";

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

    private void printTitle() {
        System.out.println();
        for (String line : TITLE_ART) {
            System.out.println(GOLD + BOLD + line + RESET);
        }
        System.out.println();
    }

    private void printDivider() {
        System.out.println(DIM + WHITE + "  " + "─".repeat(BOX_WIDTH) + RESET);
        System.out.println();
    }

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

        System.out.println(WHITE + "  ┌" + "─".repeat(contentWidth) + "┐" + RESET);
        for (String line : description) {
            int visibleLen = stripAnsi(line).length();
            int padding    = contentWidth - visibleLen - 1;
            if (padding < 0) padding = 0;
            System.out.println(WHITE + "  │ " + RESET + WHITE + line
                    + " ".repeat(padding) + WHITE + "│" + RESET);
        }
        System.out.println(WHITE + "  └" + "─".repeat(contentWidth) + "┘" + RESET);
        System.out.println();
    }

    private void printFooter() {
        System.out.println("  " + DIM + WHITE + VERSION + RESET);
        System.out.println();
        System.out.print(GREEN + BOLD + "  Press Enter to continue..." + RESET + " ");
    }
}
