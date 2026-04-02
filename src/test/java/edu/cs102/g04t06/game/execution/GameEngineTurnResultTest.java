package edu.cs102.g04t06.game.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

class GameEngineTurnResultTest {

    private GameEngine gameEngine;
    private GameState state;
    private Player player;

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngine();
        player = new Player("Alice", 0);

        Cost zeroCost = new Cost(new EnumMap<>(GemColor.class));
        List<Card> level1 = new ArrayList<>();
        List<Card> level2 = new ArrayList<>();
        List<Card> level3 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            level1.add(new Card(1, 0, GemColor.WHITE, zeroCost));
            level2.add(new Card(2, 0, GemColor.BLUE, zeroCost));
            level3.add(new Card(3, 0, GemColor.GREEN, zeroCost));
        }

        state = new GameState(
                List.of(player),
                new CardMarket(level1, level2, level3),
                new GemCollection().add(GemColor.GOLD, 5),
                new ArrayList<>(),
                15);
    }

    @Test
    void processCommand_reserveDeckReservesTopCard() {
        int deckSizeBefore = state.getMarket().getDeckSize(1);

        GameEngine.TurnResult result = gameEngine.processPlayerCommand(state, "reserve deck t1");

        assertTrue(result.isSuccess());
        assertEquals(1, player.getReservedCards().size());
        assertEquals(deckSizeBefore - 1, state.getMarket().getDeckSize(1));
        assertEquals(1, player.getGems().getCount(GemColor.GOLD));
    }

    @Test
    void processAutomaticPass_advancesToNextPlayer() {
        Player secondPlayer = new Player("Bob", 1);
        state = new GameState(
                List.of(player, secondPlayer),
                state.getMarket(),
                state.getGemBank(),
                new ArrayList<>(),
                15);

        GameEngine.TurnResult result = gameEngine.processAutomaticPass(state);

        assertTrue(result.isSuccess());
        assertEquals(1, state.getCurrentPlayerIndex());
        assertTrue(result.getMessage().contains("auto-passed"));
    }

    @Test
    void processAutomaticReturnGems_returnsExcessAndAdvancesTurn() {
        Player secondPlayer = new Player("Bob", 1);
        state = new GameState(
                List.of(player, secondPlayer),
                state.getMarket(),
                new GemCollection()
                        .add(GemColor.WHITE, 4)
                        .add(GemColor.BLUE, 4)
                        .add(GemColor.GREEN, 4)
                        .add(GemColor.RED, 4)
                        .add(GemColor.BLACK, 4)
                        .add(GemColor.GOLD, 5),
                new ArrayList<>(),
                15);
        player.addGems(new GemCollection()
                .add(GemColor.WHITE, 4)
                .add(GemColor.BLUE, 4)
                .add(GemColor.GREEN, 4));

        GameEngine.TurnResult result = gameEngine.processAutomaticReturnGems(state);

        assertTrue(result.isSuccess());
        assertEquals(10, player.getGemCount());
        assertEquals(1, state.getCurrentPlayerIndex());
        assertTrue(result.getMessage().contains("auto-returned"));
    }

    @Test
    void processPlayerCommand_multipleClaimableNoblesAwaitsSelection() {
        Player secondPlayer = new Player("Bob", 1);
        state = new GameState(
                List.of(player, secondPlayer),
                state.getMarket(),
                state.getGemBank(),
                new ArrayList<>(),
                15);

        Card whiteBonus = new Card(1, 0, GemColor.WHITE, new Cost(new EnumMap<>(GemColor.class)));
        Card blueBonus = new Card(1, 0, GemColor.BLUE, new Cost(new EnumMap<>(GemColor.class)));
        for (int i = 0; i < 3; i++) {
            player.addCard(whiteBonus);
            player.addCard(blueBonus);
        }

        Map<GemColor, Integer> whiteReq = new EnumMap<>(GemColor.class);
        whiteReq.put(GemColor.WHITE, 3);
        Noble whiteNoble = new Noble(1, "White Court", whiteReq);

        Map<GemColor, Integer> blueReq = new EnumMap<>(GemColor.class);
        blueReq.put(GemColor.BLUE, 3);
        Noble blueNoble = new Noble(2, "Blue Court", blueReq);

        state.getAvailableNobles().add(whiteNoble);
        state.getAvailableNobles().add(blueNoble);

        GameEngine.TurnResult result = gameEngine.processPlayerCommand(state, "pass");

        assertTrue(result.isSuccess());
        assertTrue(result.isAwaitingNobleSelection());
        assertEquals(2, result.getClaimableNobles().size());
        assertEquals(0, player.getClaimedNobles().size());
        assertEquals(0, state.getCurrentPlayerIndex(), "Turn should wait for noble choice");
    }

    @Test
    void processNobleSelection_claimsChosenNobleAndAdvancesTurn() {
        Player secondPlayer = new Player("Bob", 1);
        state = new GameState(
                List.of(player, secondPlayer),
                state.getMarket(),
                state.getGemBank(),
                new ArrayList<>(),
                15);

        Card whiteBonus = new Card(1, 0, GemColor.WHITE, new Cost(new EnumMap<>(GemColor.class)));
        Card blueBonus = new Card(1, 0, GemColor.BLUE, new Cost(new EnumMap<>(GemColor.class)));
        for (int i = 0; i < 3; i++) {
            player.addCard(whiteBonus);
            player.addCard(blueBonus);
        }

        Map<GemColor, Integer> whiteReq = new EnumMap<>(GemColor.class);
        whiteReq.put(GemColor.WHITE, 3);
        Noble whiteNoble = new Noble(1, "White Court", whiteReq);

        Map<GemColor, Integer> blueReq = new EnumMap<>(GemColor.class);
        blueReq.put(GemColor.BLUE, 3);
        Noble blueNoble = new Noble(2, "Blue Court", blueReq);

        state.getAvailableNobles().add(whiteNoble);
        state.getAvailableNobles().add(blueNoble);

        GameEngine.TurnResult pendingResult = gameEngine.processPlayerCommand(state, "pass");
        GameEngine.TurnResult result = gameEngine.processNobleSelection(state, "2", pendingResult.getMessage());

        assertTrue(result.isSuccess());
        assertEquals(1, player.getClaimedNobles().size());
        assertTrue(player.getClaimedNobles().contains(blueNoble));
        assertTrue(state.getAvailableNobles().contains(whiteNoble));
        assertFalse(state.getAvailableNobles().contains(blueNoble));
        assertEquals(1, state.getCurrentPlayerIndex(), "Turn should advance after noble choice");
    }
}
