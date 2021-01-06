package view;

import client.MatchRoom;

import javax.swing.*;
import java.awt.*;

public class InviteSentPane extends JOptionPane {

    private JDialog dialog;
    private MatchRoom matchRoom;

    /**
     * name: name of invited player
     * matchRoom: MatchRoom object to send cancellation to
     */
    public InviteSentPane(String name, MatchRoom matchRoom) {
        super();
        this.setMessage("Waiting for " + name + " to respond.");
        this.setMessageType(CANCEL_OPTION);
        String[] options = {"Cancel"};
        this.setOptions(options);
        this.matchRoom = matchRoom;
    }

    /**
     * Shows the InviteSentPane, with a cancel button. If the cancel button is
     * pressed, MatchRoom informs the server and the request is
     * cancelled.
     *
     * parent: the frame to display the dialog in
     */
    public void showPane(Component parent) {
        dialog = this.createDialog(parent, "Invite Sent");
        dialog.setVisible(true);
        dialog.dispose();
        if (getValue() == "Cancel") {
            matchRoom.sendStringArray(new String[]{"join", "cancel"});
        }
    }

    /**
     * Disposes of the frame, cancelling the user's chance to cancel the invite.
     */
    public void dispose() {
        dialog.dispose();
    }
}
