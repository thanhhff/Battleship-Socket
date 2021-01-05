package server.messages;

import model.Ship;

import java.io.Serializable;

/**
 * An object that is sent to both clients in response to a valid move,
 * containing information about the Square the move was applied
 * to, if it hit a ship, and contains the Ship it sank, if any.
 */
public class GameResponseMessage implements Serializable {

    private int x;
    private int y;
    private Ship shipSunk;
    private boolean hit;
    private boolean ownBoard;

    /**
     * Initialise a move response message where no ship was sunk.
     */
    public GameResponseMessage(int x, int y, boolean hit, boolean ownBoard) {
        this(x, y, null, hit, ownBoard);
    }

    /**
     * x: x coordinate of the Square
     * y: y coordinate of the Square
     * shipSunk: the ship that was sunk, if any
     * hit: true if the move hit a ship
     * ownBoard: true if receiving player's own board
     */
    public GameResponseMessage(int x, int y, Ship shipSunk, boolean hit,
            boolean ownBoard) {
        this.x = x;
        this.y = y;
        this.shipSunk = shipSunk;
        this.hit = hit;
        this.ownBoard = ownBoard;
    }

  
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Returns the Ship that was sunk in the move. Returns null
     * if the move did not result in a ship sinking.
     */
    public Ship shipSank() {
        return this.shipSunk;
    }

    /**
     * Returns true if the move hit a ship.
     */
    public boolean isHit() {
        return hit;
    }

    /**
     * return true if own board
     */
    public boolean isOwnBoard() {
        return ownBoard;
    }

    /**
     * ownBoard: true if own board
     */
    public void setOwnBoard(boolean ownBoard) {
        this.ownBoard = ownBoard;
    }

}
