package edu.cs102.g04t06.game.rules.entities;

/**
 * Common interface for game entities that contribute prestige points.
 * Both {@link Card} and {@link Noble} implement this interface.
 */
public interface GameEntity {
    /**
     * Returns the prestige points this entity contributes.
     *
     * @return prestige points
     */
    int getPoints();
}
