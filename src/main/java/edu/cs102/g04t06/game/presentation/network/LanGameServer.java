package edu.cs102.g04t06.game.presentation.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.cs102.g04t06.game.execution.GameStateFactory;
import edu.cs102.g04t06.game.execution.TurnProcessor;
import edu.cs102.g04t06.game.presentation.console.GameBoardUI;
import edu.cs102.g04t06.game.presentation.console.ThemeStyleSheet;
import edu.cs102.g04t06.game.rules.GameState;

/**
 * Host-side LAN server that owns the authoritative game state.
 */
public class LanGameServer implements ThemeStyleSheet {
    private final int port;
    private final int totalPlayers;
    private final String hostPlayerName;
    private final int hostPlayerAge;
    private final GameBoardUI boardUI = new GameBoardUI();
    private final TurnProcessor turnProcessor = new TurnProcessor();
    private final GameStateFactory gameStateFactory = new GameStateFactory();
    private final List<ClientConnection> clients = new ArrayList<>();
    private final List<String> globalLog = new ArrayList<>();
    private String lastActionMessage = "No actions yet";
    private String finalGameMessage = "Match complete.";
    private String hostInlineError = "";

    public LanGameServer(int port, int totalPlayers, String hostPlayerName, int hostPlayerAge) {
        this.port = port;
        this.totalPlayers = totalPlayers;
        this.hostPlayerName = hostPlayerName;
        this.hostPlayerAge = hostPlayerAge;
        this.boardUI.setPerspectivePlayerName(hostPlayerName);
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(GREEN + "Hosting LAN game on port " + port + RESET);
            System.out.println(CYAN + "Waiting for " + (totalPlayers - 1) + " remote player(s)..." + RESET);
            acceptClients(serverSocket);

            GameState state = createInitialGameState(buildPlayerNames());
            appendGlobalLog("Game started.");
            broadcastState(MessageType.START_GAME, "Game started.", state);
            runGameLoop(state);
            closeClients();
        } catch (IOException e) {
            System.out.println(RED + "Failed to host LAN game: " + e.getMessage() + RESET);
        }
    }

    private void acceptClients(ServerSocket serverSocket) throws IOException {
        while (clients.size() < totalPlayers - 1) {
            Socket socket = serverSocket.accept();
            ClientConnection connection = createConnection(socket, clients.size() + 1);
            if (connection == null) {
                continue;
            }
            clients.add(connection);
            broadcastLobbyState();
        }
    }

    private ClientConnection createConnection(Socket socket, int playerIndex) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        NetworkMessage join = NetworkProtocol.read(reader);
        if (join == null || join.playerName == null || join.playerName.isBlank()) {
            socket.close();
            return null;
        }

        String requestedName = join.playerName.trim();
        if (join.type == MessageType.CHECK_NAME) {
            if (isDuplicatePlayerName(requestedName)) {
                NetworkProtocol.send(writer, NetworkMessage.of(
                        MessageType.ERROR,
                        "Player name already exists. Choose a different name."));
            } else {
                NetworkProtocol.send(writer, NetworkMessage.of(
                        MessageType.INFO,
                        "Player name is available."));
            }
            socket.close();
            return null;
        }

        if (join.type != MessageType.JOIN_REQUEST || join.playerAge == null) {
            socket.close();
            return null;
        }

        if (isDuplicatePlayerName(requestedName)) {
            NetworkProtocol.send(writer, NetworkMessage.of(
                    MessageType.ERROR,
                    "Player name already exists. Choose a different name."));
            socket.close();
            return null;
        }

        ClientConnection connection = new ClientConnection(
                socket, reader, writer, requestedName, join.playerAge, playerIndex);
        NetworkMessage accepted = NetworkMessage.of(MessageType.JOIN_ACCEPTED, "Joined lobby as " + connection.playerName + ".");
        accepted.playerIndex = playerIndex;
        accepted.expectedPlayers = totalPlayers;
        NetworkProtocol.send(writer, accepted);

        System.out.println(GREEN + connection.playerName + " joined from "
                + socket.getInetAddress().getHostAddress() + RESET);
        return connection;
    }

    private void broadcastLobbyState() {
        NetworkMessage lobby = NetworkMessage.of(
                MessageType.LOBBY_STATE,
                "Lobby ready: " + (clients.size() + 1) + "/" + totalPlayers + " players connected.");
        lobby.players = buildPlayerNames();
        lobby.expectedPlayers = totalPlayers;
        broadcast(lobby);
    }

    private void runGameLoop(GameState state) {
        while (!state.isGameOver()) {
            String currentPlayerName = state.getCurrentPlayer().getName();
            if (hostPlayerName.equals(currentPlayerName)) {
                handleHostTurn(state);
            } else {
                ClientConnection activeClient = findClientByName(currentPlayerName);
                if (activeClient == null) {
                    state.setGameOver(true);
                    finalGameMessage = "Game ended: missing client connection for " + currentPlayerName + ".";
                    appendGlobalLog(finalGameMessage);
                    break;
                }
                try {
                    handleRemoteTurn(state, activeClient);
                } catch (IOException e) {
                    handleDisconnect(state, activeClient, e.getMessage());
                }
            }
        }

        NetworkMessage gameOver = NetworkMessage.of(MessageType.GAME_OVER, finalGameMessage);
        gameOver.state = state;
        gameOver.logEntries = getRecentGlobalLog();
        broadcast(gameOver);
        boardUI.displayReadOnlyState(state, finalGameMessage, getRecentGlobalLog());
        System.out.println();
        System.out.println(GOLD + BOLD + finalGameMessage + RESET);
    }

    private void handleHostTurn(GameState state) {
        while (!state.isGameOver()) {
            String input = boardUI.promptNetworkTurn(
                    state,
                    hostInlineError,
                    RED + BOLD,
                    getRecentGlobalLog());
            hostInlineError = "";

            TurnProcessor.TurnResult result = turnProcessor.processCommand(state, input);
            if (!result.isSuccess()) {
                hostInlineError = result.getMessage();
                continue;
            }

            if (result.isAwaitingReturn()) {
                if (!handleHostGemReturn(state, result)) {
                    continue;
                }
            } else {
                broadcastTurnOutcome(state, hostPlayerName, result.getMessage());
            }
            break;
        }
    }

    private boolean handleHostGemReturn(GameState state, TurnProcessor.TurnResult initialResult) {
        while (true) {
            String statusMessage = hostInlineError;
            if (statusMessage == null || statusMessage.isBlank()) {
                statusMessage = "Return " + initialResult.getExcessCount() + " gem(s).";
            }
            String statusColor = (hostInlineError == null || hostInlineError.isBlank()) ? YELLOW : RED + BOLD;
            String input = boardUI.promptNetworkTurn(
                    state,
                    statusMessage,
                    statusColor,
                    getRecentGlobalLog());
            hostInlineError = "";
            TurnProcessor.TurnResult result = turnProcessor.processReturnGems(state, input);
            if (!result.isSuccess()) {
                hostInlineError = result.getMessage();
                continue;
            }
            broadcastTurnOutcome(state, hostPlayerName, result.getMessage());
            return true;
        }
    }

    private void handleRemoteTurn(GameState state, ClientConnection connection) throws IOException {
        broadcastPassiveState(
                "Waiting for " + connection.playerName + " to play.",
                state,
                connection.playerName);

        while (!state.isGameOver()) {
            NetworkMessage request = NetworkMessage.of(MessageType.REQUEST_COMMAND, "Your turn.");
            request.state = state;
            request.logMessage = lastActionMessage;
            request.logEntries = getRecentGlobalLog();
            NetworkProtocol.send(connection.writer, request);

            NetworkMessage reply = NetworkProtocol.read(connection.reader);
            if (reply == null) {
                throw new IOException(connection.playerName + " disconnected.");
            }
            if (reply.type != MessageType.MOVE_SUBMIT) {
                sendError(connection, state, "Expected a move command.");
                continue;
            }

            TurnProcessor.TurnResult result = turnProcessor.processCommand(state, reply.command);
            if (!result.isSuccess()) {
                sendError(connection, state, result.getMessage());
                continue;
            }

            if (result.isAwaitingReturn()) {
                if (!handleRemoteGemReturn(state, connection, result)) {
                    continue;
                }
            } else {
                broadcastTurnOutcome(state, connection.playerName, result.getMessage());
            }
            break;
        }
    }

    private boolean handleRemoteGemReturn(GameState state, ClientConnection connection,
            TurnProcessor.TurnResult initialResult) throws IOException {
        while (true) {
            NetworkMessage prompt = NetworkMessage.of(MessageType.REQUEST_RETURN_GEMS, initialResult.getMessage());
            prompt.excessCount = initialResult.getExcessCount();
            prompt.state = state;
            NetworkProtocol.send(connection.writer, prompt);

            NetworkMessage reply = NetworkProtocol.read(connection.reader);
            if (reply == null) {
                throw new IOException(connection.playerName + " disconnected.");
            }
            if (reply.type != MessageType.RETURN_GEMS) {
                sendError(connection, state, "Expected gem return input.");
                continue;
            }

            TurnProcessor.TurnResult result = turnProcessor.processReturnGems(state, reply.command);
            if (!result.isSuccess()) {
                sendError(connection, state, result.getMessage());
                continue;
            }

            broadcastTurnOutcome(state, connection.playerName, result.getMessage());
            return true;
        }
    }

    private void sendError(ClientConnection connection, GameState state, String message) {
        NetworkMessage error = NetworkMessage.of(MessageType.ERROR, message);
        error.state = state;
        NetworkProtocol.send(connection.writer, error);
    }

    private void handleDisconnect(GameState state, ClientConnection connection, String reason) {
        clients.remove(connection);
        closeClient(connection);

        String message = connection.playerName + " disconnected.";
        if (reason != null && !reason.isBlank()) {
            message += " " + reason;
        }

        NetworkMessage disconnected = NetworkMessage.of(MessageType.PLAYER_DISCONNECTED, message);
        disconnected.state = state;
        disconnected.logMessage = "Player disconnected, ending game.";
        appendGlobalLog(message);
        disconnected.logEntries = getRecentGlobalLog();
        broadcast(disconnected);

        state.setGameOver(true);

        String endMessage = "Game ended: insufficient players after " + connection.playerName + " disconnected.";
        finalGameMessage = endMessage;
        appendGlobalLog(endMessage);
        NetworkMessage gameOver = NetworkMessage.of(MessageType.GAME_OVER, endMessage);
        gameOver.state = state;
        gameOver.logEntries = getRecentGlobalLog();
        broadcast(gameOver);

        boardUI.displayReadOnlyState(state, endMessage, getRecentGlobalLog());
        System.out.println();
        System.out.println(RED + endMessage + RESET);
    }

    private void broadcastState(MessageType type, String message, GameState state) {
        if (message != null && !message.isBlank()) {
            lastActionMessage = message;
        }
        NetworkMessage payload = NetworkMessage.of(type, message);
        payload.logMessage = lastActionMessage;
        payload.logEntries = getRecentGlobalLog();
        payload.state = state;
        broadcast(payload);
        renderHostState(state);
    }

    private void broadcastTurnOutcome(GameState state, String actorName, String actorMessage) {
        String publicMessage = actorName + ": " + actorMessage;
        lastActionMessage = publicMessage;
        appendGlobalLog(publicMessage);
        String nextPlayerName = state.isGameOver() ? null : state.getCurrentPlayer().getName();

        for (ClientConnection client : clients) {
            if (nextPlayerName != null && client.playerName.equals(nextPlayerName)) {
                continue;
            }
            NetworkMessage payload = NetworkMessage.of(
                    MessageType.GAME_STATE,
                    client.playerName.equals(actorName) ? actorMessage : publicMessage);
            payload.logMessage = publicMessage;
            payload.logEntries = getRecentGlobalLog();
            payload.state = state;
            NetworkProtocol.send(client.writer, payload);
        }

        if (state.isGameOver() || state.getCurrentPlayerIndex() != 0) {
            boardUI.displayReadOnlyState(
                    state,
                    state.isGameOver() ? finalGameMessage
                            : "Waiting for " + state.getCurrentPlayer().getName() + " to play.",
                    getRecentGlobalLog());
        }
    }

    private void broadcastPassiveState(String message, GameState state, String activePlayerName) {
        for (ClientConnection client : clients) {
            if (client.playerName.equals(activePlayerName)) {
                continue;
            }
            NetworkMessage payload = NetworkMessage.of(MessageType.GAME_STATE, message);
            payload.logMessage = lastActionMessage;
            payload.logEntries = getRecentGlobalLog();
            payload.state = state;
            NetworkProtocol.send(client.writer, payload);
        }
    }

    private void renderHostState(GameState state) {
        if (state == null) {
            return;
        }
        if (!state.isGameOver() && state.getCurrentPlayerIndex() == 0) {
            return;
        }
        String statusMessage = state.isGameOver()
                ? finalGameMessage
                : "Waiting for " + state.getCurrentPlayer().getName() + " to play.";
        boardUI.displayReadOnlyState(state, statusMessage, getRecentGlobalLog());
    }

    private void appendGlobalLog(String entry) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        globalLog.add(entry);
    }

    private List<String> getRecentGlobalLog() {
        if (globalLog.isEmpty()) {
            return List.of();
        }
        return List.of(globalLog.get(globalLog.size() - 1));
    }

    private void broadcast(NetworkMessage message) {
        for (ClientConnection client : clients) {
            NetworkProtocol.send(client.writer, message);
        }
    }

    private List<String> buildPlayerNames() {
        List<PlayerSlot> slots = new ArrayList<>();
        slots.add(new PlayerSlot(hostPlayerName, hostPlayerAge, 0));
        for (ClientConnection client : clients) {
            slots.add(new PlayerSlot(client.playerName, client.playerAge, client.playerIndex));
        }
        slots.sort(Comparator
                .comparingInt(PlayerSlot::age)
                .thenComparingInt(PlayerSlot::joinOrder));

        List<String> names = new ArrayList<>();
        for (PlayerSlot slot : slots) {
            names.add(slot.name());
        }
        return names;
    }

    private GameState createInitialGameState(List<String> playerNames) {
        return gameStateFactory.createInitialGameState(totalPlayers, playerNames);
    }

    private boolean isDuplicatePlayerName(String candidateName) {
        if (hostPlayerName.equalsIgnoreCase(candidateName)) {
            return true;
        }
        for (ClientConnection client : clients) {
            if (client.playerName.equalsIgnoreCase(candidateName)) {
                return true;
            }
        }
        return false;
    }

    private ClientConnection findClientByName(String playerName) {
        for (ClientConnection client : clients) {
            if (client.playerName.equals(playerName)) {
                return client;
            }
        }
        return null;
    }

    private void closeClients() {
        for (ClientConnection client : clients) {
            closeClient(client);
        }
    }

    private void closeClient(ClientConnection client) {
        try {
            client.socket.close();
        } catch (IOException ignored) {
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class ClientConnection {
        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;
        private final String playerName;
        private final int playerAge;
        private final int playerIndex;

        private ClientConnection(Socket socket, BufferedReader reader, PrintWriter writer,
                String playerName, int playerAge, int playerIndex) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
            this.playerName = playerName;
            this.playerAge = playerAge;
            this.playerIndex = playerIndex;
        }
    }

    private record PlayerSlot(String name, int age, int joinOrder) {}
}
