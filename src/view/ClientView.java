package view;

import client.*;
import server.Game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by alexstoick on 10/15/14.
 */
public class ClientView extends JFrame {

    private JTextField inputField = new JTextField();
    private JScrollPane chatScrollPane;
    private JList<String> chat = new JList<>();
    private DefaultListModel<String> chatModel = new DefaultListModel<>();
    private Client model;
    private MatchRoom matchRoom;
    private JLabel message;

    public ClientView(ObjectOutputStream out, final ObjectInputStream in,
                      final MatchRoom matchRoom) {
        chat.setModel(chatModel);

        JPanel rootPanel = new JPanel(new BorderLayout(5, 5));
        rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        final BoardView myBoard = new BoardView(true);
        final BoardView enemyBoard = new BoardView(false);

        model = new Client(this, myBoard.getModel(), enemyBoard.getModel(),
                out, in);
        this.matchRoom = matchRoom;

        JPanel controlPanel = new JPanel(new BorderLayout(10, 5));
        chatScrollPane = new JScrollPane(chat);

        controlPanel.add(chatScrollPane, BorderLayout.CENTER);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendChatMessage();
            }
        });


        JPanel bottomPanel = new JPanel(new GridLayout(1, 0));

        message = new JLabel("");
        message.setHorizontalAlignment(JLabel.CENTER);
        message.setVerticalAlignment(JLabel.CENTER);
        message.setFont(new Font("SansSerif", Font.PLAIN, 14));

        bottomPanel.add(message);

        JPanel boards = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        boards.add(new LabeledBoardView(myBoard));
        boards.add(new LabeledBoardView(enemyBoard));

        JPanel gamePanel = new JPanel(new BorderLayout(10, 10));

        gamePanel.add(boards, BorderLayout.CENTER);
        gamePanel.add(bottomPanel, BorderLayout.SOUTH);

        rootPanel.add(gamePanel, BorderLayout.CENTER);
        rootPanel.add(controlPanel, BorderLayout.EAST);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setContentPane(rootPanel);

        pack();
        setMinimumSize(getSize());
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                matchRoom.reopen();
            }
        });
    }

    public void setMessage(String s) {
        message.setText(s);
    }

    public void sendChatMessage() {
        try {
            String text = inputField.getText();
            model.sendChatMessage(text);
            addChatMessage("<b>" + matchRoom.getOwnName() + ":</b> " + text);
            inputField.setText("");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void addChatMessage(String text) {
        JScrollBar bar = chatScrollPane.getVerticalScrollBar();
        chatModel.addElement("<html>" + text + "</html>" + "\n");
        bar.setValue(bar.getMaximum());
    }

    public Client getModel() {
        return this.model;
    }

}
