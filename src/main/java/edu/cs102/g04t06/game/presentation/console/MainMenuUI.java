package edu.cs102.g04t06.game.presentation.console;

/**
 * Renders the main menu for the console application.
 *
 * This screen presents the top-level navigation choices after the splash
 * screen, allowing the user to start an offline game, host a LAN session, join
 * a LAN session, or quit the application.
 */
public class MainMenuUI extends AbstractConsoleUI {

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

    /**
     * Available top-level menu actions.
     */
    public enum MenuChoice {
        OFFLINE_PLAY,
        HOST_LAN,
        JOIN_LAN,
        QUIT
    }

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
                case 'o': return MenuChoice.OFFLINE_PLAY;
                case 'h': return MenuChoice.HOST_LAN;
                case 'j': return MenuChoice.JOIN_LAN;
                case 'q': return MenuChoice.QUIT;
                default:
                    printInvalidKey();
                    break;
            }
        }
    }

    /**
     * Prints the title art shown above the menu options.
     */
    private void printTitle() {
        System.out.println();
        for (String line : TITLE_ART) {
            System.out.println(GOLD + BOLD + line + RESET);
        }
        System.out.println();
    }

    /**
     * Prints the boxed list of available top-level menu options.
     */
    private void printMenu() {
        String top    = "  ┌" + "─".repeat(BOX_WIDTH) + "┐";
        String div    = "  ├" + "─".repeat(BOX_WIDTH) + "┤";
        String bottom = "  └" + "─".repeat(BOX_WIDTH) + "┘";

        String title       = centreInBox("MAIN MENU", BOX_WIDTH);
        String offlinePlay = menuLine(GREEN,  "O", "Offline Play", BOX_WIDTH);
        String hostLan     = menuLine(CYAN,   "H", "Host LAN",    BOX_WIDTH);
        String joinLan     = menuLine(PURPLE, "J", "Join LAN",    BOX_WIDTH);
        String quit        = menuLine(WHITE,  "Q", "Quit",        BOX_WIDTH);

        System.out.println(WHITE + top    + RESET);
        System.out.println(WHITE + "  │" + BOLD + WHITE + title + RESET + WHITE + "│" + RESET);
        System.out.println(WHITE + div    + RESET);
        System.out.println(WHITE + "  │" + offlinePlay + WHITE + "│" + RESET);
        System.out.println(WHITE + "  │" + hostLan     + WHITE + "│" + RESET);
        System.out.println(WHITE + "  │" + joinLan     + WHITE + "│" + RESET);
        System.out.println(WHITE + "  │" + quit        + WHITE + "│" + RESET);
        System.out.println(WHITE + bottom + RESET);
        System.out.println();
    }

    /**
     * Prints the version label and the prompt asking the user for a menu choice.
     */
    private void printFooter() {
        System.out.println(DIM + WHITE + "  " + VERSION + RESET);
        System.out.println();
        System.out.print(WHITE + "  Enter choice and press Enter: " + RESET);
    }

    /**
     * Shows the validation message for an unsupported menu key.
     */
    private void printInvalidKey() {
        System.out.println(RED + "  Invalid choice. Enter O, H, J, or Q then press Enter." + RESET);
        sleep(1000);
    }

    /**
     * Builds a single formatted menu option row.
     *
     * @param colour accent color for the hotkey badge
     * @param key shortcut key displayed to the user
     * @param label human-readable option label
     * @param width visible width of the boxed menu row
     * @return the formatted menu row content
     */
    private String menuLine(String colour, String key, String label, int width) {
        String bracket = colour + BOLD + "[ " + key + " ]" + RESET;
        String text    = WHITE + "  " + label + RESET;
        String visible = "  [ " + key + " ]  " + label;
        int padding    = width - visible.length();
        if (padding < 0) padding = 0;
        return "  " + bracket + text + " ".repeat(padding);
    }

    /**
     * Centers plain text within a fixed-width menu box row.
     *
     * @param text the text to center
     * @param width the visible width available for the row
     * @return centered text padded with spaces
     */
    private String centreInBox(String text, int width) {
        int totalPad = width - text.length();
        int left     = totalPad / 2;
        int right    = totalPad - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    /**
     * Reads the next menu input line and returns its first character.
     *
     * @return the selected key, or {@code '\0'} when the line is empty
     */
    private char readChoice() {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return '\0';
        return input.charAt(0);
    }
}
