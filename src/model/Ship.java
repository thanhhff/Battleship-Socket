package model;

import view.ShipView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Ship which can be placed on the Board for the other player to fire 
 * at. It has a length of 2-5 (inclusive) Squares and must have a 
 * Type.
 */
public class Ship implements Serializable {

    private Type type;
    private ArrayList<Square> squares;
    private boolean vertical;
    private int health;
    private transient ShipView view;


    /**
     * Constructs a Ship with its health and number of Squares 
     * dependant on the Type provided. The default orientation is 
     * horizontal.
     * type: The type of the Ship
     */
    public Ship(Type type) {
        this.type = type;
        this.vertical = false;
        this.health = type.length;
        squares = new ArrayList<Square>();
    }

    /**
     * return the length of the Ship (the number of Squares it covers)
     */
    public int getLength() {
        return type.length;
    }

    /**
     * return the type of the Ship
     */
    public Type getType() {
        return type;
    }

    /**
     * return true if Ship is vertical, false if Ship is horizontal
     */
    public boolean isVertical() {
        return vertical;
    }

    /**
     * Sets the orientation of the Ship.
     * b: true for vertical, false for horizontal
     */
    public void setVertical(boolean b) {
        this.vertical = b;
    }

    /**
     * Gets all of the Squares that the Ship is on (its location on the Board).
     * return an ArrayList of Squares
     */
    public ArrayList<Square> getSquares() {
        return squares;
    }

    /**
     * Adds a Square to the list of Squares the Ship is on.
     * square: The new Square to be added to the list of Squares
     */
    public void setSquare(Square square) {
        this.squares.add(square);
    }

    /**
     * Removes all of the Squares from the Ship.
     */
    public void clearSquares() {
        squares.clear();
    }

    /**
     * Reduces the health of the Ship by 1 when it is hit.
     */
    public void gotHit() {
        health--;
    }

    /**
     * Gets whether the Ship is sunk or still afloat.
     * true if Ship has been sunk, false otherwise
     */
    public boolean isSunk() {
        return (health == 0);
    }

    /**
     * Reduces the health of the Ship to 0, regardless of previous level of 
     * health.
     */
    public void sink() {
        health = 0;
    }

    /**
     * Gets the co-ordinates of the top-left (the head) Square of the 
     * Ship which is used for positioning the Ship.
     * return the co-ordinates of the top-left (the head) Square of the Ship
     */
    public int[] getTopLeft() {
        Square firstSquare = squares.get(0);
        int[] tl = { firstSquare.getX(), firstSquare.getY() };
        for (int i = 1; i < squares.size(); ++i) {
            Square s = squares.get(i);
            if (s.getX() < tl[0]) {
                tl[0] = s.getX();
            }
            if (s.getY() < tl[1]) {
                tl[1] = s.getY();
            }
        }
        return tl;
    }

    /**
     * Sets the ShipView belonging to the Ship. This is used for displaying the 
     * Ship in the GUI.
     * view: The ShipView used for displaying the Ship
     */
    public void setView(ShipView view) {
        this.view = view;
    }

    /**
     * The different types of Ship available, with lengths and names of types
     */
    public enum Type {
        AIRCRAFT_CARRIER(5, "aircraft carrier"), BATTLESHIP(4, "battleship"), SUBMARINE(
                3, "submarine"), DESTROYER(3, "destroyer"), PATROL_BOAT(2,
                "patrol boat");

        private int length;
        private String name;

        /**
         * Constructs a new Type of Ship
         * length: The length of the Type
         * name: The name of the Type
         */
        Type(int length, String name) {
            this.length = length;
            this.name = name;
        }

        /**
         * Gets the name of the Type
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Updates the list of Squares which indicate the position of the 
     * Ship to match board's Squares
     * board: The Board which is being used to update from
     */
    public void updateSquareReferences(Board board) {
        ArrayList<Square> newSquares = new ArrayList<>();
        for (Square s : squares) {
            newSquares.add(board.getSquare(s.getX(), s.getY()));
        }
        this.squares = newSquares;
    }

}
