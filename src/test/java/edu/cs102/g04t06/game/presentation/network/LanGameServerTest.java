package edu.cs102.g04t06.game.presentation.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.execution.GameStateFactory;
import edu.cs102.g04t06.game.rules.GameState;

class LanGameServerTest {
    private InputStream originalIn;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalIn = System.in;
        originalOut = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    void handleHostTurn_withQ_marksGameOver() throws Exception {
        System.setIn(new ByteArrayInputStream("q\n".getBytes()));
        LanGameServer server = new LanGameServer(4567, 2, "Host", 20);
        GameState state = new GameStateFactory().createInitialGameState(2, List.of("Host", "Guest"));

        Method handleHostTurn = LanGameServer.class.getDeclaredMethod("handleHostTurn", GameState.class);
        handleHostTurn.setAccessible(true);
        handleHostTurn.invoke(server, state);

        Field finalGameMessage = LanGameServer.class.getDeclaredField("finalGameMessage");
        finalGameMessage.setAccessible(true);

        assertTrue(state.isGameOver());
        assertEquals("Host disconnected. Host quit the session.", finalGameMessage.get(server));
    }
}
