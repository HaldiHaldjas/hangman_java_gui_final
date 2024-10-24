package listeners;

import models.Database;
import models.Model;
import views.View;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

public class ButtonSend implements ActionListener, KeyListener {

    private Model model;
    private View view;

    public ButtonSend(Model model, View view) {
        this.model = model;
        this.view = view;

        // Register ActionListener for the send button
        view.getGameBoard().getBtnSend().addActionListener(this);

        // Register KeyListener for the text field to handle Enter key press
        view.getGameBoard().getTxtChar().addKeyListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("SAADA");
        handleSendAction();
    }
    /**
     * Registreerib ka ENTER key vajutuse nupuvajutusena
     * */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            handleSendAction();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used, but must be implemented due to KeyListener interface
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not needed for handling Enter key
    }

    private void handleSendAction() {

        // kasutaja sisestus väikeste tähtedega
        String input = view.getGameBoard().getTxtChar().getText().toLowerCase().trim();
        // kontroll, kas on 1-kohaline sisestus ja täht
        if (input.length() == 1 && Character.isLetter(input.charAt(0))) {
            model.processUserInput(input.charAt(0), view);

        } else {
            // kui input ei ole tühi ja kui ei vasta esimese tingimuse nõuetele
            if (!input.isEmpty()) {
                JOptionPane.showMessageDialog(view.getGameBoard(), "Sisesta üks täht! Numbrid jm on keelatud!");
            }
        }
        view.getGameBoard().getTxtChar().setText("");
        view.getGameBoard().getTxtChar().requestFocus();
    }

}
