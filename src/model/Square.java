package model;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Square on the Board which can contain a Ship and can also 
 * be guessed by the player.
 */
public class Square implements Serializable {
    private Ship ship;
    private boolean guessed;
    private int x, y;
    private State state;
    private transient ArrayList<ChangeListener> changeListeners;

    /**
     * x: The index of the Square on the X-axis of the Board.
     * y: The index of the Square on the Y-axis of the Board.
     * ownBoard: Indicates whether the Board belongs to the player
     */
    public Square(int x, int y, boolean ownBoard) {
        this.ship = null;
        this.guessed = false;
        this.x = x;
        this.y = y;
        this.state = (ownBoard) ? State.NO_SHIP : State.UNKNOWN;
        this.changeListeners = new ArrayList<>();
    }

    /**
     * return true if there is a Ship on the Square, false otherwise
     */
    public boolean isShip() {
        return (ship != null);
    }

    /**
     * return the Ship on this Square, returns null if there is no 
     * the Ship currently on the Square
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Sets the Ship on the Square and updates the State.
     * ship: The new the Ship on the Square
     */
    public void setShip(Ship ship) {
        this.ship = ship;
        this.state = State.CONTAINS_SHIP;
    }

    /**
     * Gets whether the Square has been guessed.
     */
    public boolean isGuessed() {
        return guessed;
    }

    /**
     * Sets whether the Square has been guessed and reduces the health of the 
     * the Ship on the Square.
     * b: Indicates whether Square has been guessed
     */
    public void setGuessed(boolean b) {
        if (ship != null)
            ship.gotHit();
        guessed = b;
    }

    /**
     * Guesses the Square and reduces the health of the Ship if there 
     * is one on the Square.
     * return true if there is a Ship on the Square, false otherwise
     */
    public boolean guess() {
        guessed = true;
        if (ship != null) {
            ship.gotHit();
            return true;
        }
        return false;
    }

    /**
     * hit: Indicates if there is a Ship on the Square to hit
     * shipSunk: The new Ship to update the Square with
     */
    public void update(boolean hit, Ship shipSunk) {
        this.guessed = true;
        if (this.state == State.UNKNOWN) {
            this.state = (hit) ? State.CONTAINS_SHIP : State.NO_SHIP;
        } else if (this.ship != null) {
            ship.gotHit();
        }
        if (this.ship == null) {
            this.ship = shipSunk;
        }
        fireChange();
    }

    /**
     * Gets the index of the Square on the X-axis of the Board.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the index of the Square on the Y-axis of the Board.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the current State of the Square.
     */
    public State getState() {
        return state;
    }

    public enum State {
        CONTAINS_SHIP, NO_SHIP, UNKNOWN
    }

    /**
     * Adds a new ChangeListener to the Square.
     * listener: The new ChangeListener for the Square
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    private void fireChange() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(event);
        }
    }
}