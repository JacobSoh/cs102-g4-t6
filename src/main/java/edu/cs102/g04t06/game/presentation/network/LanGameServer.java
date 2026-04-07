package edu.cs102.g04t06.game.presentation.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import edu.cs102.g04t06.game.execution.GameStateFactory;
import edu.cs102.g04t06.game.execution.GameEngine;
import edu.cs102.g04t06.game.presentation.console.GameBoardUI;
import edu.cs102.g04t06.game.presentation.console.ThemeStyleSheet;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Noble;

/**
 * Host-side LAN server that owns the authoritative game state.
 */
public class LanGameServer implements ThemeStyleSheet {
    private final int port;
    private final int totalPlayers;
    private final String hostPlayerName;
    private final int hostPlayerAge;
    private final GameBoardUI boardUI = new GameBoardUI();
    private final GameEngine gameEngine = new GameEngine();
    private final GameStateFactory gameStateFactory = new GameStateFactory();
    private final List<ClientConnection> clients = new ArrayList<>();
    private final List<String> globalLog = new ArrayList<>();
    private String lastActionMessage = "No actions yet";
    private String finalGameMessage = "Match complete.";
    private String hostInlineError = "";
    private String hostPendingTurnMessage = "";

    /**
     * Creates the authoritative LAN host controller.
     *
     * @param port the local port to listen on
     * @param totalPlayers the total number of players expected in the match
     * @param hostPlayerName the host player's display name
     * @param hostPlayerAge the host player's age used for turn ordering
     */
    public LanGameServer(int port, int totalPlayers, String hostPlayerName, int hostPlayerAge) {
        this.port = port;
        this.totalPlayers = totalPlayers;
        this.hostPlayerName = hostPlayerName;
        this.hostPlayerAge = hostPlayerAge;
        this.boardUI.setPerspectivePlayerName(hostPlayerName);
    }

    /**
     * Starts the host-side server, accepts clients, and runs the authoritative game loop.
     */
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(GREEN + "Hosting LAN game on port " + port + RESET);
            String hostIpAddress = resolveHostIpv4();
            if (hostIpAddress != null) {
                System.out.println(CYAN + "Host IP: " + hostIpAddress + RESET);
            }
            System.out.println(CYAN + "Waiting for " + (totalPlayers - 1) + " remote player(s)..." + RESET);
            acceptClients(serverSocket);

            GameState state = createInitialGameState(buildPlayerNames());
            appendGlobalLog("Game started.");
            broadcastState(MessageType.START_GAME, "Game started.", state);
            runGameLoop(state);
            closeClients();
        } catch (IOException e) {
            System.out.println(RED + "Failed to host LAN game: " + e.getMessage() + RESET);
            System.out.println("Press Enter to return to the main menu...");
            try {
                System.in.read();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Accepts remote players until the requested lobby size has been reached.
     *
     * @param serverSocket the listening server socket
     * @throws IOException if accepting a client fails
     */
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

    /**
     * Validates an incoming socket and turns it into a tracked client connection.
     *
     * @param socket the newly accepted socket
     * @param playerIndex the provisional join order for the client
     * @return the established connection, or {@code null} when the socket was only a probe or was rejected
     * @throws IOException if the handshake cannot be completed
     */
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
            writer.flush();
            try {
                socket.shutdownOutput();
            } catch (IOException ignored) {
                // Some platforms may not support half-close cleanly.
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

    /**
     * Broadcasts the current lobby occupancy and ordered player names.
     */
    private void broadcastLobbyState() {
        NetworkMessage lobby = NetworkMessage.of(
                MessageType.LOBBY_STATE,
                "Lobby ready: " + (clients.size() + 1) + "/" + totalPlayers + " players connected.");
        lobby.players = buildPlayerNames();
        lobby.expectedPlayers = totalPlayers;
        broadcast(lobby);
    }

    /**
     * Runs the authoritative turn loop until the game ends.
     *
     * @param state the shared game state owned by the host
     */
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
                if (activeClient.disconnected) {
                    autoResolveDisconnectedTurn(state, currentPlayerName);
                    continue;
                }
                try {
                    handleRemoteTurn(state, activeClient);
                } catch (IOException e) {
                    handleDisconnect(state, activeClient, e.getMessage());
                    autoResolveDisconnectedTurn(state, currentPlayerName);
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

    /**
     * Processes one complete host-controlled turn, including follow-up prompts.
     *
     * @param state the shared game state
     */
    private void handleHostTurn(GameState state) {
        while (!state.isGameOver()) {
            String input = boardUI.promptNetworkTurn(
                    state,
                    hostInlineError,
                    RED + BOLD,
                    getRecentGlobalLog());
            hostInlineError = "";
            if (isDisconnectCommand(input)) {
                handleDisconnect(state, null, "Host quit the session.");
                return;
            }

            GameEngine.TurnResult result = gameEngine.processPlayerCommand(state, input);
            if (!result.isSuccess()) {
                hostInlineError = result.getMessage();
                continue;
            }

            if (result.isAwaitingReturn()) {
                hostPendingTurnMessage = result.getMessage();
                if (!handleHostGemReturn(state, result)) {
                    continue;
                }
            } else if (result.isAwaitingNobleSelection()) {
                hostPendingTurnMessage = result.getMessage();
                if (!handleHostNobleSelection(state, result)) {
                    continue;
                }
            } else {
                hostPendingTurnMessage = "";
                broadcastTurnOutcome(state, hostPlayerName, result.getMessage());
            }
            break;
        }
    }

    /**
     * Resolves the host's mandatory gem-return flow.
     *
     * @param state the shared game state
     * @param initialResult the turn result that triggered gem return
     * @return {@code true} when the flow completed successfully
     */
    private boolean handleHostGemReturn(GameState state, GameEngine.TurnResult initialResult) {
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
            if (isDisconnectCommand(input)) {
                handleDisconnect(state, null, "Host quit the session.");
                return false;
            }
            GameEngine.TurnResult result = gameEngine.processGemReturn(state, input);
            if (!result.isSuccess()) {
                hostInlineError = result.getMessage();
                continue;
            }
            if (result.isAwaitingNobleSelection()) {
                hostPendingTurnMessage = result.getMessage();
                return handleHostNobleSelection(state, result);
            }
            hostPendingTurnMessage = "";
            broadcastTurnOutcome(state, hostPlayerName, result.getMessage());
            return true;
        }
    }

    /**
     * Resolves the host's mandatory noble-selection flow.
     *
     * @param state the shared game state
     * @param initialResult the turn result that triggered noble selection
     * @return {@code true} when the flow completed successfully
     */
    private boolean handleHostNobleSelection(GameState state, GameEngine.TurnResult initialResult) {
        while (true) {
            String statusMessage = hostInlineError;
            if (statusMessage == null || statusMessage.isBlank()) {
                statusMessage = formatNobleSelectionPrompt(initialResult.getClaimableNobles());
            }
            String statusColor = (hostInlineError == null || hostInlineError.isBlank()) ? YELLOW : RED + BOLD;
            String input = boardUI.promptNetworkTurn(
                    state,
                    statusMessage,
                    statusColor,
                    getRecentGlobalLog());
            hostInlineError = "";
            if (isDisconnectCommand(input)) {
                handleDisconnect(state, null, "Host quit the session.");
                return false;
            }
            GameEngine.TurnResult result = gameEngine.processNobleSelection(state, input, hostPendingTurnMessage);
            if (!result.isSuccess()) {
                hostInlineError = result.getMessage();
                continue;
            }
            hostPendingTurnMessage = "";
            broadcastTurnOutcome(state, hostPlayerName, result.getMessage());
            return true;
        }
    }

    /**
     * Drives a full turn for the currently active remote player.
     *
     * @param state the shared game state
     * @param connection the active remote player connection
     * @throws IOException if the remote client disconnects mid-turn
     */
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
            if (reply.type == MessageType.DISCONNECT_REQUEST) {
                handleDisconnect(state, connection, "Player quit the session.");
                return;
            }
            if (reply.type != MessageType.MOVE_SUBMIT) {
                sendError(connection, state, "Expected a move command.");
                continue;
            }

            GameEngine.TurnResult result = gameEngine.processPlayerCommand(state, reply.command);
            if (!result.isSuccess()) {
                sendError(connection, state, result.getMessage());
                continue;
            }

            if (result.isAwaitingReturn()) {
                if (!handleRemoteGemReturn(state, connection, result, result.getMessage())) {
                    continue;
                }
            } else if (result.isAwaitingNobleSelection()) {
                if (!handleRemoteNobleSelection(state, connection, result, result.getMessage())) {
                    continue;
                }
            } else {
                broadcastTurnOutcome(state, connection.playerName, result.getMessage());
            }
            break;
        }
    }

    /**
     * Resolves a remote player's mandatory gem-return flow.
     *
     * @param state the shared game state
     * @param connection the active remote player connection
     * @param initialResult the turn result that triggered gem return
     * @param pendingTurnMessage the pending public turn summary
     * @return {@code true} when the flow completed successfully
     * @throws IOException if the remote client disconnects mid-flow
     */
    private boolean handleRemoteGemReturn(GameState state, ClientConnection connection,
            GameEngine.TurnResult initialResult, String pendingTurnMessage) throws IOException {
        while (true) {
            NetworkMessage prompt = NetworkMessage.of(MessageType.REQUEST_RETURN_GEMS, initialResult.getMessage());
            prompt.excessCount = initialResult.getExcessCount();
            prompt.state = state;
            NetworkProtocol.send(connection.writer, prompt);

            NetworkMessage reply = NetworkProtocol.read(connection.reader);
            if (reply == null) {
                throw new IOException(connection.playerName + " disconnected.");
            }
            if (reply.type == MessageType.DISCONNECT_REQUEST) {
                handleDisconnect(state, connection, "Player quit the session.");
                return false;
            }
            if (reply.type != MessageType.RETURN_GEMS) {
                sendError(connection, state, "Expected gem return input.");
                continue;
            }

            GameEngine.TurnResult result = gameEngine.processGemReturn(state, reply.command);
            if (!result.isSuccess()) {
                sendError(connection, state, result.getMessage());
                continue;
            }
            if (result.isAwaitingNobleSelection()) {
                return handleRemoteNobleSelection(state, connection, result, result.getMessage());
            }
            broadcastTurnOutcome(state, connection.playerName, result.getMessage());
            return true;
        }
    }

    /**
     * Resolves a remote player's mandatory noble-selection flow.
     *
     * @param state the shared game state
     * @param connection the active remote player connection
     * @param initialResult the turn result that triggered noble selection
     * @param pendingTurnMessage the pending public turn summary
     * @return {@code true} when the flow completed successfully
     * @throws IOException if the remote client disconnects mid-flow
     */
    private boolean handleRemoteNobleSelection(GameState state, ClientConnection connection,
            GameEngine.TurnResult initialResult, String pendingTurnMessage) throws IOException {
        while (true) {
            NetworkMessage prompt = NetworkMessage.of(
                    MessageType.REQUEST_NOBLE_SELECTION,
                    formatNobleSelectionPrompt(initialResult.getClaimableNobles()));
            prompt.state = state;
            prompt.logEntries = getRecentGlobalLog();
            NetworkProtocol.send(connection.writer, prompt);

            NetworkMessage reply = NetworkProtocol.read(connection.reader);
            if (reply == null) {
                throw new IOException(connection.playerName + " disconnected.");
            }
            if (reply.type == MessageType.DISCONNECT_REQUEST) {
                handleDisconnect(state, connection, "Player quit the session.");
                return false;
            }
            if (reply.type != MessageType.NOBLE_SELECTION) {
                sendError(connection, state, "Expected a noble selection.");
                continue;
            }

            GameEngine.TurnResult result = gameEngine.processNobleSelection(state, reply.command, pendingTurnMessage);
            if (!result.isSuccess()) {
                sendError(connection, state, result.getMessage());
                continue;
            }

            broadcastTurnOutcome(state, connection.playerName, result.getMessage());
            return true;
        }
    }

    /**
     * Sends an error response to a connected client together with the current state snapshot.
     *
     * @param connection the client to notify
     * @param state the latest authoritative game state
     * @param message the error text to send
     */
    private void sendError(ClientConnection connection, GameState state, String message) {
        if (connection.disconnected) {
            return;
        }
        NetworkMessage error = NetworkMessage.of(MessageType.ERROR, message);
        error.state = state;
        NetworkProtocol.send(connection.writer, error);
    }

    /**
     * Marks a participant as disconnected and updates the session outcome accordingly.
     *
     * @param state the shared game state
     * @param connection the disconnected client, or {@code null} when the host quits
     * @param reason the human-readable disconnect reason
     */
    private void handleDisconnect(GameState state, ClientConnection connection, String reason) {
        if (connection == null) {
            String message = hostPlayerName + " disconnected.";
            if (reason != null && !reason.isBlank()) {
                message += " " + reason;
            }
            finalGameMessage = message;
            appendGlobalLog(message);
            state.setGameOver(true);            
            return;
        }
        if (connection.disconnected) {
            return;
        }
        connection.disconnected = true;
        closeClient(connection);

        if (totalPlayers == 2) {
            String message = connection.playerName + " disconnected. Ending the 2-player game.";
            if (reason != null && !reason.isBlank()) {
                message += " " + reason;
            }
            finalGameMessage = message;
            appendGlobalLog(message);
            state.setGameOver(true);
            return;
        }

        String message = connection.playerName + " disconnected.";
        if (reason != null && !reason.isBlank()) {
            message += " " + reason;
        }
        message += " Future turns will auto-pass.";

        NetworkMessage disconnected = NetworkMessage.of(MessageType.PLAYER_DISCONNECTED, message);
        disconnected.state = state;
        disconnected.logMessage = message;
        appendGlobalLog(message);
        disconnected.logEntries = getRecentGlobalLog();
        broadcast(disconnected);
    }

    /**
     * Broadcasts a stateful message to all connected clients and refreshes the host view.
     *
     * @param type the message type to broadcast
     * @param message the status text to attach
     * @param state the current authoritative game state
     */
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

    /**
     * Broadcasts the outcome of a completed turn to spectators and recently active players.
     *
     * @param state the current authoritative game state
     * @param actorName the player who performed the turn
     * @param actorMessage the turn result message produced by the engine
     */
    private void broadcastTurnOutcome(GameState state, String actorName, String actorMessage) {
        String publicMessage = actorName + ": " + actorMessage;
        lastActionMessage = publicMessage;
        appendGlobalLog(publicMessage);
        String nextPlayerName = state.isGameOver() ? null : state.getCurrentPlayer().getName();

        for (ClientConnection client : clients) {
            if (client.disconnected) {
                continue;
            }
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

        if (state.isGameOver() || !hostPlayerName.equals(state.getCurrentPlayer().getName())) {
            boardUI.displayReadOnlyState(
                    state,
                    state.isGameOver() ? finalGameMessage
                            : "Waiting for " + state.getCurrentPlayer().getName() + " to play.",
                    getRecentGlobalLog());
        }
    }

    /**
     * Broadcasts a passive waiting-state update to all non-active remote clients.
     *
     * @param message the waiting message to show
     * @param state the current authoritative game state
     * @param activePlayerName the player whose turn is in progress
     */
    private void broadcastPassiveState(String message, GameState state, String activePlayerName) {
        for (ClientConnection client : clients) {
            if (client.disconnected) {
                continue;
            }
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

    /**
     * Refreshes the host-side board when the host is not currently entering input.
     *
     * @param state the authoritative game state to display
     */
    private void renderHostState(GameState state) {
        if (state == null) {
            return;
        }
        if (!state.isGameOver() && hostPlayerName.equals(state.getCurrentPlayer().getName())) {
            return;
        }
        String statusMessage = state.isGameOver()
                ? finalGameMessage
                : "Waiting for " + state.getCurrentPlayer().getName() + " to play.";
        boardUI.displayReadOnlyState(state, statusMessage, getRecentGlobalLog());
    }

    /**
     * Determines whether the raw input requests leaving the LAN session.
     *
     * @param input the raw console input
     * @return {@code true} when the quit shortcut was entered
     */
    private boolean isDisconnectCommand(String input) {
        return input != null && input.equalsIgnoreCase("q");
    }

    /**
     * Appends a non-blank entry to the shared session log.
     *
     * @param entry the log entry to record
     */
    private void appendGlobalLog(String entry) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        globalLog.add(entry);
    }

    /**
     * Returns the most recent shared log entries shown in the LAN UI.
     *
     * @return up to the last three log entries
     */
    private List<String> getRecentGlobalLog() {
        if (globalLog.isEmpty()) {
            return List.of();
        }
        int start = Math.max(0, globalLog.size() - 3);
        return new ArrayList<>(globalLog.subList(start, globalLog.size()));
    }

    /**
     * Sends the given message to every currently connected client.
     *
     * @param message the message to broadcast
     */
    private void broadcast(NetworkMessage message) {
        for (ClientConnection client : clients) {
            if (client.disconnected) {
                continue;
            }
            NetworkProtocol.send(client.writer, message);
        }
    }

    /**
     * Formats the prompt shown when a player must choose one of several nobles.
     *
     * @param nobles the nobles available to claim
     * @return a numbered prompt string
     */
    private String formatNobleSelectionPrompt(List<Noble> nobles) {
        StringBuilder prompt = new StringBuilder("Choose noble");
        for (int i = 0; i < nobles.size(); i++) {
            if (i == 0) {
                prompt.append(": ");
            } else {
                prompt.append("  ");
            }
            prompt.append(i + 1).append("=").append(nobles.get(i).getName());
        }
        return prompt.toString();
    }

    /**
     * Resolves the best IPv4 address to display to remote players.
     *
     * @return a site-local IPv4 address when available, otherwise the first non-loopback IPv4, or {@code null}
     */
    private String resolveHostIpv4() {
        String firstIpv4 = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!(address instanceof Inet4Address ipv4) || ipv4.isLoopbackAddress()) {
                        continue;
                    }
                    if (firstIpv4 == null) {
                        firstIpv4 = ipv4.getHostAddress();
                    }
                    if (ipv4.isSiteLocalAddress()) {
                        return ipv4.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
            return null;
        }
        return firstIpv4;
    }

    /**
     * Automatically advances a disconnected player's turn when possible.
     *
     * @param state the shared game state
     * @param playerName the disconnected player whose turn is active
     */
    private void autoResolveDisconnectedTurn(GameState state, String playerName) {
        if (state == null || state.isGameOver() || !playerName.equals(state.getCurrentPlayer().getName())) {
            return;
        }

        GameEngine.TurnResult result;
        if (state.getCurrentPlayer().getGemCount() > 10) {
            result = gameEngine.processAutomaticReturnGems(state);
        } else {
            result = gameEngine.processAutomaticPass(state);
        }

        if (!result.isSuccess()) {
            String failureMessage = "Failed to auto-resolve " + playerName + "'s disconnected turn: "
                    + result.getMessage();
            finalGameMessage = failureMessage;
            appendGlobalLog(failureMessage);
            state.setGameOver(true);
            NetworkMessage gameOver = NetworkMessage.of(MessageType.GAME_OVER, failureMessage);
            gameOver.state = state;
            gameOver.logEntries = getRecentGlobalLog();
            broadcast(gameOver);
            boardUI.displayReadOnlyState(state, failureMessage, getRecentGlobalLog());
            System.out.println();
            System.out.println(RED + failureMessage + RESET);
            return;
        }

        broadcastTurnOutcome(state, playerName, result.getMessage());
    }

    /**
     * Builds the player-name order used to initialize the shared game state.
     *
     * @return player names ordered by age, then join order
     */
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

    /**
     * Creates the initial authoritative game state for the LAN match.
     *
     * @param playerNames the ordered player names participating in the match
     * @return the initialized game state
     */
    private GameState createInitialGameState(List<String> playerNames) {
        return gameStateFactory.createInitialGameState(totalPlayers, playerNames);
    }

    /**
     * Determines whether a proposed player name is already in use in the session.
     *
     * @param candidateName the proposed player name
     * @return {@code true} when the name conflicts with the host or a joined client
     */
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

    /**
     * Finds the tracked client connection for the given player.
     *
     * @param playerName the player name to resolve
     * @return the matching client connection, or {@code null} when absent
     */
    private ClientConnection findClientByName(String playerName) {
        for (ClientConnection client : clients) {
            if (client.playerName.equals(playerName)) {
                return client;
            }
        }
        return null;
    }

    /**
     * Closes every tracked client socket.
     */
    private void closeClients() {
        for (ClientConnection client : clients) {
            closeClient(client);
        }
    }

    /**
     * Closes a single client socket, ignoring shutdown errors.
     *
     * @param client the client connection to close
     */
    private void closeClient(ClientConnection client) {
        try {
            client.socket.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Sleeps the current thread for the requested duration.
     *
     * @param ms the number of milliseconds to sleep
     */
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Internal record of a remote client's active session resources.
     */
    private static final class ClientConnection {
        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;
        private final String playerName;
        private final int playerAge;
        private final int playerIndex;
        private boolean disconnected;

        /**
         * Creates a tracked client connection.
         *
         * @param socket the connected socket
         * @param reader the socket reader
         * @param writer the socket writer
         * @param playerName the player's display name
         * @param playerAge the player's age used for turn ordering
         * @param playerIndex the player's join order among remote clients
         */
        private ClientConnection(Socket socket, BufferedReader reader, PrintWriter writer,
                String playerName, int playerAge, int playerIndex) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
            this.playerName = playerName;
            this.playerAge = playerAge;
            this.playerIndex = playerIndex;
            this.disconnected = false;
        }
    }

    /**
     * Lightweight sortable view of player identity metadata used during lobby ordering.
     *
     * @param name the player's display name
     * @param age the player's age
     * @param joinOrder the player's join order for tie-breaking
     */
    private record PlayerSlot(String name, int age, int joinOrder) {}
}
