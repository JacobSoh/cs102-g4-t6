package edu.cs102.g04t06.game.presentation.console;

import java.util.Scanner;

import edu.cs102.g04t06.App;

/**
 * Console-driven UI flow for the game.
 */
public class ConsoleUI {
    private static final int SLOT_COUNT = 4;

    private final App application;
    private final Scanner scanner = new Scanner(System.in);
    private final String[] slotNames = { "Player 1 (You)", null, null, null };
    private boolean modeOfPlay; // false = offline, true = online

    public ConsoleUI(App application) {
        this.application = application;
    }

    public void showOnBoarding() {
        while (true) {
            System.out.println();
            System.out.println("=== Splendor ===");
            System.out.println("1) Start Offline");
            System.out.println("2) Start Online");
            System.out.println("3) Exit");

            int choice = readInt("Choose option: ", 1, 3);
            if (choice == 1) {
                this.modeOfPlay = false;
                application.showLobby(false);
                return;
            }
            if (choice == 2) {
                this.modeOfPlay = true;
                application.showLobby(true);
                return;
            }
            exitProgram();
        }
    }

    public void showLobby(boolean modeOfPlay) {
        this.modeOfPlay = modeOfPlay;

        while (true) {
            System.out.println();
            System.out.println("=== Lobby (" + (this.modeOfPlay ? "Online" : "Offline") + ") ===");
            printSlots();
            System.out.println("1) Add NPC");
            System.out.println("2) Remove NPC");
            System.out.println("3) Start Game");
            System.out.println("4) Back to Main Menu");
            System.out.println("5) Exit");

            int choice = readInt("Choose option: ", 1, 5);
            switch (choice) {
                case 1 -> addNpc();
                case 2 -> removeNpc();
                case 3 -> {
                    application.showGame();
                    return;
                }
                case 4 -> {
                    application.showOnBoarding();
                    return;
                }
                case 5 -> exitProgram();
                default -> {
                    // No-op, guarded by readInt range validation.
                }
            }
        }
    }

    public void showGame() {
        while (true) {
            System.out.println();
            System.out.println("=== Game ===");
            System.out.println("Console mode placeholder: game logic can be wired here.");
            System.out.println("1) Back to Lobby");
            System.out.println("2) Exit");

            int choice = readInt("Choose option: ", 1, 2);
            if (choice == 1) {
                application.showLobby(this.modeOfPlay);
                return;
            }
            exitProgram();
        }
    }

    private void printSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            String label = slotNames[i] == null ? "Waiting..." : slotNames[i];
            System.out.println((i + 1) + ". " + label);
        }
    }

    private void addNpc() {
        int slot = readInt("Add NPC to slot (2-4): ", 2, 4) - 1;
        if (slotNames[slot] != null) {
            System.out.println("Slot " + (slot + 1) + " is already occupied.");
            return;
        }
        slotNames[slot] = "NPC " + slot;
        System.out.println("Added " + slotNames[slot] + ".");
    }

    private void removeNpc() {
        int slot = readInt("Remove NPC from slot (2-4): ", 2, 4) - 1;
        if (slotNames[slot] == null) {
            System.out.println("Slot " + (slot + 1) + " is already empty.");
            return;
        }
        slotNames[slot] = null;
        System.out.println("Removed NPC from slot " + (slot + 1) + ".");
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Handled by retry message below.
            }
            System.out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    private void exitProgram() {
        System.out.println("Exiting Splendor.");
        System.exit(0);
    }
}
