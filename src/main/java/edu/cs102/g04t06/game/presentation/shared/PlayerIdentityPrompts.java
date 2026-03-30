package edu.cs102.g04t06.game.presentation.shared;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import edu.cs102.g04t06.game.presentation.console.ThemeStyleSheet;

/**
 * Shared prompts for player identity fields used across offline and LAN setup.
 */
public class PlayerIdentityPrompts implements ThemeStyleSheet {

    private final Scanner scanner;

    /**
     * Creates a prompt helper backed by the provided scanner.
     *
     * @param scanner the scanner used to read user input
     */
    public PlayerIdentityPrompts(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prompts for a non-empty player name.
     *
     * @param label the field label to display
     * @return the validated player name
     */
    public String promptName(String label) {
        while (true) {
            System.out.print(WHITE + "  " + label + ": " + RESET);
            String name = scanner.nextLine().trim();

            if (name.isEmpty()) {
                printError("Name cannot be empty. Please try again.");
                continue;
            }
            if (name.length() > 20) {
                printError("Name must be 20 characters or fewer.");
                continue;
            }
            return name;
        }
    }

    /**
     * Prompts for a birthday and converts it into an age in years.
     *
     * @param playerName the player whose birthday is being entered
     * @return the validated age in years
     */
    public int promptBirthdayAsAge(String playerName) {
        while (true) {
            System.out.print(WHITE + "  Birthday for " + playerName
                    + DIM + " (YYYY-MM-DD): " + RESET);

            String input = scanner.nextLine().trim();

            try {
                LocalDate birthday = LocalDate.parse(input);
                LocalDate today = LocalDate.now();

                if (birthday.isAfter(today)) {
                    printError("Birthday cannot be in the future.");
                    continue;
                }

                int age = Period.between(birthday, today).getYears();
                if (age < 0 || age > 120) {
                    printError("Please enter a realistic birthday.");
                    continue;
                }

                return age;
            } catch (DateTimeParseException e) {
                printError("Please use the format YYYY-MM-DD.");
            }
        }
    }

    /**
     * Prompts for the total number of players in the match.
     *
     * @return the validated player count
     */
    public int promptTotalPlayers() {
        while (true) {
            System.out.println(WHITE + "  How many players total?"
                    + " " + DIM + "(2–4)" + RESET);
            System.out.println();
            System.out.print(WHITE + "  Total players (2-4): " + RESET);
            String value = scanner.nextLine().trim();
            try {
                int totalPlayers = Integer.parseInt(value);
                if (totalPlayers < 2 || totalPlayers > 4) {
                    throw new NumberFormatException();
                }
                return totalPlayers;
            } catch (NumberFormatException e) {
                printError("Please enter a number between 2 and 4.");
            }
        }
    }

    private void printError(String msg) {
        System.out.println(RED + "  ✖  " + msg + RESET);
    }
}
