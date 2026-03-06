package edu.cs102.g04t06.game.execution;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

public class ActionExecutorGemTest {

    private GameState state;
    private Player player;
    private GemCollection startingBank;

    @BeforeEach
    public void setUp() {
        // 1. Create Players
        List<Player> players = new ArrayList<>();
        players.add(new Player("Zyik", 0));
        players.add(new Player("Dong En", 1));

        // 2. Setup Bank (Give it 5 of every standard gem for a healthy start)
        startingBank = new GemCollection();
        startingBank = startingBank.add(GemColor.RED, 5)
                                   .add(GemColor.BLUE, 5)
                                   .add(GemColor.GREEN, 5)
                                   .add(GemColor.BLACK, 5)
                                   .add(GemColor.WHITE, 5);

        // 3. Initialize GameState (We pass null for Market/Nobles since Gem actions don't use them)
        state = new GameState(players, null, startingBank, new ArrayList<>());
        
        // 4. Grab the current player (Zyik) to easily verify their inventory later
        player = state.getCurrentPlayer();
    }

    // ==========================================================
    // TESTS FOR: executeTakeThreeDifferentGems()
    // ==========================================================

    @Test
    public void testExecuteTakeThreeDifferentGems_ValidSelection() {
        GemCollection selection = new GemCollection()
            .add(GemColor.RED, 1).add(GemColor.BLUE, 1).add(GemColor.GREEN, 1);
            
        ActionResult result = ActionExecutor.executeTakeThreeDifferentGems(state, selection);

        assertTrue(result.isSuccess(), "Action should succeed.");
        assertEquals(1, player.getGems().getCount(GemColor.RED), "Red gem added to player");
        assertEquals(1, player.getGems().getCount(GemColor.BLUE), "Blue gem added to player");
        assertEquals(4, state.getGemBank().getCount(GemColor.RED), "Red gem removed from bank");
    }

    @Test
    public void testExecuteTakeThreeDifferentGems_InvalidNotInBank() {
        // Empty the bank of RED gems
        state.removeGemsFromBank(new GemCollection().add(GemColor.RED, 5));
        
        GemCollection selection = new GemCollection()
            .add(GemColor.RED, 1).add(GemColor.BLUE, 1).add(GemColor.GREEN, 1);
            
        ActionResult result = ActionExecutor.executeTakeThreeDifferentGems(state, selection);

        assertFalse(result.isSuccess(), "Should fail because Bank has no Red gems.");
        assertEquals(0, player.getGemCount(), "Player should receive no gems.");
    }

    @Test
    public void testExecuteTakeThreeDifferentGems_PlayerExceeds10Gems() {
        // Give player 9 gems to start
        player.addGems(new GemCollection().add(GemColor.WHITE, 9));
        
        GemCollection selection = new GemCollection()
            .add(GemColor.RED, 1).add(GemColor.BLUE, 1).add(GemColor.GREEN, 1);
            
        ActionResult result = ActionExecutor.executeTakeThreeDifferentGems(state, selection);

        assertTrue(result.isSuccess(), "Action succeeds but requires return.");
        assertTrue(result.getMessage().contains("excess"), "Message should warn about exceeding 10 gems.");
        assertEquals(12, player.getGemCount(), "Player temporarily has 12 gems before returning.");
    }

    // ==========================================================
    // TESTS FOR: executeTakeTwoSameGems()
    // ==========================================================

    @Test
    public void testExecuteTakeTwoSameGems_ValidBankHas4Plus() {
        ActionResult result = ActionExecutor.executeTakeTwoSameGems(state, GemColor.BLACK);

        assertTrue(result.isSuccess(), "Action should succeed when bank has 5.");
        assertEquals(2, player.getGems().getCount(GemColor.BLACK), "2 Black gems added to player");
        assertEquals(3, state.getGemBank().getCount(GemColor.BLACK), "2 Black gems removed from bank");
    }

    @Test
    public void testExecuteTakeTwoSameGems_InvalidBankHasOnly3() {
        // Reduce bank to exactly 3 Black gems
        state.removeGemsFromBank(new GemCollection().add(GemColor.BLACK, 2));
        
        ActionResult result = ActionExecutor.executeTakeTwoSameGems(state, GemColor.BLACK);

        assertFalse(result.isSuccess(), "Action should fail when bank has only 3.");
        assertEquals(0, player.getGems().getCount(GemColor.BLACK), "No gems added to player");
    }

    // ==========================================================
    // TESTS FOR: executeReturnGems()
    // ==========================================================

    @Test
    public void testExecuteReturnGems_ValidReturn() {
        // Player has 12 gems, needs to return 2 RED
        player.addGems(new GemCollection().add(GemColor.RED, 2).add(GemColor.BLUE, 10));
        GemCollection toReturn = new GemCollection().add(GemColor.RED, 2);

        ActionResult result = ActionExecutor.executeReturnGems(state, toReturn);

        assertTrue(result.isSuccess(), "Return action should succeed.");
        assertEquals(10, player.getGemCount(), "Player ends with exactly 10 gems.");
        assertEquals(0, player.getGems().getCount(GemColor.RED), "Red gems removed from player.");
        assertEquals(7, state.getGemBank().getCount(GemColor.RED), "Red gems added back to bank (5 initial + 2 returned).");
    }
}