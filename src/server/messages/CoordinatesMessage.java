package server.messages;

import java.io.Serializable;

/**
 * An object sent from a player containing the coordinates of the move they
 * wish to make, which refers to the {@link model.Square} they are guessing.
 */
public class CoordinatesMessage implements Serializable {

    private int x;
    private int y;

    /**
     * x: x coordinate of guess
     * y: y coordinate of guess
     */
    public CoordinatesMessage(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}
