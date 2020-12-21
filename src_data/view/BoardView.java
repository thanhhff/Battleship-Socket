package view;

import model.Board;
import model.Ship;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Class for view of the board.
 */
public class BoardView extends JPanel implements PropertyChangeListener {

    public static int SQUARE_WIDTH = 35;
    private static final int BOARD_SIZE = Board.BOARD_DIMENSION;
    private ShipView selectedShipView = null;
    private SquareView[][] squareViews;
    private ArrayList<ShipView> shipViews = new ArrayList<>();
    private Board model;

    /**
     * Constructs BoardView.
     * @param ownBoard if true, then the board is own and ships will be added under the board for placing.
     *                 Otherwise, ships are not added.
     */
    public BoardView(boolean ownBoard) {
        this.model = new Board(ownBoard);
        model.addPropertyChangeListener(this);

        addSquares();
        if (model.isOwnBoard()) {
            addShips();
        }
    }

    /**
     * Resets selected ship.
     */
    public void resetSelectedShipView() {
        if (selectedShipView != null) {
            selectedShipView.setSelected(false);
        }
        selectedShipView = null;
        repaint();
    }

    /**
     * Returns ship model.
     * @return ship model.
     */
    public Board getModel() {
        return model;
    }

    /**
     * Moves selected ship.
     */
    private void moveSelectedShip() {
        final int x = selectedShipView.getX() + SQUARE_WIDTH / 2;
        final int y = selectedShipView.getY() + SQUARE_WIDTH / 2;
        SquareView hovered = getSquare(x, y);
        int[] newPosition = translateCoordinates(x, y);
        boolean shouldReset = true;
        model.pickUpShip(selectedShipView.getModel());
        if (hovered != null) {
            boolean result = this.model.placeShip(selectedShipView.getModel(),
                    newPosition[0], newPosition[1]);
            if (result) {
                selectedShipView.setX(hovered.getX());
                selectedShipView.setY(hovered.getY());
                System.out.println();

                shouldReset = false;
            }
        }
        if (shouldReset) {
            selectedShipView.resetPosition();
        }
        this.model.printBoard(true);
    }

    public ShipView getSelectedShip() {
        return selectedShipView;
    }

    private int[] translateCoordinates(int x, int y) {
        return new int[]{x / SQUARE_WIDTH, y / SQUARE_WIDTH};
    }

    /**
     * Gets square at specified coordinates (where mouse was clicked).
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @return SquareView at given x and y.
     */
    private SquareView getSquare(int x, int y) {
        int i = x / SQUARE_WIDTH;
        int j = y / SQUARE_WIDTH;
        return i >= 0 && j >= 0 && i < 10 && j < 10 ? squareViews[i][j] : null;
    }

    public void addShipView(Ship ship) {
        int topLeft[] = ship.getTopLeft();
        ShipView shipView = new ShipView(ship.getLength(), SQUARE_WIDTH,
                topLeft[0] * SQUARE_WIDTH, topLeft[1] * SQUARE_WIDTH, ship);
        if (ship.isVertical()) {
            shipView.rotate();
        }
        shipViews.add(shipView);
    }

    /**
     * Initializes board's squares.
     */
    private void addSquares() {
        squareViews = new SquareView[BOARD_SIZE][BOARD_SIZE];

        setPreferredSize(new Dimension((BOARD_SIZE) * SQUARE_WIDTH + 1,
                (BOARD_SIZE + 3) * SQUARE_WIDTH + 15));
        setVisible(true);
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                squareViews[i][j] = new SquareView(i * SQUARE_WIDTH, j * SQUARE_WIDTH,
                        SQUARE_WIDTH, SQUARE_WIDTH, this, model.getSquare(i, j));
            }
        }
    }

    /**
     * Adds ships to the board.
     */
    private void addShips() {
        int x = 0;
        int y = SQUARE_WIDTH * BOARD_SIZE + 5;
        for (Ship shipModel : model.getShips()) {
            int length = shipModel.getLength();
            ShipView shipView = new ShipView(length, SQUARE_WIDTH, x, y,
                    shipModel);
            shipModel.setView(shipView);
            shipViews.add(shipView);
            final int newPosition = x + length * SQUARE_WIDTH + 5;
            if (newPosition + length * SQUARE_WIDTH + 5 > SQUARE_WIDTH * 10) {
                x = 0;
                y += SQUARE_WIDTH + 5;
            } else {
                x = newPosition;
            }
        }
    }

    /**
     * Rotates selected ship.
     */
    private void rotateSelectedShip() {
        selectedShipView.rotate();
        repaint();
        moveSelectedShip();
    }

    /**
     * Paints BoardView.
     *
     * @param g Graphics.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ArrayList<SquareView> hit = new ArrayList<>();
        for (SquareView[] row : squareViews) {
            for (SquareView square : row) {
                square.paint(g);
                if (square.getState() == SquareView.HIT) {
                    hit.add(square);
                }
            }
        }
        for (ShipView s : shipViews) {
            s.paint(g);
        }
        for (SquareView square : hit) {
            if (square.animated()) {
                square.drawExplosion(g);
            } else {
                square.drawCross(g);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("sankShip")) {
            addShipView((Ship) evt.getNewValue());
        } else if (evt.getPropertyName().equals("rotateSelectedShip")) {
            rotateSelectedShip();
        } else if (evt.getPropertyName().equals("resetSelectedShip")) {
            resetSelectedShipView();
        }
    }
}
