package server.messages;

import java.io.Serializable;

/**
 * An object that contains a String representing a message sent as a chat message.
 */
public class ChatMessage implements Serializable {

    private String message;

    /**
     * message: the message to send
     */
    public ChatMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the contained message.
     */
    public String getMessage() {
        return message;
    }
}
