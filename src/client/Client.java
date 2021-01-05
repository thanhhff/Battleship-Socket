package client;

import server.Game;
import server.messages.ChatMessage;
import server.messages.CoordinatesMessage;
import server.messages.GameResponseMessage;
import server.messages.NotificationMessage;
import view.ClientView;

import model.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * A Client used for communicating with the server. Contains both player's
 * Boards
 */
public class Client extends Thread {

    private Board ownBoard;
    private Board opponentBoard;
    private ClientView view;

    private DataOutputStream out;
    private DataInputStream in;

    private String opponentName = "Player";

    /**
     * clientView: The ClientView used for the GUI
     * ownBoard: The Board belonging to the player
     * opponentBoard: The Board belonging to the player's opponent
     * out: The DataOutputStream for sending data
     * in: The DataOutputStream for receiving data
    */
    public Client(ClientView clientView, Board ownBoard, Board opponentBoard,
                DataOutputStream out, DataInputStream in) {
        this.ownBoard = ownBoard;
        this.opponentBoard = opponentBoard;
        this.view = clientView;

        ownBoard.setClient(this);
        opponentBoard.setClient(this);

        this.out = out;
        this.in = in;
    }

    /**
     * Runs this {@link Thread}. Waits to receive input from the server, parses
     * the input and executes instructions based on the input.
     */
    @Override
    public void run() {
        super.run();
        String input;
        try {
            while ((input = in.readUTF()) != null) {
                parseInput(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines the type of message of the input and then responds
     * accordingly.
     * input: The message from the server allowing the Client to determine
     * what course of action to take next.
     */
    public void parseInput(String input) {
        Object tmp = input;
        if (tmp instanceof NotificationMessage) {
            NotificationMessage n = (NotificationMessage) tmp;
            System.out.println(n.getCode());

            switch (n.getCode()) {
            case NotificationMessage.OPPONENTS_NAME:
                // TODO: handle receiving opponents name
                //view.addChatMessage("Received opponent's name.");
                if (n.getText().length == 1) {
                    opponentName = n.getText()[0];
                    view.setTitle("Playing Battleships against " + opponentName);
                }
                break;
            case NotificationMessage.BOARD_ACCEPTED:
                view.setMessage("Board accepted. Waiting for opponent.");
                view.stopTimer();
                ownBoard.setBoatPositionLocked(true);
                break;

            case NotificationMessage.SHOT:
                break;

            case NotificationMessage.GAME_TOKEN:
                // TODO: handle receiving game token to share with friend
                view.addChatMessage("Received game token.");
                break;
            case NotificationMessage.GAME_NOT_FOUND:
                // TODO: handle joining a game that doesn't exist
                view.addChatMessage("Game not found.");
                break;
            case NotificationMessage.PLACE_SHIPS:
                // TODO: allow player to start positioning ships
                //view.addChatMessage("Can place ships now.");
                ownBoard.setBoatPositionLocked(false);
                break;
            case NotificationMessage.YOUR_TURN:
                view.stopTimer();
                view.setTimer(Game.TURN_TIMEOUT / 1000);
                view.setMessage("Your turn.");
                break;
            case NotificationMessage.OPPONENTS_TURN:
                view.stopTimer();
                view.setTimer(Game.TURN_TIMEOUT / 1000);
                view.addChatMessage("Opponent's turn.");
                view.setMessage("Opponent's turn.");
                break;
            case NotificationMessage.GAME_WIN:
                // TODO: inform player they have won the game
                view.setMessage("You won.");
                view.stopTimer();
                view.gameOverAction("You won!");
                break;
            case NotificationMessage.GAME_LOSE:
                // TODO: inform player they have lost the game
                view.setMessage("You lost.");
                view.stopTimer();
                view.gameOverAction("You lost!");
                break;
            case NotificationMessage.TIMEOUT_WIN:
                // TODO: inform of win due to opponent taking too long
                view.addChatMessage("Your opponent took to long, you win!");
                view.gameOverAction("Your opponent took to long, you win!");
                break;
            case NotificationMessage.TIMEOUT_LOSE:
                // TODO: inform of loss due to taking too long
                view.addChatMessage("You took too long, you lose!");
                view.gameOverAction("You took too long, you lose!");
                break;
            case NotificationMessage.TIMEOUT_DRAW:
                // TODO: inform that both took too long to place ships
                view.addChatMessage("Game ended a draw.");
                view.gameOverAction("Game ended a draw.");
                break;
            case NotificationMessage.NOT_YOUR_TURN:
                view.addChatMessage("It's not your turn!");
                break;
            case NotificationMessage.INVALID_BOARD:
                view.addChatMessage("Invalid board.");
                break;
            case NotificationMessage.NOT_IN_GAME:
                view.addChatMessage("You're not in a game.");
                break;
            case NotificationMessage.INVALID_MOVE:
                view.addChatMessage("Invalid move.");
                break;
            case NotificationMessage.REPEATED_MOVE:
                view.addChatMessage("You cannot repeat a move.");
                break;
            case NotificationMessage.OPPONENT_DISCONNECTED:
                view.addChatMessage("Opponent disconnected.");
            }
        } else if (tmp instanceof GameResponseMessage) {
            GameResponseMessage gameResponse = (GameResponseMessage) tmp;
            if (gameResponse.isOwnBoard()) {
                ownBoard.applyMove(gameResponse);
            } else {
                opponentBoard.applyMove(gameResponse);
            }
        }
        else if (tmp instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage) tmp;
            view.addChatMessage("<b>" + opponentName + ":</b> " + chatMessage.getMessage());
        }
    }

    /**
     * Sends the Board over the DataOutputStream.
     * board: The Board to send to the server
     */
    public void sendBoard(Board board) throws IOException {
        out.reset();
        out.writeUTF(String.valueOf(board));
        out.flush();
    }

    /**
     * Gets the ClientView.
     * return the ClientView belonging to the Client
     */
    public ClientView getView() {
        return view;
    }

    /**
     * Sends a message to be displayed in the opponents chat window.
     * message: The text of the message to be sent
     */
    public void sendChatMessage(String message) throws IOException {
        System.out.println(message);
        out.writeUTF(message);
        out.flush();
    }

    /**
     * Sends a move to be executed on the opponent's Board.
     * x: The index of the Square on the X-axis to be hit
     * y: The index of the {@link Square} on the Y-axis to be hit
     * 
     */
    public void sendCoordinates(int x, int y) throws IOException {
        out.writeUTF(String.valueOf(new CoordinatesMessage(x, y)));
        out.flush();
    }

    /**
     * Gets the opponent's name
     */
    public String getOpponentName() {
        return opponentName;
    }

}
