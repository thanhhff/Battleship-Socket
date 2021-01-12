package server;

import model.Board;
import model.Ship;
import model.Square;
import server.messages.MoveMessage;
import server.messages.MoveResponseMessage;
import server.messages.NotificationMessage;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Game {

    private Player player1;
    private Player player2;
    private Player turn;

    private Timer placementTimer;
    private Timer turnTimer;

    public final static int TURN_TIMEOUT = 60000;
    public final static int PLACEMENT_TIMEOUT = 120000;

    private boolean gameStarted;

    /**
     * Constructs a Game between two players, and informs the players of the
     * name of their opponent. A timer is started, which the ships are to be
     * placed by the end of.
     *
     * @param player1 a player
     * @param player2 another player
     */
    public Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        player1.setGame(this);
        player2.setGame(this);
        player1.writeNotification(NotificationMessage.OPPONENTS_NAME,
                player2.getPlayerName());
        player2.writeNotification(NotificationMessage.OPPONENTS_NAME,
                player1.getPlayerName());
        NotificationMessage placeShipsMessage = new NotificationMessage(
                NotificationMessage.PLACE_SHIPS);
        player1.writeObject(placeShipsMessage);
        player2.writeObject(placeShipsMessage);

        placementTimer = new Timer();
        placementTimer.schedule(new PlacementTimerTask(), PLACEMENT_TIMEOUT);
    }

    /**
     * Returns the other player in the game who is not the specified player.
     *
     * @param self the specified player
     * @return the player playing the specified player
     */
    public Player getOpponent(Player self) {
        if (player1 == self) {
            return player2;
        }
        return player1;
    }

    /**
     * Sets the game in both players to null.
     */
    public void killGame() {
        player1.setGame(null);
        player2.setGame(null);
    }

    /**
     * Sets the turn to the specified player's.
     *
     * @param player the player who's turn it becomes
     */
    public synchronized void setTurn(Player player) {
        turn = player;
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        turnTimer = new Timer();
        turnTimer.schedule(new TurnTimerTask(), TURN_TIMEOUT);
        turn.writeNotification(NotificationMessage.YOUR_TURN);
        getOpponent(turn).writeNotification(NotificationMessage.OPPONENTS_TURN);
    }

    /**
     * Checks if both players have set valid boards. If they have, the game is
     * started.
     */
    public void checkBoards() {
        if (player1.getBoard() != null && player2.getBoard() != null) {
            placementTimer.cancel();
            startGame();
        }
    }

    /**
     * Starts the game and sets the turn to a random player's.
     */
    private void startGame() {
        gameStarted = true;
        if (new Random().nextInt(2) == 0) {
            setTurn(player1);
        } else {
            setTurn(player2);
        }
    }

    /**
     * Applies a move a player has sent. Responds to the move with either
     * a NotificationMessage stating an error, or a MoveResponseMessage.
     *
     * @see server.messages.NotificationMessage
     * @see server.messages.MoveResponseMessage
     * @param move the move sent by the player
     * @param player the player who sent the move
     */
    public synchronized void applyMove(MoveMessage move, Player player) {
        int x = move.getX();
        int y = move.getY();
        int max = Board.BOARD_DIMENSION;

        System.out.println("<< " + NotificationMessage.SHOT + " "
                + player.socket.getRemoteSocketAddress().toString() + " " + x + " " + y);

        if (player != turn) {
            System.out.println(">> " + NotificationMessage.NOT_YOUR_TURN + " "
                    + player.socket.getRemoteSocketAddress().toString() );
            player.writeNotification(NotificationMessage.NOT_YOUR_TURN);
            return;
        }

        if (x < 0 || x >= max || y < 0 || y >= max) {
            System.out.println(">> " + NotificationMessage.INVALID_MOVE + " "
                    + player.socket.getRemoteSocketAddress().toString());
            player.writeNotification(NotificationMessage.INVALID_MOVE);
        } else {
            Player opponent = getOpponent(player);
            Square square = opponent.getBoard().getSquare(x, y);
            if (square.isGuessed()) {
                System.out.println(">> " + NotificationMessage.REPEATED_MOVE + " "
                        + player.socket.getRemoteSocketAddress().toString());
                player.writeNotification(NotificationMessage.REPEATED_MOVE);
                return;
            }

            System.out.println(">> " + NotificationMessage.SHOT + " "
                    + opponent.socket.getRemoteSocketAddress().toString() + " " + x + " " + y);

            boolean hit = square.guess();
            Ship ship = square.getShip();
            MoveResponseMessage response;
            if (ship != null && ship.isSunk()) {
                response = new MoveResponseMessage(x, y, ship, true, false);
            } else {
                response = new MoveResponseMessage(x, y, null, hit, false);
            }
            player.writeObject(response);
            response.setOwnBoard(true);
            opponent.writeObject(response);
            if (opponent.getBoard().gameOver()) {
                System.out.println(">> " + NotificationMessage.GAME_WIN + " "
                        + turn.socket.getRemoteSocketAddress().toString());
                turn.writeNotification(NotificationMessage.GAME_WIN);

                System.out.println(">> " + NotificationMessage.GAME_LOSE + " "
                        + opponent.socket.getRemoteSocketAddress().toString() );
                opponent.writeNotification(NotificationMessage.GAME_LOSE);
                turn = null;
            } else if (hit) {
                System.out.println("<< " + NotificationMessage.OPPONENTS_TURN + " " + opponent.socket.getRemoteSocketAddress().toString());
                setTurn(player); // player gets another go if hit
                System.out.println(">> " + NotificationMessage.YOUR_TURN + " " + player.socket.getRemoteSocketAddress().toString());
            } else {
                System.out.println("<< " + NotificationMessage.YOUR_TURN + " " + opponent.socket.getRemoteSocketAddress().toString());
                setTurn(getOpponent(player));
                System.out.println(">> " + NotificationMessage.OPPONENTS_TURN + " " + player.socket.getRemoteSocketAddress().toString());
            }
        }
    }

    private class PlacementTimerTask extends TimerTask {

        @Override
        public void run() {
            if (player1.getBoard() == null & player2.getBoard() == null) {
                NotificationMessage draw = new NotificationMessage(
                        NotificationMessage.TIMEOUT_DRAW);
                player1.writeObject(draw);
                player2.writeObject(draw);
                killGame();
            } else if (player1.getBoard() == null) {
                // Player1 failed to place ships in time
                player1.writeNotification(NotificationMessage.TIMEOUT_LOSE);
                player2.writeNotification(NotificationMessage.TIMEOUT_WIN);
                killGame();
            } else if (player2.getBoard() == null) {
                // Player2 failed to place ships in time
                player1.writeNotification(NotificationMessage.TIMEOUT_WIN);
                player2.writeNotification(NotificationMessage.TIMEOUT_LOSE);
                killGame();
            }
        }
    }

    private class TurnTimerTask extends TimerTask {

        @Override
        public void run() {
            if (turn != null) {
                turn.writeNotification(NotificationMessage.TIMEOUT_LOSE);
                getOpponent(turn).writeNotification(
                        NotificationMessage.TIMEOUT_WIN);
                killGame();
            }
        }

    }

}
