package edu.cs102.g04t06.game.presentation.console;

/**
 * Renders the splash screen shown when the console application first launches.
 *
 * <p>This screen introduces the game with the title art, a short rules summary,
 * and a simple prompt that pauses the flow before the main menu is shown.</p>
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

    /**
     * Prints the large ASCII title art for the splash screen.
     */
    private void printTitle() {
        System.out.println();
        for (String line : TITLE_ART) {
            System.out.println(GOLD + BOLD + line + RESET);
        }
        System.out.println();
    }

    /**
     * Prints the horizontal divider separating the title from the description box.
     */
    private void printDivider() {
        System.out.println(DIM + WHITE + "  " + "─".repeat(BOX_WIDTH) + RESET);
        System.out.println();
    }

    /**
     * Prints the boxed game introduction and win-condition summary.
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

    /**
     * Prints the version label and the prompt that advances to the main menu.
     */
    private void printFooter() {
        System.out.println("  " + DIM + WHITE + VERSION + RESET);
        System.out.println();
        System.out.print(GREEN + BOLD + "  Press Enter to continue..." + RESET + " ");
    }
}
