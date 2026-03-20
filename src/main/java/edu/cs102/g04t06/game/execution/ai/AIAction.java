package edu.cs102.g04t06.game.execution.ai;

import edu.cs102.g04t06.game.rules.entities.ActionType;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Encapsulates the action an AI has decided to take on its turn.
 * Passed from AIStrategy back to GameBoardUI for execution.
 */
public class AIAction {
    private final ActionType actionType;
    private final Card targetCard;
    private final boolean fromReserved;
    private final GemCollection gemSelection;
    private final String description;

    /**
     * Creates an AIAction with all fields.
     *
     * @param actionType    the type of action to perform
     * @param targetCard    the card to purchase or reserve (null for gem actions)
     * @param fromReserved  true if purchasing from the player's reserved hand
     * @param gemSelection  the gems to take (null for card actions)
     * @param description   human-readable description for the action log
     */
    public AIAction(ActionType actionType, Card targetCard, boolean fromReserved,
                    GemCollection gemSelection, String description) {
        this.actionType = actionType;
        this.targetCard = targetCard;
        this.fromReserved = fromReserved;
        this.gemSelection = gemSelection;
        this.description = description;
    }

    /** Returns the type of action the AI has chosen. */
    public ActionType getActionType() {
        return actionType;
    }

    /** Returns the card to purchase or reserve, or null for gem actions. */
    public Card getTargetCard() {
        return targetCard;
    }

    /** Returns true if the AI is purchasing from its reserved hand. */
    public boolean isFromReserved() {
        return fromReserved;
    }

    /** Returns the gem selection for take-gem actions, or null for card actions. */
    public GemCollection getGemSelection() {
        return gemSelection;
    }

    /** Returns a human-readable description of this action for the action log. */
    public String getDescription() {
        return description;
    }
}
