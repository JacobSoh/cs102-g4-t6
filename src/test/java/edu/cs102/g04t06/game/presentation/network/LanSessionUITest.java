package edu.cs102.g04t06.game.presentation.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

class LanSessionUITest {
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
    void requestCommand_withQ_returnsDisconnectRequest() {
        System.setIn(new ByteArrayInputStream("q\n".getBytes()));
        LanSessionUI sessionUI = new LanSessionUI("Alice");

        NetworkMessage request = NetworkMessage.of(MessageType.REQUEST_COMMAND, "Your turn.");
        request.state = makeGameState();

        NetworkMessage reply = sessionUI.handle(request);

        assertNotNull(reply);
        assertEquals(MessageType.DISCONNECT_REQUEST, reply.type);
        assertEquals("Client requested disconnect.", reply.message);
    }

    private GameState makeGameState() {
        Player alice = new Player("Alice", 0);
        Player bob = new Player("Bob", 1);

        List<Card> tier1 = new ArrayList<>();
        tier1.add(new Card(1, 1, GemColor.WHITE, new Cost(Map.of(GemColor.RED, 2))));
        while (tier1.size() < 20) {
            tier1.add(new Card(1, 0, GemColor.BLUE, new Cost(Map.of(GemColor.WHITE, 1))));
        }

        List<Card> tier2 = new ArrayList<>();
        while (tier2.size() < 20) {
            tier2.add(new Card(2, 1, GemColor.RED, new Cost(Map.of(GemColor.BLUE, 2))));
        }

        List<Card> tier3 = new ArrayList<>();
        while (tier3.size() < 20) {
            tier3.add(new Card(3, 3, GemColor.GREEN, new Cost(Map.of(GemColor.BLACK, 3))));
        }

        CardMarket market = new CardMarket(tier1, tier2, tier3);
        GemCollection bank = new GemCollection()
                .add(GemColor.WHITE, 4)
                .add(GemColor.RED, 4)
                .add(GemColor.BLUE, 4)
                .add(GemColor.GREEN, 4)
                .add(GemColor.BLACK, 4)
                .add(GemColor.GOLD, 5);

        Map<GemColor, Integer> requirements = new EnumMap<>(GemColor.class);
        requirements.put(GemColor.WHITE, 3);
        requirements.put(GemColor.RED, 3);
        Noble noble = new Noble(1, "Test Noble", requirements);

        return new GameState(List.of(alice, bob), market, bank, new ArrayList<>(List.of(noble)), 15);
    }
}
