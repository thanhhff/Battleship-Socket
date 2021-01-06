package model;

import server.messages.GameResponseMessage;
import client.Client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Battleships board containing a 10x10 Square grid and 5 Ship
 * objects. The Board implements Serializable to allow it to be sent over
 * DataOutputStreams/DataInputStreams
 */
public class Board implements Serializable {
    /**
     * The dimension of the Board
     */
    public static final int BOARD_DIMENSION = 10;
    private Square[][] squares;
    private ArrayList<Ship> ships;
    private boolean ownBoard;
    private transient Client client;
    private transient boolean boatPositionLocked = true;
    private transient ArrayList<PropertyChangeListener> changeListeners;

    /**
     * Creates a Board with a 10x10 Square grid and 5 unplaced
     * Ships
     *
     * ownBoard: Indicates whether it is its own Board
     */
    public Board(boolean ownBoard) {
        this.ownBoard = ownBoard;
        squares = new Square[BOARD_DIMENSION][BOARD_DIMENSION];

        // populates the squares array
        for (int i = 0; i < BOARD_DIMENSION; i++) {
            for (int j = 0; j < BOARD_DIMENSION; j++) {
                squares[i][j] = new Square(i, j, ownBoard);
            }
        }

        ships = new ArrayList<>();
        ships.add(new Ship(Ship.Type.AIRCRAFT_CARRIER));
        ships.add(new Ship(Ship.Type.BATTLESHIP));
        ships.add(new Ship(Ship.Type.DESTROYER));
        ships.add(new Ship(Ship.Type.PATROL_BOAT));
        ships.add(new Ship(Ship.Type.SUBMARINE));

        this.changeListeners = new ArrayList<>();
    }

    /**
     * Validates a Board by checking its Ship positions are correct
     *
     * board: The Board which is having its validity tested
     * return true if the Board and its Ships are valid(i.e. not
     * overlapping or over the edge of the board), otherwise false
     */
    public static boolean isValid(Board board) {
        Board tempBoard = new Board(true);
        for (Ship s : board.getShips()) {
            if (s.getSquares().size() == 0) {
                return false;
            }
            int[] tl = s.getTopLeft();
            Ship tempBoardShip = tempBoard.findShipByType(s.getType());
            tempBoardShip.setVertical(s.isVertical());
            if (!tempBoard.placeShip(tempBoardShip, tl[0], tl[1])) {
                return false;
            }
        }
        return tempBoard.shipPlacementEquals(board);
    }

    /**
     * Checks whether the Ship positions are locked
     */
    public boolean isBoatPositionLocked() {
        return boatPositionLocked;
    }

    /**
     * Sets the Ship positions to be locked or unlocked
     *
     * boatPositionLocked: True to lock the Ship positions, false to unlock
     */
    public void setBoatPositionLocked(boolean boatPositionLocked) {
        this.boatPositionLocked = boatPositionLocked;
        client.getView().setSendShipState(!boatPositionLocked);
        firePropertyChange("resetSelectedShip", null, null);
    }

    /**
     * Checks whether it is its own Board
     */
    public boolean isOwnBoard() {
        return (ownBoard);
    }

    /**
     * Gets a Square from the Board
     *
     * x: The index of the Square on the X-axis
     * y: The index of the Square on the Y-axis
     */
    public Square getSquare(int x, int y) {
        return squares[x][y];
    }

    /**
     * Places a Ship on the Board with the top-left Square of
     * the Ship at the given co-ordinates
     *
     * ship: The Ship to be placed on the Board
     * x: The index of the Square on the X-axis
     * y: The index of the Square on the Y-axis
     * return true if the Ship has been placed on the Board, false if
     * it can't be placed there due to overlapping or being off the Board
     */
    public boolean placeShip(Ship ship, int x, int y) {
        // checks if it is within the board
        int end = (ship.isVertical()) ? y + ship.getLength() - 1 : x
                + ship.getLength() - 1;
        if (x < 0 || y < 0 || end >= BOARD_DIMENSION) {
            return false;
        }

        // checks for overlapping
        for (int i = 0; i < ship.getLength(); i++) {
            if (ship.isVertical()) {
                if (squares[x][y + i].isShip())
                    return false;
            } else {
                if (squares[x + i][y].isShip())
                    return false;
            }
        }

        // puts ship on squares
        for (int i = 0; i < ship.getLength(); i++) {
            if (ship.isVertical()) {
                squares[x][y + i].setShip(ship);
                ship.setSquare(squares[x][y + i]);
            } else if (!ship.isVertical()) {
                squares[x + i][y].setShip(ship);
                ship.setSquare(squares[x + i][y]);
            }
        }

        return true;
    }

    /**
     * Picks up the Ship from the Board and clears its {@link Square}s
     *
     * ship: The Ship to pick up
     */
    public void pickUpShip(Ship ship) {
        for (Square s : ship.getSquares()) {
            s.setShip(null);
        }
        ship.clearSquares();
    }

    /**
     * return true if all Ships are sunk, false otherwise
     */
    public boolean gameOver() {
        for (Ship ship : ships) {
            if (!ship.isSunk())
                return false;
        }
        return true;
    }

    /**
     * Prints the Board to the console, showing the status of each Square
     *
     */
    public void printBoard(boolean clean) {
        for (int i = 0; i < BOARD_DIMENSION; ++i) {
            for (int j = 0; j < BOARD_DIMENSION; ++j) {
                Square s = squares[j][i];
                Ship ship = s.getShip();
                char c = '-';
                if (s.isGuessed() && !clean
                        && s.getState() == Square.State.CONTAINS_SHIP) {
                    c = 'X';
                } else if (s.isGuessed() && !clean) {
                    c = 'O';
                } else if (ship != null) {
                    switch (ship.getType()) {
                    case AIRCRAFT_CARRIER:
                        c = 'A';
                        break;
                    case BATTLESHIP:
                        c = 'B';
                        break;
                    case SUBMARINE:
                        c = 'S';
                        break;
                    case DESTROYER:
                        c = 'D';
                        break;
                    case PATROL_BOAT:
                        c = 'P';
                    }
                }
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    /**
     * Gets all of the Ships belonging to the Board
     *
     * return an ArrayList of the Ships from the Board (the
     * Ships might not be placed on the Board yet)
     */
    public ArrayList<Ship> getShips() {
        return ships;
    }

    /**
     * Applies a move to the Board, updating the Square and sinking the
     * Ship if necessary
     *
     * gameResponse: The GameResponseMessage being applied to the Board
     */
    public void applyMove(GameResponseMessage gameReponse) {
        Ship ship = gameReponse.shipSank();
        if (ship != null) {
            ship.sink();
            if (!ownBoard) {
                ship.updateSquareReferences(this);
                ships.add(ship);
                firePropertyChange("sankShip", null, ship);
            }
            for (Square shipSquare : ship.getSquares()) {
                Square boardSquare = getSquare(shipSquare.getX(),
                        shipSquare.getY());
                boardSquare.update(true, ship);
            }
            // TODO: Fix me
            client.getView().addChatMessage("SUNK SHIP" + ship.toString());
        } else {
            Square square = getSquare(gameReponse.getX(), gameReponse.getY());
            square.update(gameReponse.isHit(), null);
        }
    }

    /**
     * Checks if two Boards have identical Ship positions
     *
     * board: The Board which this Board is being compared against
     * return true if the Boards have Ships in identical positions
     */
    public boolean shipPlacementEquals(Board board) {
        for (int y = 0; y < BOARD_DIMENSION; ++y) {
            for (int x = 0; x < BOARD_DIMENSION; ++x) {
                Square s1 = this.getSquare(x, y);
                Square s2 = board.getSquare(x, y);
                if ((s1.isShip() != s2.isShip())) {
                    return false;
                }
                if (s1.getShip() != null && s2.getShip() != null
                        && s1.getShip().getType() != s2.getShip().getType()) {
                    return false;
                }
            }
        }
        return true;
    }

    private Ship findShipByType(Ship.Type type) {
        for (Ship s : ships) {
            if (s.getType() == type) {
                return s;
            }
        }
        return null;
    }

    /**
     * Checks if this Square is next to a Ship horizontally,
     * vertically or diagonally
     *
     * square: The Square which is being checked for nearby Ships
     * return true if there is a Ship next to this Square, false otherwise
     */
    public boolean isSquareNearShip(Square square) {
        for (int x = square.getX() - 1; x <= square.getX() + 1; x++) {
            for (int y = square.getY() - 1; y <= square.getY() + 1; y++) {
                if (isCoordWithinBounds(x, y) && getSquare(x, y).isShip()
                        && !(x == square.getX() && y == square.getY())) {
                    return true;
                }
            }
        }
        return false;
    }

    // checks if x and y are between 0 and 9 inclusive
    private boolean isCoordWithinBounds(int x, int y) {
        return (x >= 0 && x < 10 && y >= 0 && y < 10);
    }

    /**
     * Sends a move at the provided co-ordinates to the Client's DataOutputStream
     *
     * x: The index of the move on the X-axis
     * y: The index of the move on the Y-axis
     * 
     */
    public void sendCoordinates(int x, int y) throws IOException {
        client.sendCoordinates(x, y);
    }

    /**
     * Adds a new PropertyChangeListener to the Board
     *
     * listener:The PropertyChangeListener which is being added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Fires a PropertyChangeEvent when a Ship is rotated
     */
    public void selectedShipRotated() {
        firePropertyChange("rotateSelectedShip", null, null);
    }

    /**
     * Gets the Board's Client
     *
     * return the Board's Client
     */
    public Client getClient() {
        return client;
    }

    /**
     * Sets the Board's Client
     *
     * client: The new Client for the Board
     */
    public void setClient(Client client) {
        this.client = client;
    }

    private void firePropertyChange(String property, Object oldValue,
            Object newValue) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, property,
                oldValue, newValue);
        for (PropertyChangeListener listener : changeListeners) {
            listener.propertyChange(event);
        }
    }
}
